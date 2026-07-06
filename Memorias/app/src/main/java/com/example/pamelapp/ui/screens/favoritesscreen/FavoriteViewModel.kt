package com.example.pamelapp.ui.screens.favoritesscreen

import android.content.ContentResolver
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pamelapp.data.Photo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoriteViewModel : ViewModel() {
  private val _favorites = MutableStateFlow<List<Photo>>(emptyList())
  val favorites = _favorites.asStateFlow()
  
  private val _recycleBinMode = MutableStateFlow<Boolean>(false)
  val recycleBinMode = _recycleBinMode.asStateFlow()
  
  private val _isLoading = MutableStateFlow<Boolean>(false)
  val isLoading = _isLoading.asStateFlow()
  
  fun loadFavorites(context: Context) {
    viewModelScope.launch {
      val prefs = context.getSharedPreferences("memorias_prefs", Context.MODE_PRIVATE)
      val favoriteIds = prefs.getStringSet("favorites", emptySet()) ?: emptySet()
      _recycleBinMode.update { prefs.getBoolean("recycle_bin_mode", false) }
      // Los fotos se cargarán desde SwipeViewModel, por ahora mantenemos lista vacía
    }
  }
  
  fun updateFavorites(photos: List<Photo>) {
    _favorites.update { photos }
  }
  
  fun deleteSelected(
    photos: List<Photo>,
    contentResolver: ContentResolver,
    launcher: ActivityResultLauncher<IntentSenderRequest>
  ) {
    viewModelScope.launch {
      _isLoading.update { true }
      withContext(kotlinx.coroutines.Dispatchers.IO) {
        if (_recycleBinMode.value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          val uris = photos.map { it.uri }
          val pendingIntent = MediaStore.createTrashRequest(contentResolver, uris, true)
          launcher.launch(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
        } else {
          photos.forEach { photo ->
            contentResolver.delete(photo.uri, null, null)
          }
          onDeleteSuccess(photos)
        }
      }
      _isLoading.update { false }
    }
  }
  
  fun onDeleteSuccess(deletedPhotos: List<Photo>) {
    _favorites.update { current -> current.filterNot { deletedPhotos.contains(it) } }
  }
}