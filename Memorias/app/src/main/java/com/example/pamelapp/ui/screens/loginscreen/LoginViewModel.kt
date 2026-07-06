package com.example.pamelapp.ui.screens.loginscreen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
  private val _isLoading = MutableStateFlow<Boolean>(false)
  val isLoading = _isLoading.asStateFlow()
  
  private val _username = MutableStateFlow<String>("")
  val username = _username.asStateFlow()
  
  private val _password = MutableStateFlow<String>("")
  val password = _password.asStateFlow()
  
  private val _error = MutableStateFlow<String?>(null)
  val error = _error.asStateFlow()
  
  private val _onSuccess = MutableStateFlow<Boolean>(false)
  val onSuccess = _onSuccess.asStateFlow()
  
  private val _navigateToRegister = MutableStateFlow<Boolean>(false)
  val navigateToRegister = _navigateToRegister.asStateFlow()
  
  fun updateUsername(value: String) {
    _username.update { value }
  }
  
  fun updatePassword(value: String) {
    _password.update { value }
  }
  
  fun onLoginClick() {
    viewModelScope.launch {
      _isLoading.update { true }
      _error.update { null }
      delay(1000)
      
      if (_username.value.isNotEmpty() && _password.value.isNotEmpty()) {
        _onSuccess.update { true }
      } else {
        _error.update { "Usuario o contraseña incorrectos" }
      }
      _isLoading.update { false }
    }
  }
  
  fun onGoogleSignInClick(context: Context) {
    viewModelScope.launch {
      _isLoading.update { true }
      delay(1000)
      _onSuccess.update { true }
      _isLoading.update { false }
    }
  }
  
  fun navigateToRegister() {
    _navigateToRegister.update { true }
  }
}