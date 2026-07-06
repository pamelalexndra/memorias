package com.example.pamelapp.ui.screens.configscreen

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConfigurationViewModel : ViewModel() {
  private val _recycleBinMode = MutableStateFlow<Boolean>(false)
  val recycleBinMode = _recycleBinMode.asStateFlow()
  
  private val prefsKey = "memorias_prefs"
  
  fun loadSettings(context: Context) {
    val prefs = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
    _recycleBinMode.update { prefs.getBoolean("recycle_bin_mode", false) }
  }
  
  fun setRecycleBinMode(enabled: Boolean) {
    _recycleBinMode.update { enabled }
    viewModelScope.launch {
      // Guardar en SharedPreferences cuando se implemente
    }
  }
  
  fun clearFavorites(context: Context) {
    val prefs = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
    prefs.edit { remove("favorites") }
  }
  
  fun deleteAccount() {
    // Implementar lógica de borrado de cuenta
  }
  
  fun logout() {
    // Implementar lógica de cierre de sesión
  }
}