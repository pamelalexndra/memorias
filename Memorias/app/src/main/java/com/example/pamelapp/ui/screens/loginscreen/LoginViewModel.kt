package com.example.pamelapp.ui.screens.loginscreen

import android.content.Context
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pamelapp.BuildConfig
import com.example.pamelapp.MemoriasApplication
import com.example.pamelapp.R
import com.example.pamelapp.data.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
  private val authRepository: AuthRepository
) : ViewModel() {

  private val _isLoading = MutableStateFlow(false)
  val isLoading = _isLoading.asStateFlow()

  private val _isGoogleSignInLoading = MutableStateFlow(false)
  val isGoogleSignInLoading = _isGoogleSignInLoading.asStateFlow()

  private val _error = MutableStateFlow<String?>(null)
  val error = _error.asStateFlow()

  private val _onSuccess = MutableStateFlow(false)
  val onSuccess = _onSuccess.asStateFlow()

  fun onLoginClick(
    email: String,
    password: String
  ) {
    val cleanEmail = email.trim()

    if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
      _error.value = "Ingresa un correo válido."
      return
    }

    if (password.length < 6) {
      _error.value = "La contraseña debe tener al menos 6 caracteres."
      return
    }

    viewModelScope.launch {
      _isLoading.value = true
      _error.value = null

      authRepository.login(
        email = cleanEmail,
        password = password
      ).onSuccess {
        _onSuccess.value = true
      }.onFailure { exception ->
        _error.value = exception.message ?: "No se pudo iniciar sesión."
      }

      _isLoading.value = false
    }
  }

  fun onGoogleSignInClick(
    context: Context
  ) {
    viewModelScope.launch {
      _isGoogleSignInLoading.value = true
      _error.value = null

      try {
        val googleIdOption = GetGoogleIdOption.Builder()
          .setServerClientId(BuildConfig.TMDB_TOKEN)
          .setFilterByAuthorizedAccounts(false)
          .build()

        val request = GetCredentialRequest.Builder()
          .addCredentialOption(googleIdOption)
          .build()

        val credentialManager = CredentialManager.create(context)

        val result = credentialManager.getCredential(
          context = context,
          request = request
        )

        handleGoogleCredential(result.credential)
      } catch (exception: GetCredentialException) {
        _error.value = "No se pudo obtener la cuenta de Google."
      } catch (exception: Exception) {
        _error.value = exception.message ?: "Error inesperado con Google."
      } finally {
        _isGoogleSignInLoading.value = false
      }
    }
  }
  
  private suspend fun handleGoogleCredential(
    credential: Credential
  ) {
    if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
      val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
      val idToken = googleIdTokenCredential.idToken

      authRepository.loginWithGoogle(idToken)
        .onSuccess {
          _onSuccess.value = true
        }
        .onFailure { exception ->
          _error.value = exception.message ?: "No se pudo iniciar sesión con Google."
        }
    } else {
      _error.value = "Tipo de credencial no soportado."
    }
  }

  fun resetSuccess() {
    _onSuccess.value = false
  }

  companion object {
    fun provideFactory() = viewModelFactory {
      initializer {
        val app = this[APPLICATION_KEY] as MemoriasApplication

        LoginViewModel(
          authRepository = app.appProvider.provideAuthRepository()
        )
      }
    }
  }
}