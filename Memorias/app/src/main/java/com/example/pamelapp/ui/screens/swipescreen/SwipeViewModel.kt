package com.example.pamelapp.ui.screens.swipescreen

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pamelapp.domain.model.DeleteMode
import com.example.pamelapp.domain.model.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.ArrayDeque

class SwipeViewModel : ViewModel() {

  private val _state = MutableStateFlow(UiState())
  val state = _state.asStateFlow()

  private val prefsKey = "memorias_prefs"
  private val favoritesKey = "favorites"
  private val deleteModeKey = "delete_mode"

  private var cachedFavoriteIds: Set<String> = emptySet()

  private val undoStack = ArrayDeque<SwipeActionRecord>()

  private enum class SwipeActionType {
    KEEP,
    DELETE,
    FAVORITE
  }

  private data class SwipeActionRecord(
    val type: SwipeActionType,
    val photo: Photo,
    val indexBeforeAction: Int,
    val wasFavoriteBeforeAction: Boolean
  )

  fun loadSettings(context: Context) {
    val prefs = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)

    val deleteMode = when (prefs.getString(deleteModeKey, DeleteMode.TRASH.name)) {
      DeleteMode.PERMANENT.name -> DeleteMode.PERMANENT
      else -> DeleteMode.TRASH
    }

