package com.example.pamelapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.example.pamelapp.ui.navigation.MemoriasApp
import com.example.pamelapp.ui.navigation.Routes

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val initialRoute = if (intent.getBooleanExtra("open_login", false)) {
      Routes.LogIn
    } else {
      Routes.Welcome
    }

    setContent {
      MaterialTheme {
        MemoriasApp(initialRoute = initialRoute)
      }
    }
  }
}