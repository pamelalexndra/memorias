package com.example.pamelapp

import android.app.Application
import com.example.pamelapp.data.AppProvider

class MemoriasApplication: Application() {
  val appProvider by lazy { AppProvider(this) }
}