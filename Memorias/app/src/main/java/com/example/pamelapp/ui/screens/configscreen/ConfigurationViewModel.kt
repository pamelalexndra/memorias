package com.example.pamelapp.ui.screens.configscreen

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import com.example.pamelapp.domain.model.DeleteMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ConfigurationViewModel : ViewModel() {

  private val prefsKey = "memorias_prefs"
  private val deleteModeKey = "delete_mode"
  private val favoritesKey = "favorites"
  private val showSwipeButtonsKey = "show_swipe_buttons"

  private val _deleteMode = MutableStateFlow(DeleteMode.TRASH)
  val deleteMode = _deleteMode.asStateFlow()

  private val _showSwipeButtons = MutableStateFlow(true)
  val showSwipeButtons = _showSwipeButtons.asStateFlow()

  fun loadSettings(context: Context) {
    val prefs = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)

    val savedDeleteMode = when (prefs.getString(deleteModeKey, DeleteMode.TRASH.name)) {
      DeleteMode.PERMANENT.name -> DeleteMode.PERMANENT
      else -> DeleteMode.TRASH
    }

    val savedShowSwipeButtons = prefs.getBoolean(showSwipeButtonsKey, true)

    _deleteMode.update { savedDeleteMode }
    _showSwipeButtons.update { savedShowSwipeButtons }
  }

  fun setDeleteMode(context: Context, mode: DeleteMode) {
    context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
      .edit {
        putString(deleteModeKey, mode.name)
      }

    _deleteMode.update { mode }
  }

  fun setShowSwipeButtons(context: Context, show: Boolean) {
    context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
      .edit {
        putBoolean(showSwipeButtonsKey, show)
      }

    _showSwipeButtons.update { show }
  }

  fun clearFavorites(context: Context) {
    context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
      .edit {
        remove(favoritesKey)
      }
  }

  fun deleteAccount() {
    // Pendiente: implementar lógica de borrado de cuenta.
  }

  fun logout() {
    // Pendiente: implementar lógica de cierre de sesión.
  }
}