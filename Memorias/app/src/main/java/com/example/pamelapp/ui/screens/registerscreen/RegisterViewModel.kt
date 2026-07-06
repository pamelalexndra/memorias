package com.example.pamelapp.ui.screens.registerscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
  private val _isLoading = MutableStateFlow<Boolean?>(null)
  val isLoading = _isLoading.asStateFlow()
  
  private val _email = MutableStateFlow<String>("")
  val email = _email.asStateFlow()
  
  private val _username = MutableStateFlow<String>("")
  val username = _username.asStateFlow()
  
  private val _password = MutableStateFlow<String>("")
  val password = _password.asStateFlow()
  
  private val _confirmPassword = MutableStateFlow<String>("")
  val confirmPassword = _confirmPassword.asStateFlow()
  
  private val _emailAvailable = MutableStateFlow<Boolean?>(null)
  val emailAvailable = _emailAvailable.asStateFlow()
  
  private val _usernameAvailable = MutableStateFlow<Boolean?>(null)
  val usernameAvailable = _usernameAvailable.asStateFlow()
  
  private val _error = MutableStateFlow<String?>(null)
  val error = _error.asStateFlow()
  
  private val _onSuccess = MutableStateFlow(false)
  val onSuccess = _onSuccess.asStateFlow()
  
  private val _navigateToBack = MutableStateFlow(false)
  val navigateToBack = _navigateToBack.asStateFlow()
  
  fun isFormValid(): Boolean {
    return _email.value.isNotBlank() &&
            _email.value.contains("@") &&
            _username.value.isNotBlank() &&
            _username.value.length >= 3 &&
            _password.value.length >= 6 &&
            _password.value == _confirmPassword.value &&
            _emailAvailable.value == true &&
            _usernameAvailable.value == true
  }
  
  fun updateEmail(value: String) {
    _email.update { value }
    if (value.isNotEmpty() && value.contains("@")) {
      checkEmailAvailability(value)
    } else {
      _emailAvailable.update { null }
    }
  }
  
  fun updateUsername(value: String) {
    _username.update { value }
    if (value.isNotEmpty() && value.length >= 3) {
      checkUsernameAvailability(value)
    } else {
      _usernameAvailable.update { null }
    }
  }
  
  fun updatePassword(value: String) {
    _password.update { value }
  }
  
  fun updateConfirmPassword(value: String) {
    _confirmPassword.update { value }
  }
  
  fun checkEmailAvailability(email: String) {
    viewModelScope.launch {
      delay(500)
      // Simular verificación de disponibilidad
      _emailAvailable.update { !email.contains("test") }
    }
  }
  
  fun checkUsernameAvailability(username: String) {
    viewModelScope.launch {
      delay(500)
      // Simular verificación de disponibilidad
      _usernameAvailable.update { !username.contains("admin") }
    }
  }
  
  fun onRegisterClick() {
    viewModelScope.launch {
      _isLoading.update { true }
      _error.update { null }
      delay(1000)
      
      if (isFormValid()) {
        _onSuccess.update { true }
      } else {
        val errorMessage = when {
          _email.value.isBlank() || !_email.value.contains("@") -> "Correo electrónico inválido"
          _username.value.isBlank() || _username.value.length < 3 -> "Usuario debe tener al menos 3 caracteres"
          _password.value.length < 6 -> "Contraseña debe tener al menos 6 caracteres"
          _password.value != _confirmPassword.value -> "Las contraseñas no coinciden"
          _emailAvailable.value == false -> "Correo electrónico no disponible"
          _usernameAvailable.value == false -> "Usuario no disponible"
          else -> "Datos inválidos"
        }
        _error.update { errorMessage }
      }
      _isLoading.update { false }
    }
  }
  
  
  fun navigateToBack() {
    _navigateToBack.update { true }
  }
}