package com.example.pamelapp.ui.screens.configscreen

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import com.example.pamelapp.domain.model.DeleteMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ConfigurationViewModel : ViewModel() {
  private val _deleteMode = MutableStateFlow(DeleteMode.TRASH)
  val deleteMode = _deleteMode.asStateFlow()

  private val prefsKey = "memorias_prefs"
  private val deleteModeKey = "delete_mode"
  private val favoritesKey = "favorites"
  private val showSwipeButtonsKey = "show_swipe_buttons"

  private val _showSwipeButtons = MutableStateFlow(true)
  val showSwipeButtons = _showSwipeButtons.asStateFlow()

  fun loadSettings(context: Context) {
    val prefs = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
    val showSwipeButtons = prefs.getBoolean(showSwipeButtonsKey, true)

    val savedMode = when (prefs.getString(deleteModeKey, DeleteMode.TRASH.name)) {
      DeleteMode.PERMANENT.name -> DeleteMode.PERMANENT
      else -> DeleteMode.TRASH
    }

    _showSwipeButtons.update { showSwipeButtons }
    _deleteMode.update { savedMode }
  }

  fun setShowSwipeButtons(context: Context, show: Boolean) {
    context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
      .edit {
        putBoolean(showSwipeButtonsKey, show)
      }

    _showSwipeButtons.update { show }
  }
  fun setDeleteMode(context: Context, mode: DeleteMode) {
    context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
      .edit {
        putString(deleteModeKey, mode.name)
      }

    _deleteMode.update { mode }
  }

  fun clearFavorites(context: Context) {
    val prefs = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
    prefs.edit { remove(favoritesKey) }
  }

  fun deleteAccount() {
    // Implementar lógica de borrado de cuenta
  }

  fun logout() {
    // Implementar lógica de cierre de sesión
  }
}