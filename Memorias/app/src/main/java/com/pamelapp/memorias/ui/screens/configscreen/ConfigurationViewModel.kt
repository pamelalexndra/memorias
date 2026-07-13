package com.pamelapp.memorias.ui.screens.configscreen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pamelapp.memorias.MemoriasApplication
import com.pamelapp.memorias.data.preferences.SwipePreferences
import com.pamelapp.memorias.data.remote.authRepository.AuthRepository
import com.pamelapp.memorias.domain.model.DeleteMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
  
  private val _isDeletingAccount = MutableStateFlow(false)
  val isDeletingAccount = _isDeletingAccount.asStateFlow()
  
  private val _deleteAccountError = MutableStateFlow<String?>(null)
  val deleteAccountError = _deleteAccountError.asStateFlow()
  
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
    if (_isDeletingAccount.value) return
    
    viewModelScope.launch {
      _isDeletingAccount.update { true }
      _deleteAccountError.update { null }
      
      authRepository.deleteAccount()
        .onSuccess {
          _logoutCompleted.update { true }
        }
        .onFailure { exception ->
          _deleteAccountError.update {
            exception.message ?: "No se pudo borrar la cuenta. Intenta de nuevo."
          }
        }
      
      _isDeletingAccount.update { false }
    }
  }
  
  fun dismissDeleteAccountError() {
    _deleteAccountError.update { null }
  }
  
  fun logout() {
    viewModelScope.launch {
      authRepository.logout()
      
      _logoutCompleted.update {
        true
      }
    }
  }
  
  fun resetLogoutState() {
    _logoutCompleted.update {
      false
    }
  }
}