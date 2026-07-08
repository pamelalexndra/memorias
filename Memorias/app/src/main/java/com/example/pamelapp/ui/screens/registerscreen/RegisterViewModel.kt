package com.example.pamelapp.ui.screens.registerscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pamelapp.MemoriasApplication
import com.example.pamelapp.data.remote.AuthApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
  private val authApiService: AuthApiService
) : ViewModel() {

  private val _isLoading = MutableStateFlow(false)
  val isLoading = _isLoading.asStateFlow()

  private val _email = MutableStateFlow("")
  val email = _email.asStateFlow()

  private val _username = MutableStateFlow("")
  val username = _username.asStateFlow()

  private val _password = MutableStateFlow("")
  val password = _password.asStateFlow()

  private val _confirmPassword = MutableStateFlow("")
  val confirmPassword = _confirmPassword.asStateFlow()

  private val _emailAvailable = MutableStateFlow<Boolean?>(null)
  val emailAvailable = _emailAvailable.asStateFlow()

  private val _usernameAvailable = MutableStateFlow<Boolean?>(null)
  val usernameAvailable = _usernameAvailable.asStateFlow()

  private val _error = MutableStateFlow<String?>(null)
  val error = _error.asStateFlow()

  private val _onSuccess = MutableStateFlow(false)
  val onSuccess = _onSuccess.asStateFlow()

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
    _error.update { null }

    if (value.isNotBlank() && value.contains("@")) {
      checkEmailAvailability(value)
    } else {
      _emailAvailable.update { null }
    }
  }

  fun updateUsername(value: String) {
    _username.update { value }
    _error.update { null }

    if (value.isNotBlank() && value.length >= 3) {
      checkUsernameAvailability(value)
    } else {
      _usernameAvailable.update { null }
    }
  }

  fun updatePassword(value: String) {
    _password.update { value }
    _error.update { null }
  }

  fun updateConfirmPassword(value: String) {
    _confirmPassword.update { value }
    _error.update { null }
  }

  fun checkEmailAvailability(email: String) {
    viewModelScope.launch {
      authApiService.checkEmailAvailability(email.trim())
        .onSuccess { available ->
          _emailAvailable.update { available }
        }
        .onFailure {
          _emailAvailable.update { null }
        }
    }
  }

  fun checkUsernameAvailability(username: String) {
    viewModelScope.launch {
      authApiService.checkUsernameAvailability(username.trim())
        .onSuccess { available ->
          _usernameAvailable.update { available }
        }
        .onFailure {
          _usernameAvailable.update { null }
        }
    }
  }

  fun onRegisterClick() {
    viewModelScope.launch {
      _isLoading.update { true }
      _error.update { null }

      if (!isFormValid()) {
        _error.update { getValidationErrorMessage() }
        _isLoading.update { false }
        return@launch
      }

      authApiService.register(
        email = _email.value.trim(),
        username = _username.value.trim(),
        password = _password.value
      ).onSuccess {
        _onSuccess.update { true }
      }.onFailure { exception ->
        _error.update {
          exception.message ?: "No se pudo registrar el usuario."
        }
      }

      _isLoading.update { false }
    }
  }

  private fun getValidationErrorMessage(): String {
    return when {
      _email.value.isBlank() || !_email.value.contains("@") -> {
        "Correo electrónico inválido."
      }

      _username.value.isBlank() || _username.value.length < 3 -> {
        "El usuario debe tener al menos 3 caracteres."
      }

      _password.value.length < 6 -> {
        "La contraseña debe tener al menos 6 caracteres."
      }

      _password.value != _confirmPassword.value -> {
        "Las contraseñas no coinciden."
      }

      _emailAvailable.value == false -> {
        "Correo electrónico no disponible."
      }

      _usernameAvailable.value == false -> {
        "Usuario no disponible."
      }

      else -> {
        "Datos inválidos."
      }
    }
  }

  fun resetSuccess() {
    _onSuccess.update { false }
  }

  companion object {
    fun provideFactory() = viewModelFactory {
      initializer {
        val app = this[APPLICATION_KEY] as MemoriasApplication

        RegisterViewModel(
          authApiService = app.appProvider.authApiService
        )
      }
    }
  }
}