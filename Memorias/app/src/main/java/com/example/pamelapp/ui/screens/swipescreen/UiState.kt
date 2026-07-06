package com.example.pamelapp.ui.screens.swipescreen

import com.example.pamelapp.data.Photo
import com.example.pamelapp.ui.navigation.Screen

data class UiState(
  val loading: Boolean = true,
  val permissionDenied: Boolean = false,
  val photos: List<Photo> = emptyList(),     // todas las fotos del día
  val favoritePhotos: List<Photo> = emptyList(), // fotos favoritas
  val currentIndex: Int = 0,
  val pendingDelete: List<Photo> = emptyList(),
  val favorited: Int = 0,
  val screen: Screen = Screen.SWIPE,
  val deletedCount: Int = 0,
  val freedBytes: Long = 0L,
  val error: String? = null,
  val recycleBinMode: Boolean = false, // false = borrar permanentemente, true = enviar a papelera
)