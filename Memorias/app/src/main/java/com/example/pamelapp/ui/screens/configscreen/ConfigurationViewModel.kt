package com.example.pamelapp.ui.screens.configscreen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.pamelapp.MemoriasApplication
import com.example.pamelapp.data.preferences.SwipePreferences
import com.example.pamelapp.data.repository.AuthRepository
import com.example.pamelapp.domain.model.DeleteMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ConfigurationViewModel(
  application: Application
) : AndroidViewModel(application) {

  private val appProvider = (application as MemoriasApplication).appProvider

  private val preferences: SwipePreferences =
    appProvider.provideSwipePreferences()

  private val authRepository: AuthRepository =
    appProvider.provideAuthRepository()

  private val _deleteMode = MutableStateFlow(DeleteMode.TRASH)
  val deleteMode = _deleteMode.asStateFlow()

  private val _showSwipeButtons = MutableStateFlow(true)
  val showSwipeButtons = _showSwipeButtons.asStateFlow()

  private val _logoutCompleted = MutableStateFlow(false)
  val logoutCompleted = _logoutCompleted.asStateFlow()

  fun loadSettings() {
    _deleteMode.update {
      preferences.getDeleteMode()
    }

    _showSwipeButtons.update {
      preferences.getShowSwipeButtons()
    }
  }

  fun setDeleteMode(mode: DeleteMode) {
    preferences.setDeleteMode(mode)

    _deleteMode.update {
      mode
    }
  }

  fun setShowSwipeButtons(show: Boolean) {
    preferences.setShowSwipeButtons(show)

    _showSwipeButtons.update {
      show
    }
  }

  fun deleteAccount() {
    // Pendiente
  }

  fun logout() {
    authRepository.logout()

    _logoutCompleted.update {
      true
    }
  }

  fun resetLogoutState() {
    _logoutCompleted.update {
      false
    }
  }
}