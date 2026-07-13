package com.pamelapp.memorias.ui.screens.favoritesscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pamelapp.memorias.MemoriasApplication
import com.pamelapp.memorias.domain.model.Photo
import com.pamelapp.memorias.domain.repository.favoritePhotoRepository.FavoritePhotoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.pamelapp.memorias.widget.FavoriteMemoryWidgetProvider

class FavoritePhotoViewModel(
  private val favoritePhotoRepository: FavoritePhotoRepository,
  private val app: MemoriasApplication
) : ViewModel() {
  
  val favoritePhotos: StateFlow<List<Photo>> = favoritePhotoRepository.getFavoritePhotos()
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = emptyList()
    )
  
  val favoritePhotosIds: StateFlow<Set<Long>> = favoritePhotos
    .map { movies -> movies.map { it.id }.toSet() }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = emptySet()
    )
  
  fun addFavoritePhoto(favoritePhoto: Photo) {
    viewModelScope.launch {
      favoritePhotoRepository.addFavoritePhoto(favoritePhoto)
      FavoriteMemoryWidgetProvider.updateAllWidgets(app.applicationContext)
    }
  }
  
  fun deleteFavoritePhoto(favoritePhoto: Photo) {
    viewModelScope.launch {
      favoritePhotoRepository.removeFavoritePhoto(favoritePhoto)
      FavoriteMemoryWidgetProvider.updateAllWidgets(app.applicationContext)
    }
  }
  
  fun clearFavoritePhotos() {
    viewModelScope.launch {
      favoritePhotoRepository.removeFavoritePhotos()
      FavoriteMemoryWidgetProvider.updateAllWidgets(app.applicationContext)
    }
  }
  
  companion object {
    fun provideFactory() = viewModelFactory {
      initializer {
        val app = this[APPLICATION_KEY] as MemoriasApplication
        FavoritePhotoViewModel(
          favoritePhotoRepository = app.appProvider.provideFavoritePhotoRepository(),
          app = app
        )
      }
    }
  }
  
}