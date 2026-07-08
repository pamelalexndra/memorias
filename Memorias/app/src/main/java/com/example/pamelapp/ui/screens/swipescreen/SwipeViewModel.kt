package com.example.pamelapp.ui.screens.swipescreen

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pamelapp.MemoriasApplication
import com.example.pamelapp.data.media.MediaStorePhotoRepository
import com.example.pamelapp.data.preferences.SwipePreferences
import com.example.pamelapp.domain.model.DeleteMode
import com.example.pamelapp.domain.model.Photo
import com.example.pamelapp.domain.model.SwipeActionRecord
import com.example.pamelapp.domain.model.SwipeActionType
import com.example.pamelapp.domain.repository.PhotoRepository
import com.example.pamelapp.domain.repository.favoritePhotoRepository.FavoritePhotoRepository
import com.example.pamelapp.widget.FavoriteMemoryWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayDeque

class SwipeViewModel(
  application: Application
) : AndroidViewModel(application) {

  private val appContext = application.applicationContext

  private val photoRepository: PhotoRepository =
    MediaStorePhotoRepository(appContext.contentResolver)

  private val favoritePhotoRepository: FavoritePhotoRepository =
    (application as MemoriasApplication).appProvider.provideFavoritePhotoRepository()

  private val preferences = SwipePreferences(appContext)

  private val _state = MutableStateFlow(UiState())
  val state = _state.asStateFlow()

  private var cachedFavoriteIds: Set<Long> = emptySet()

  private val undoStack = ArrayDeque<SwipeActionRecord>()

  fun loadSettings() {
    _state.update {
      it.copy(
        deleteMode = preferences.getDeleteMode(),
        showSwipeButtons = preferences.getShowSwipeButtons()
      )
    }
  }

  fun setDeleteMode(mode: DeleteMode) {
    preferences.setDeleteMode(mode)

    _state.update {
      it.copy(deleteMode = mode)
    }
  }

  fun setShowSwipeButtons(show: Boolean) {
    preferences.setShowSwipeButtons(show)

    _state.update {
      it.copy(showSwipeButtons = show)
    }
  }

  fun setError(message: String) {
    _state.update {
      it.copy(
        loading = false,
        error = message
      )
    }
  }
  
  fun filterByYear(year: Int?) {
    _state.update { currentState ->
      val filteredPhotos = if (year != null) {
        currentState.allPhotos.filter { it.year == year }
      } else {
        currentState.allPhotos
      }
      
      currentState.copy(
        photos = filteredPhotos,
        currentIndex = 0,
        selectedYear = year,
        error = null,
        loading = false
      )
    }
    
    updateFavoritePhotosFromIds(cachedFavoriteIds)
  }
  
  @RequiresApi(Build.VERSION_CODES.O)
  fun loadPhotos() {
    viewModelScope.launch {
      _state.update {
        it.copy(
          loading = true,
          error = null,
          permissionDenied = false
        )
      }
      
      try {
        val favoritePhotosFromRoom = favoritePhotoRepository.getFavoritePhotosOnce()
        cachedFavoriteIds = favoritePhotosFromRoom.map { it.id }.toSet()
        
        val photos = photoRepository.getPhotosOnThisDay()
        
        val allPhotos = photos.toList()
        
        val favoritePhotos = photos.filter {
          it.id in cachedFavoriteIds
        }
        
        undoStack.clear()
        
        _state.update {
          it.copy(
            loading = false,
            allPhotos = allPhotos,
            photos = allPhotos,
            favoritePhotos = favoritePhotos,
            favorited = favoritePhotos.size,
            currentIndex = 0,
            error = null,
            permissionDenied = false,
            selectedYear = null
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

  fun loadFavorites() {
    viewModelScope.launch {
      val favoritePhotosFromRoom = favoritePhotoRepository.getFavoritePhotosOnce()
      val favoriteIds = favoritePhotosFromRoom.map { it.id }.toSet()

      syncFavoriteIds(favoriteIds)
    }
  }

  fun syncFavoriteIds(
    favoriteIds: Set<Long>
  ) {
    cachedFavoriteIds = favoriteIds
    updateFavoritePhotosFromIds(favoriteIds)
  }

  private fun markFavorite(
    photo: Photo
  ) {
    if (photo.id in cachedFavoriteIds) {
      updateFavoritePhotosFromIds(cachedFavoriteIds)
      return
    }

    val updatedIds = cachedFavoriteIds + photo.id
    cachedFavoriteIds = updatedIds

    updateFavoritePhotosFromIds(updatedIds)

    viewModelScope.launch {
      favoritePhotoRepository.addFavoritePhoto(photo)
      FavoriteMemoryWidgetProvider.updateAllWidgets(appContext)
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
        wasFavoriteBeforeAction = photo.id in cachedFavoriteIds
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
        wasFavoriteBeforeAction = photo.id in cachedFavoriteIds
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

  fun onFavorite() {
    val photo = currentPhoto() ?: return
    val state = _state.value

    val wasFavoriteBeforeAction = photo.id in cachedFavoriteIds

    undoStack.addLast(
      SwipeActionRecord(
        type = SwipeActionType.FAVORITE,
        photo = photo,
        indexBeforeAction = state.currentIndex,
        wasFavoriteBeforeAction = wasFavoriteBeforeAction
      )
    )

    markFavorite(photo)
    advancePhoto()
  }

  fun undoLastSwipeAction() {
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
    photo: Photo,
    shouldBeFavorite: Boolean
  ) {
    val updatedIds = if (shouldBeFavorite) {
      cachedFavoriteIds + photo.id
    } else {
      cachedFavoriteIds - photo.id
    }

    cachedFavoriteIds = updatedIds
    updateFavoritePhotosFromIds(updatedIds)

    viewModelScope.launch {
      if (shouldBeFavorite) {
        favoritePhotoRepository.addFavoritePhoto(photo)
      } else {
        favoritePhotoRepository.removeFavoritePhoto(photo)
      }

      FavoriteMemoryWidgetProvider.updateAllWidgets(appContext)
    }
  }

  fun removeFromQueue(photo: Photo) {
    _state.update { state ->
      state.copy(
        pendingDelete = state.pendingDelete.filterNot { it.id == photo.id }
      )
    }
  }
  
  fun onDeleteSuccess() {
    undoStack.clear()
    
    val deleted = _state.value.pendingDelete
    if (deleted.isEmpty()) return
    
    val deletedIds = deleted.map { it.id }.toSet()
    val freed = deleted.sumOf { it.sizeBytes }
    
    removeDeletedPhotosFromFavorites(deletedIds)
    
    _state.update { state ->
      val remainingPhotos = state.photos.filterNot { it.id in deletedIds }
      val remainingAllPhotos = state.allPhotos.filterNot { it.id in deletedIds }
      val remainingFavorites = state.favoritePhotos.filterNot { it.id in deletedIds }
      
      state.copy(
        photos = remainingPhotos,
        allPhotos = remainingAllPhotos,
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
            appContext.contentResolver.delete(photo.uri, null, null) > 0
          } catch (exception: Exception) {
            false
          }
        }
      }

      if (successfullyDeleted.isNotEmpty()) {
        val deletedIds = successfullyDeleted.map { it.id }.toSet()
        val freed = successfullyDeleted.sumOf { it.sizeBytes }

        removeDeletedPhotosFromFavorites(deletedIds)
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
            error = "No se pudieron borrar las fotos."
          )
        }
      }
    }
  }

  private fun removeDeletedPhotosFromFavorites(
    deletedIds: Set<Long>
  ) {
    cachedFavoriteIds = cachedFavoriteIds - deletedIds

    updateFavoritePhotosFromIds(cachedFavoriteIds)

    viewModelScope.launch {
      favoritePhotoRepository.removeFavoritePhotosByIds(deletedIds)
      FavoriteMemoryWidgetProvider.updateAllWidgets(appContext)
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
        deleteMode = it.deleteMode,
        showSwipeButtons = it.showSwipeButtons
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

  fun clearFavorites() {
    cachedFavoriteIds = emptySet()

    _state.update {
      it.copy(
        favoritePhotos = emptyList(),
        favorited = 0
      )
    }

    viewModelScope.launch {
      favoritePhotoRepository.removeFavoritePhotos()
      FavoriteMemoryWidgetProvider.updateAllWidgets(appContext)
    }
  }
  
  private fun updateFavoritePhotosFromIds(favoriteIds: Set<Long>) {
    val currentPhotos = _state.value.photos
    
    val favoritePhotos = currentPhotos.filter {
      it.id in favoriteIds
    }
    
    _state.update {
      it.copy(
        favoritePhotos = favoritePhotos,
        favorited = favoritePhotos.size
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
    return _state.value.photos.getOrNull(_state.value.currentIndex)
  }

  fun getPendingDelete(): List<Photo> {
    return _state.value.pendingDelete
  }
}