    _state.update { it.copy(deleteMode = deleteMode) }
  }

  fun setDeleteMode(context: Context, mode: DeleteMode) {
    context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
      .edit {
        putString(deleteModeKey, mode.name)
      }

    _state.update { it.copy(deleteMode = mode) }
  }

  fun setError(message: String) {
    _state.update {
      it.copy(
        loading = false,
        error = message
      )
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  fun loadPhotos(contentResolver: ContentResolver) {
    viewModelScope.launch {
      _state.update {
        it.copy(
          loading = true,
          error = null,
          permissionDenied = false
        )
      }

      try {
        val photos = queryOnThisDayPhotos(contentResolver)
        val favoritePhotos = photos.filter { cachedFavoriteIds.contains(it.id.toString()) }

        undoStack.clear()

        _state.update {
          it.copy(
            loading = false,
            photos = photos,
            favoritePhotos = favoritePhotos,
            favorited = favoritePhotos.size,
            currentIndex = 0,
            error = null,
            permissionDenied = false
          )
        }
      } catch (exception: Exception) {
        _state.update {
          it.copy(
            loading = false,
            error = exception.message ?: "No se pudieron cargar las fotos."
          )
        }
      }
    }
  }

  fun loadFavorites(context: Context) {
    val prefs = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
    cachedFavoriteIds = prefs.getStringSet(favoritesKey, emptySet()).orEmpty()

    val favoritePhotos = _state.value.photos.filter {
      cachedFavoriteIds.contains(it.id.toString())
    }

    _state.update {
      it.copy(
        favoritePhotos = favoritePhotos,
        favorited = favoritePhotos.size
      )
    }
  }

  fun toggleFavorite(photo: Photo, context: Context) {
    val prefs = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
    val favoriteIds = prefs.getStringSet(favoritesKey, emptySet())
      ?.toMutableSet()
      ?: mutableSetOf()

    val photoId = photo.id.toString()

    if (favoriteIds.contains(photoId)) {
      favoriteIds.remove(photoId)
    } else {
      favoriteIds.add(photoId)
    }

    cachedFavoriteIds = favoriteIds

    prefs.edit {
      putStringSet(favoritesKey, favoriteIds)
    }

    _state.update { state ->
      val updatedFavorites = state.favoritePhotos.toMutableList()
      val isNowFavorite = favoriteIds.contains(photoId)
      val alreadyInFavorites = updatedFavorites.any { it.id == photo.id }

      if (isNowFavorite && !alreadyInFavorites) {
        updatedFavorites.add(photo)
      }

      if (!isNowFavorite) {
        updatedFavorites.removeAll { it.id == photo.id }
      }

      state.copy(
        favoritePhotos = updatedFavorites,
        favorited = updatedFavorites.size
      )
    }
  }

  fun onKeep() {
    val photo = currentPhoto() ?: return
    val state = _state.value

    undoStack.addLast(
      SwipeActionRecord(
        type = SwipeActionType.KEEP,
        photo = photo,
        indexBeforeAction = state.currentIndex,
        wasFavoriteBeforeAction = state.favoritePhotos.any { it.id == photo.id }
      )
    )

    advancePhoto()
  }

  fun onDelete() {
    val photo = currentPhoto() ?: return
    val state = _state.value

    undoStack.addLast(
      SwipeActionRecord(
        type = SwipeActionType.DELETE,
        photo = photo,
        indexBeforeAction = state.currentIndex,
        wasFavoriteBeforeAction = state.favoritePhotos.any { it.id == photo.id }
      )
    )

    _state.update { currentState ->
      val alreadyQueued = currentState.pendingDelete.any { it.id == photo.id }

      currentState.copy(
        pendingDelete = if (alreadyQueued) {
          currentState.pendingDelete
        } else {
          currentState.pendingDelete + photo
        }
      )
    }

    advancePhoto()
  }

  fun onFavorite(context: Context) {
    val photo = currentPhoto() ?: return
    val state = _state.value

    undoStack.addLast(
      SwipeActionRecord(
        type = SwipeActionType.FAVORITE,
        photo = photo,
        indexBeforeAction = state.currentIndex,
        wasFavoriteBeforeAction = state.favoritePhotos.any { it.id == photo.id }
      )
    )

    toggleFavorite(photo, context)
    advancePhoto()
  }

  fun undoLastSwipeAction(context: Context) {
    if (undoStack.isEmpty()) return

    val lastAction = undoStack.removeLast()
    val photo = lastAction.photo

    when (lastAction.type) {
      SwipeActionType.KEEP -> {
        _state.update { state ->
          state.copy(
            currentIndex = lastAction.indexBeforeAction.coerceIn(0, state.photos.size),
            error = null,
            loading = false
          )
        }
      }

      SwipeActionType.DELETE -> {
        _state.update { state ->
          state.copy(
            pendingDelete = state.pendingDelete.filterNot { it.id == photo.id },
            currentIndex = lastAction.indexBeforeAction.coerceIn(0, state.photos.size),
            error = null,
            loading = false
          )
        }
      }

      SwipeActionType.FAVORITE -> {
        restoreFavoriteState(
          context = context,
          photo = photo,
          shouldBeFavorite = lastAction.wasFavoriteBeforeAction
        )

        _state.update { state ->
          state.copy(
            currentIndex = lastAction.indexBeforeAction.coerceIn(0, state.photos.size),
            error = null,
            loading = false
          )
        }
      }
    }
  }

  private fun restoreFavoriteState(
    context: Context,
    photo: Photo,
    shouldBeFavorite: Boolean
  ) {
    val prefs = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
    val favoriteIds = prefs.getStringSet(favoritesKey, emptySet())
      ?.toMutableSet()
      ?: mutableSetOf()

    val photoId = photo.id.toString()

    if (shouldBeFavorite) {
      favoriteIds.add(photoId)
    } else {
      favoriteIds.remove(photoId)
    }

    cachedFavoriteIds = favoriteIds

    prefs.edit {
      putStringSet(favoritesKey, favoriteIds)
    }

    _state.update { state ->
      val currentFavorites = state.favoritePhotos.toMutableList()
      val alreadyFavorite = currentFavorites.any { it.id == photo.id }

      if (shouldBeFavorite && !alreadyFavorite) {
        currentFavorites.add(photo)
      }

      if (!shouldBeFavorite) {
        currentFavorites.removeAll { it.id == photo.id }
      }

      state.copy(
        favoritePhotos = currentFavorites,
        favorited = currentFavorites.size
      )
    }
  }

  fun removeFromQueue(photo: Photo) {
    _state.update { state ->
      state.copy(
        pendingDelete = state.pendingDelete.filterNot { it.id == photo.id }
      )
    }
  }

  fun onDeleteSuccess(context: Context) {
    undoStack.clear()

    val deleted = _state.value.pendingDelete
    if (deleted.isEmpty()) return

    val deletedIds = deleted.map { it.id }.toSet()
    val deletedIdsAsString = deletedIds.map { it.toString() }.toSet()
    val freed = deleted.sumOf { it.sizeBytes }

    removeDeletedPhotosFromFavoritesPrefs(context, deletedIdsAsString)

    _state.update { state ->
      val remainingPhotos = state.photos.filterNot { it.id in deletedIds }
      val remainingFavorites = state.favoritePhotos.filterNot { it.id in deletedIds }

      state.copy(
        photos = remainingPhotos,
        favoritePhotos = remainingFavorites,
        favorited = remainingFavorites.size,
        pendingDelete = emptyList(),
        deletedCount = state.deletedCount + deleted.size,
        freedBytes = state.freedBytes + freed,
        currentIndex = state.currentIndex.coerceAtMost(remainingPhotos.size),
        error = null,
        loading = false
      )
    }
  }

  fun onDeleteCancelled() {
    _state.update {
      it.copy(
        loading = false,
        error = null
      )
    }
  }

  fun deletePendingDirectly(
    context: Context,
    contentResolver: ContentResolver,
    onComplete: () -> Unit
  ) {
    val photosToDelete = _state.value.pendingDelete
    if (photosToDelete.isEmpty()) return

    viewModelScope.launch {
      _state.update {
        it.copy(
          loading = true,
          error = null
        )
      }

      val successfullyDeleted = withContext(Dispatchers.IO) {
        photosToDelete.filter { photo ->
          try {
            contentResolver.delete(photo.uri, null, null) > 0
          } catch (exception: Exception) {
            false
          }
        }
      }

      if (successfullyDeleted.isNotEmpty()) {
        val deletedIds = successfullyDeleted.map { it.id }.toSet()
        val deletedIdsAsString = deletedIds.map { it.toString() }.toSet()
        val freed = successfullyDeleted.sumOf { it.sizeBytes }

        removeDeletedPhotosFromFavoritesPrefs(context, deletedIdsAsString)
        undoStack.clear()

        _state.update { state ->
          val remainingPending = state.pendingDelete.filterNot { it.id in deletedIds }
          val remainingPhotos = state.photos.filterNot { it.id in deletedIds }
          val remainingFavorites = state.favoritePhotos.filterNot { it.id in deletedIds }

          state.copy(
            loading = false,
            photos = remainingPhotos,
            favoritePhotos = remainingFavorites,
            favorited = remainingFavorites.size,
            pendingDelete = remainingPending,
            deletedCount = state.deletedCount + successfullyDeleted.size,
            freedBytes = state.freedBytes + freed,
            currentIndex = state.currentIndex.coerceAtMost(remainingPhotos.size),
            error = null
          )
        }

        onComplete()
      } else {
        _state.update {
          it.copy(
            loading = false,
            error = "No se pudieron borrar las fotos seleccionadas."
          )
        }
      }
    }
  }

  private fun removeDeletedPhotosFromFavoritesPrefs(
    context: Context,
    deletedIds: Set<String>
  ) {
    val prefs = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
    val favoriteIds = prefs.getStringSet(favoritesKey, emptySet())
      ?.toMutableSet()
      ?: mutableSetOf()

    favoriteIds.removeAll(deletedIds)
    cachedFavoriteIds = favoriteIds

    prefs.edit {
      putStringSet(favoritesKey, favoriteIds)
    }
  }

  fun restartReview() {
    undoStack.clear()

    _state.update {
      it.copy(
        currentIndex = 0,
        error = null,
        loading = false
      )
    }
  }

  fun reset() {
    undoStack.clear()

    _state.update {
      UiState(
        loading = false,
        photos = it.photos,
        favoritePhotos = it.favoritePhotos,
        favorited = it.favorited,
        deleteMode = it.deleteMode
      )
    }
  }

  fun setPermissionDenied() {
    _state.update {
      it.copy(
        loading = false,
        permissionDenied = true
      )
    }
  }

  fun clearFavorites(context: Context) {
    val prefs = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)

    prefs.edit {
      remove(favoritesKey)
    }

    cachedFavoriteIds = emptySet()

    _state.update {
      it.copy(
        favoritePhotos = emptyList(),
        favorited = 0
      )
    }
  }

  private fun advancePhoto() {
    _state.update { state ->
      val nextIndex = state.currentIndex + 1

      state.copy(
        currentIndex = if (nextIndex >= state.photos.size) {
          state.photos.size
        } else {
          nextIndex
        }
      )
    }
  }

  fun currentPhoto(): Photo? {
    val state = _state.value
    return state.photos.getOrNull(state.currentIndex)
  }

  fun getPendingDelete(): List<Photo> {
    return _state.value.pendingDelete
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private suspend fun queryOnThisDayPhotos(contentResolver: ContentResolver): List<Photo> =
    withContext(Dispatchers.IO) {
      val today = LocalDate.now()
      val zone = ZoneId.systemDefault()

      val projection = buildList {
        add(MediaStore.Images.Media._ID)
        add(MediaStore.Images.Media.DISPLAY_NAME)
        add(MediaStore.Images.Media.DATE_TAKEN)
        add(MediaStore.Images.Media.DATE_ADDED)
        add(MediaStore.Images.Media.DATE_MODIFIED)
        add(MediaStore.Images.Media.SIZE)
        add(MediaStore.Images.Media.MIME_TYPE)
        add(MediaStore.Images.Media.WIDTH)
        add(MediaStore.Images.Media.HEIGHT)
      }.toTypedArray()

      val results = mutableListOf<Photo>()

      contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
      )?.use { cursor ->
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
        val dateTakenCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
        val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
        val dateModifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
        val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
        val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
        val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
        val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

        while (cursor.moveToNext()) {
          val id = cursor.getLong(idCol)
          val displayName = cursor.getStringOrNullSafe(nameCol) ?: "foto"

          val dateTakenMillis = cursor.getLongOrNullSafe(dateTakenCol)
          val dateAddedSeconds = cursor.getLongOrNullSafe(dateAddedCol)
          val dateModifiedSeconds = cursor.getLongOrNullSafe(dateModifiedCol)

          val matchedInstant = resolveBestDateForOnThisDay(
            displayName = displayName,
            dateTakenMillis = dateTakenMillis,
            dateAddedSeconds = dateAddedSeconds,
            dateModifiedSeconds = dateModifiedSeconds,
            today = today,
            zone = zone
          ) ?: continue

          val local = matchedInstant.atZone(zone).toLocalDate()

          val uri = ContentUris.withAppendedId(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            id
          )

          results.add(
            Photo(
              id = id,
              uri = uri,
              dateTaken = matchedInstant,
              year = local.year,
              month = local.monthValue,
              day = local.dayOfMonth,
              sizeBytes = cursor.getLongOrNullSafe(sizeCol) ?: 0L,
              displayName = displayName,
              mimeType = cursor.getStringOrNullSafe(mimeCol) ?: "image/*",
              width = cursor.getIntOrZero(widthCol),
              height = cursor.getIntOrZero(heightCol),
            )
          )
        }
      }

      results
        .distinctBy { it.id }
        .sortedByDescending { it.dateTaken }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun resolveBestDateForOnThisDay(
  displayName: String,
  dateTakenMillis: Long?,
  dateAddedSeconds: Long?,
  dateModifiedSeconds: Long?,
  today: LocalDate,
  zone: ZoneId
): Instant? {
  val candidateInstants = listOfNotNull(
    dateTakenMillis
      ?.takeIf { it > 0L }
      ?.let { Instant.ofEpochMilli(it) },

    extractDateFromFileName(displayName)
      ?.atStartOfDay(zone)
      ?.toInstant(),

    dateModifiedSeconds
      ?.takeIf { it > 0L }
      ?.let { Instant.ofEpochSecond(it) },

    dateAddedSeconds
      ?.takeIf { it > 0L }
      ?.let { Instant.ofEpochSecond(it) },
  )

  return candidateInstants.firstOrNull { instant ->
    val local = instant.atZone(zone).toLocalDate()

    local.monthValue == today.monthValue &&
            local.dayOfMonth == today.dayOfMonth &&
            local.year < today.year
  }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun extractDateFromFileName(fileName: String): LocalDate? {
  val patterns = listOf(
    // WhatsApp: IMG-20240706-WA0006.jpg
    Regex("""(?i).*?(\d{4})(\d{2})(\d{2}).*"""),

    // Screenshot_2024-07-06-20-27-49-479_com.whatsapp.jpg
    Regex("""(?i).*?(\d{4})-(\d{2})-(\d{2}).*"""),

    // Instagram: IMG_20230706_192239_375.webp
    Regex("""(?i).*?(\d{4})_(\d{2})(\d{2}).*""")
  )

  for (regex in patterns) {
    val match = regex.matchEntire(fileName) ?: continue

    val year = match.groupValues[1].toIntOrNull() ?: continue
    val month = match.groupValues[2].toIntOrNull() ?: continue
    val day = match.groupValues[3].toIntOrNull() ?: continue

    try {
      return LocalDate.of(year, month, day)
    } catch (exception: DateTimeException) {
      continue
    }
  }

  return null
}

private fun Cursor.getLongOrNullSafe(columnIndex: Int): Long? {
  return if (columnIndex < 0 || isNull(columnIndex)) {
    null
  } else {
    getLong(columnIndex)
  }
}

private fun Cursor.getStringOrNullSafe(columnIndex: Int): String? {
  return if (columnIndex < 0 || isNull(columnIndex)) {
    null
  } else {
    getString(columnIndex)
  }
}

private fun Cursor.getIntOrZero(columnIndex: Int): Int {
  return if (columnIndex < 0 || isNull(columnIndex)) {
    0
  } else {
    getInt(columnIndex)
  }
}