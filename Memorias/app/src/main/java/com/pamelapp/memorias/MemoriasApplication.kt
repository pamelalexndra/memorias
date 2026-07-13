package com.pamelapp.memorias

import android.app.Application
import android.util.Log
import com.pamelapp.memorias.data.AppProvider
import com.google.firebase.FirebaseApp

class MemoriasApplication: Application() {
  val appProvider by lazy { AppProvider(this) }
  
  override fun onCreate() {
    super.onCreate()
    Log.d("FirebaseCheck", "Apps inicializadas: ${FirebaseApp.getApps(this).size}")
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this) // fuerza init manual como fallback
        }
  }
}