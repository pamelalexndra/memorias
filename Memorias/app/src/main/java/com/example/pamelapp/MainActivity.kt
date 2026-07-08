package com.example.pamelapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.example.pamelapp.data.preferences.SwipePreferences
import com.example.pamelapp.ui.navigation.MemoriasApp
import com.example.pamelapp.ui.navigation.Routes

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val preferences = SwipePreferences(applicationContext)

    val initialRoute = when {
      intent.getBooleanExtra("open_login", false) -> {
        Routes.LogIn
      }

      preferences.isLoggedIn() -> {
        Routes.PhotosScreen
      }

      else -> {
        Routes.Welcome
      }
    }

    setContent {
      MaterialTheme {
        MemoriasApp(initialRoute = initialRoute)
      }
    }
  }
}