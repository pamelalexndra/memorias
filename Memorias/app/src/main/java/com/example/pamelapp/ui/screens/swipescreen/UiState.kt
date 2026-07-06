package com.example.pamelapp.ui.screens.swipescreen

import com.example.pamelapp.domain.model.DeleteMode
import com.example.pamelapp.domain.model.Photo
import com.example.pamelapp.ui.navigation.Screen

data class UiState(
  val loading: Boolean = true,
  val permissionDenied: Boolean = false,
  val photos: List<Photo> = emptyList(),
  val favoritePhotos: List<Photo> = emptyList(),
  val currentIndex: Int = 0,
  val pendingDelete: List<Photo> = emptyList(),
  val favorited: Int = 0,
  val screen: Screen = Screen.SWIPE,
  val deletedCount: Int = 0,
  val freedBytes: Long = 0L,
  val error: String? = null,
  val deleteMode: DeleteMode = DeleteMode.TRASH,
) {
  val recycleBinMode: Boolean
    get() = deleteMode == DeleteMode.TRASH
}