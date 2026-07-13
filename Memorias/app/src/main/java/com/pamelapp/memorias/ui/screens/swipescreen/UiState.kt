package com.pamelapp.memorias.ui.screens.swipescreen

import com.pamelapp.memorias.domain.model.DeleteMode
import com.pamelapp.memorias.domain.model.Photo
import com.pamelapp.memorias.ui.navigation.Screen

data class UiState(
  val loading: Boolean = true,
  val permissionDenied: Boolean = false,
  val photos: List<Photo> = emptyList(),
  val allPhotos: List<Photo> = emptyList(),
  val favoritePhotos: List<Photo> = emptyList(),
  val currentIndex: Int = 0,
  val pendingDelete: List<Photo> = emptyList(),
  val favorited: Int = 0,
  val screen: Screen = Screen.SWIPE,
  val deletedCount: Int = 0,
  val freedBytes: Long = 0L,
  val error: String? = null,
  val deleteMode: DeleteMode = DeleteMode.TRASH,
  val showSwipeButtons: Boolean = true,
  val selectedYear: Int? = null,
) {
  val recycleBinMode: Boolean
    get() = deleteMode == DeleteMode.TRASH
  
  val availableYears: List<Int>
    get() = allPhotos.map { it.year }.distinct().sorted()
}