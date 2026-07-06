package com.example.pamelapp.ui.screens.swipescreen

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
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
    _state.update { it.copy(pendingDelete = it.pendingDelete + photo) }
    advancePhoto()
  }
  
  fun onFavorite(context: Context) {
    val photo = currentPhoto() ?: return
    toggleFavorite(photo, context)
    advancePhoto()
  }
  
  fun removeFromQueue(photo: Photo) {
    _state.update { it.copy(pendingDelete = it.pendingDelete - photo) }
  }
  
  fun onDeleteSuccess() {
    val deleted = _state.value.pendingDelete
    val freed = deleted.sumOf { it.sizeBytes }
    _state.update {
      it.copy(
        pendingDelete = emptyList(),
        deletedCount = it.deletedCount + deleted.size,
        freedBytes = it.freedBytes + freed,
      )
    }
  }
  
  fun reset() {
    _state.update {
      UiState(
        loading = false,
        photos = it.photos,
        favoritePhotos = it.favoritePhotos,
        favorited = it.favorited,
        recycleBinMode = it.recycleBinMode
      )
    }
  }
  
  fun setPermissionDenied() {
    _state.update { it.copy(loading = false, permissionDenied = true) }
  }
  
  fun setRecycleBinMode(enabled: Boolean) {
    _state.update { it.copy(recycleBinMode = enabled) }
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

      val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATE_TAKEN,
        MediaStore.Images.Media.SIZE,
        MediaStore.Images.Media.MIME_TYPE,
        MediaStore.Images.Media.WIDTH,
        MediaStore.Images.Media.HEIGHT,
      )
      
      val results = mutableListOf<Photo>()
      
      cr.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        "${MediaStore.Images.Media.DATE_TAKEN} IS NOT NULL",
        null,
        "${MediaStore.Images.Media.DATE_TAKEN} DESC",
      )?.use { cursor ->
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
        val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
        val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
        val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
        val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
        val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
        
        while (cursor.moveToNext()) {
          val dateTakenMillis = cursor.getLong(dateCol)
          val dateTaken = Instant.ofEpochMilli(dateTakenMillis)
          val local = dateTaken.atZone(ZoneId.systemDefault()).toLocalDate()

          if (local.monthValue != thisMonth) continue
          if (local.dayOfMonth != thisDay) continue
          if (local.year >= thisYear) continue
          
          val id = cursor.getLong(idCol)
          val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
          results.add(
            Photo(
              id = id,
              uri = uri,
              dateTaken = dateTaken,
              year = local.year,
              month = local.monthValue,
              day = local.dayOfMonth,
              sizeBytes = cursor.getLong(sizeCol),
              displayName = cursor.getString(nameCol) ?: "foto",
              mimeType = cursor.getString(mimeCol) ?: "image/*",
              width = if (cursor.isNull(widthCol)) 0 else cursor.getInt(widthCol),
              height = if (cursor.isNull(heightCol)) 0 else cursor.getInt(heightCol),
            )
          )
        }
      }
      results
    }
}