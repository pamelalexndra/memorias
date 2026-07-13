package com.pamelapp.memorias.ui.screens.welcomescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WelcomeViewModel : ViewModel() {
  private val _state = MutableStateFlow(WelcomeState.Idle)
  val state: StateFlow<WelcomeState> = _state.asStateFlow()
  
  fun onGetStarted() {
    viewModelScope.launch {
      _state.value = WelcomeState.NavigateToLogin
    }
  }
}