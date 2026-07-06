package com.example.pamelapp.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class Routes : NavKey {
  @Serializable
  data object Welcome : Routes()
  
  @Serializable
  data object LogIn : Routes()
  
  @Serializable
  data object Register : Routes()
  
  @Serializable
  data object PhotosScreen : Routes()
  
  @Serializable
  data object Favorites : Routes()
  
  @Serializable
  data object DeletePhoto : Routes()
  
  @Serializable
  data object Configuration : Routes()
  
}