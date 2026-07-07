package com.example.pamelapp.ui.screens.loginscreen

import android.content.Context
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pamelapp.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {
  
  private val auth: FirebaseAuth by lazy {
    try {
      FirebaseAuth.getInstance()
    } catch (e: Exception) {
      Log.e("LoginViewModel", "Firebase no inicializado", e)
      throw IllegalStateException("FirebaseApp no inicializado. Asegúrate de llamar a FirebaseApp.initializeApp()")
    }
  }
  
  private val _isGoogleSignInLoading = MutableStateFlow(false)
  val isGoogleSignInLoading = _isGoogleSignInLoading.asStateFlow()
  
  private val _error = MutableStateFlow<String?>(null)
  val error = _error.asStateFlow()
  
  private val _onSuccess = MutableStateFlow(false)
  val onSuccess = _onSuccess.asStateFlow()
  
  private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
  val currentUser = _currentUser.asStateFlow()
  
  init {
    try {
      FirebaseApp.getInstance()
      Log.d("LoginViewModel", "Firebase inicializado correctamente")
    } catch (e: Exception) {
      Log.e("LoginViewModel", "Firebase NO inicializado", e)
    }
  }
  
  fun onGoogleSignInClick(context: Context) {
    viewModelScope.launch {
      _isGoogleSignInLoading.value = true
      _error.value = null
      
      try {
        val googleIdOption = GetGoogleIdOption.Builder()
          .setServerClientId(context.getString(R.string.default_web_client_id))
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
        
        handleCredentialResult(result.credential)
        
      } catch (e: GetCredentialException) {
        Log.e("LoginViewModel", "Error al obtener credencial de Google: ${e.message}", e)
        _error.value = "Error de configuración de Google: ${e.message}"
        _isGoogleSignInLoading.value = false
      } catch (e: Exception) {
        Log.e("LoginViewModel", "Error inesperado: ${e.message}", e)
        _error.value = "Error inesperado: ${e.message}"
        _isGoogleSignInLoading.value = false
      }
    }
  }
  
  private suspend fun handleCredentialResult(credential: Credential) {
    if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
      try {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
      } catch (e: Exception) {

        _error.value = "Error al procesar la credencial"
        _isGoogleSignInLoading.value = false
      }
    } else {
      _error.value = "Tipo de credencial no soportado"
      _isGoogleSignInLoading.value = false
    }
  }
  
  private suspend fun firebaseAuthWithGoogle(idToken: String) {
    try {
      val credential = GoogleAuthProvider.getCredential(idToken, null)
      val authResult = auth.signInWithCredential(credential).await()
      
      if (authResult.user != null) {
        _currentUser.value = authResult.user
        _onSuccess.value = true
      } else {
        _error.value = "Falló la autenticación con Firebase"
      }
    } catch (e: Exception) {
      _error.value = "Falló la autenticación"
    } finally {
      _isGoogleSignInLoading.value = false
    }
  }
  
  fun resetSuccess() {
    _onSuccess.value = false
  }
}