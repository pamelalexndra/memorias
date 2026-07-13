package com.pamelapp.memorias

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.pamelapp.memorias.ui.navigation.AppRoot

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    val openLogin = intent.getBooleanExtra("open_login", false)
    
    setContent {
      MaterialTheme {
        AppRoot(openLogin = openLogin)
      }
    }
  }
}