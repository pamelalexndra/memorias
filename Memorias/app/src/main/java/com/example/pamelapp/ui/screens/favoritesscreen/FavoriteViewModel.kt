package com.example.pamelapp.ui.screens.favoritesscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pamelapp.MemoriasApplication
import com.example.pamelapp.domain.model.Photo
import com.example.pamelapp.domain.repository.favoritePhotoRepository.FavoritePhotoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritePhotoViewModel(private val favoritePhotoRepository: FavoritePhotoRepository) :
  ViewModel() {
  
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
    }
  }
  
  fun deleteFavoritePhoto(favoritePhoto: Photo) {
    viewModelScope.launch {
      favoritePhotoRepository.removeFavoritePhoto(favoritePhoto)
    }
  }
  
  fun clearFavoritePhotos() {
    viewModelScope.launch {
      favoritePhotoRepository.removeFavoritePhotos()
    }
  }
  
  companion object {
    fun provideFactory() = viewModelFactory {
      initializer {
        val app = this[APPLICATION_KEY] as MemoriasApplication
        FavoritePhotoViewModel(app.appProvider.provideFavoritePhotoRepository())
      }
    }
  }
  
}