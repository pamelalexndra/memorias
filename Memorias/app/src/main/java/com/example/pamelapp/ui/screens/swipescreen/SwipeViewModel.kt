package com.example.pamelapp.ui.screens.swipescreen

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.net.Uri
import android.database.Cursor
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
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
import java.time.LocalDate
import java.time.ZoneId
import kotlin.collections.filter
import java.time.Instant
import kotlin.collections.toMutableList

class SwipeViewModel : ViewModel() {
  private val _state = MutableStateFlow<UiState>(UiState())
  val state = _state.asStateFlow()
  
  private val prefsKey = "memorias_prefs"
  private val favoritesKey = "favorites"
  private val deleteModeKey = "delete_mode"

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
    _state.update { it.copy(loading = false, error = message) }
  }
  
  @RequiresApi(Build.VERSION_CODES.O)
  fun loadPhotos(contentResolver: ContentResolver) {
    viewModelScope.launch {
      _state.update { it.copy(loading = true, error = null) }
      try {
        val photos = queryOnThisDayPhotos(contentResolver)
        _state.update { it.copy(loading = false, photos = photos, currentIndex = 0) }
        loadFavoritesFromPrefs(contentResolver)
      } catch (e: Exception) {
        _state.update { it.copy(loading = false, error = e.message) }
      }
    }
  }
  
  private fun loadFavoritesFromPrefs(contentResolver: ContentResolver) {
    viewModelScope.launch {
      // Los favoritos se cargan desde el contexto, pero aquí no tenemos contexto
      // Se cargarán desde la pantalla
    }
  }
  
  fun loadFavorites(context: Context) {
    viewModelScope.launch {
      val prefs = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
      val favoriteIds = prefs.getStringSet(favoritesKey, emptySet()) ?: emptySet()
      val favoritePhotos = _state.value.photos.filter { favoriteIds.contains(it.id.toString()) }
      _state.update { it.copy(favoritePhotos = favoritePhotos, favorited = favoritePhotos.size) }
    }
  }
  
  fun toggleFavorite(photo: Photo, context: Context) {
    val prefs = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
    val favoriteIds = prefs.getStringSet(favoritesKey, emptySet())?.toMutableSet() ?: mutableSetOf()
    
    if (favoriteIds.contains(photo.id.toString())) {
      favoriteIds.remove(photo.id.toString())
    } else {
      favoriteIds.add(photo.id.toString())
    }
    
    prefs.edit { putStringSet(favoritesKey, favoriteIds) }
    
    val updatedFavorites = _state.value.favoritePhotos.toMutableList()
    if (favoriteIds.contains(photo.id.toString())) {
      updatedFavorites.add(photo)
    } else {
      updatedFavorites.remove(photo)
    }
    _state.update { it.copy(favoritePhotos = updatedFavorites, favorited = updatedFavorites.size) }
  }
  
  fun onKeep() = advancePhoto()

  fun onDelete() {
    val photo = currentPhoto() ?: return

    _state.update { state ->
      val alreadyQueued = state.pendingDelete.any { it.id == photo.id }

      state.copy(
        pendingDelete = if (alreadyQueued) {
          state.pendingDelete
        } else {
          state.pendingDelete + photo
        }
      )
    }

    advancePhoto()
  }
  
  fun onFavorite(context: Context) {
    val photo = currentPhoto() ?: return
    toggleFavorite(photo, context)
    advancePhoto()
  }

  fun removeFromQueue(photo: Photo) {
    _state.update { state ->
      state.copy(
        pendingDelete = state.pendingDelete.filterNot { it.id == photo.id }
      )
    }
  }

  fun onDeleteSuccess(context: Context) {
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
      _state.update { it.copy(loading = true, error = null) }

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

    prefs.edit {
      putStringSet(favoritesKey, favoriteIds)
    }
  }

  fun reset() {
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
    _state.update { it.copy(loading = false, permissionDenied = true) }
  }

  fun clearFavorites(context: Context) {
    val prefs = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
    prefs.edit { remove(favoritesKey) }
    _state.update { it.copy(favoritePhotos = emptyList(), favorited = 0) }
  }
  
  private fun advancePhoto() {
    _state.update { s ->
      val next = s.currentIndex + 1
      if (next >= s.photos.size) {
        s.copy(currentIndex = s.photos.size)
      } else {
        s.copy(currentIndex = next)
      }
    }
  }
  
  fun currentPhoto(): Photo? {
    val s = _state.value
    return s.photos.getOrNull(s.currentIndex)
  }
  
  fun getPendingDelete(): List<Photo> = _state.value.pendingDelete
  @RequiresApi(Build.VERSION_CODES.O)
  private suspend fun queryOnThisDayPhotos(cr: ContentResolver): List<Photo> =
    withContext(Dispatchers.IO) {
      val today = LocalDate.now()
      val thisMonth = today.monthValue
      val thisDay = today.dayOfMonth
      val thisYear = today.year
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          add(MediaStore.Images.Media.RELATIVE_PATH)
        }
      }.toTypedArray()

      val results = mutableListOf<Photo>()

      cr.query(
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
          val displayName = cursor.getStringOrNull(nameCol) ?: "foto"

          val dateTakenMillis = cursor.getLongOrNull(dateTakenCol)
          val dateAddedSeconds = cursor.getLongOrNull(dateAddedCol)
          val dateModifiedSeconds = cursor.getLongOrNull(dateModifiedCol)

          val matchedInstant = resolveBestDateForOnThisDay(
            displayName = displayName,
            dateTakenMillis = dateTakenMillis,
            dateAddedSeconds = dateAddedSeconds,
            dateModifiedSeconds = dateModifiedSeconds,
            today = today,
            zone = zone
          ) ?: continue

          val local = matchedInstant.atZone(zone).toLocalDate()

          if (local.monthValue != thisMonth) continue
          if (local.dayOfMonth != thisDay) continue
          if (local.year >= thisYear) continue

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
              sizeBytes = cursor.getLongOrNull(sizeCol) ?: 0L,
              displayName = displayName,
              mimeType = cursor.getStringOrNull(mimeCol) ?: "image/*",
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

    // Screenshots: Screenshot_2024-07-06-20-27-49-479_com.whatsapp.jpg
    Regex("""(?i).*?(\d{4})-(\d{2})-(\d{2}).*"""),

    // Algunos nombres tipo IMG_20230706_192239_375.webp
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

private fun Cursor.getLongOrNull(columnIndex: Int): Long? {
  return if (columnIndex < 0 || isNull(columnIndex)) {
    null
  } else {
    getLong(columnIndex)
  }
}

private fun Cursor.getStringOrNull(columnIndex: Int): String? {
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