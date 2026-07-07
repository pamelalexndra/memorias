package com.example.pamelapp

import android.app.Application
import com.example.pamelapp.data.AppProvider
import com.google.firebase.FirebaseApp

class MemoriasApplication: Application() {
  val appProvider by lazy { AppProvider(this) }
  
  override fun onCreate() {
    super.onCreate()
    FirebaseApp.initializeApp(this)
  }
}