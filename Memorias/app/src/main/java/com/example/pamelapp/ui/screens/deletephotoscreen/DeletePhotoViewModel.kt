package com.example.pamelapp.ui.screens.deletephotoscreen

import androidx.lifecycle.ViewModel
import com.example.pamelapp.data.Photo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DeletePhotoViewModel : ViewModel() {
  private val _pendingDelete = MutableStateFlow<List<Photo>>(emptyList())
  val pendingDelete = _pendingDelete.asStateFlow()
  
  fun updatePendingDelete(photos: List<Photo>) {
    _pendingDelete.update { photos }
  }
  
  fun removeFromQueue(photo: Photo) {
    _pendingDelete.update { current ->
      current.filter { it.id != photo.id } }
  }
  
  fun onDeleteConfirmed() {
    _pendingDelete.update { emptyList() }
  }
}