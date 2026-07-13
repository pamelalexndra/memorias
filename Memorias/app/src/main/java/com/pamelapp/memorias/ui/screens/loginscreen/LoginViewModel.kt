package com.pamelapp.memorias.ui.screens.loginscreen

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pamelapp.memorias.MemoriasApplication
import com.pamelapp.memorias.R
import com.pamelapp.memorias.data.remote.authRepository.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.combine

class LoginViewModel(
  private val authRepository: AuthRepository
) : ViewModel() {
  
  private val auth: FirebaseAuth by lazy {
    try {
      FirebaseAuth.getInstance()
    } catch (e: Exception) {
      Log.e("LoginViewModel", "Firebase no inicializado", e)
      throw IllegalStateException("FirebaseApp no inicializado. Asegúrate de llamar a FirebaseApp.initializeApp()")
    }
  }
  
  private val _isGoogleSignedIn = MutableStateFlow(
    auth.currentUser?.providerData?.any { it.providerId == GoogleAuthProvider.PROVIDER_ID } == true
  )
  val isGoogleSignedIn = _isGoogleSignedIn.asStateFlow()
  
  private val _isSigningOut = MutableStateFlow(false)
  val isSigningOut = _isSigningOut.asStateFlow()
  
  
  private val _signOutCompleted = MutableStateFlow(false)
  val signOutCompleted = _signOutCompleted.asStateFlow()
  
  private val _isLoading = MutableStateFlow(false)
  val isLoading = _isLoading.asStateFlow()
  
  private val _isGoogleSignInLoading = MutableStateFlow(false)
  val isGoogleSignInLoading = _isGoogleSignInLoading.asStateFlow()
  
  private val _error = MutableStateFlow<String?>(null)
  val error = _error.asStateFlow()
  
  private val _onSuccess = MutableStateFlow(false)
  val onSuccess = _onSuccess.asStateFlow()
  
  private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
  val currentUser = _currentUser.asStateFlow()
  
  val isLoggedIn: StateFlow<Boolean?> = combine(
    authRepository.isLoggedIn,
    isGoogleSignedIn
  ) { backendLoggedIn, googleSignedIn ->
    backendLoggedIn || googleSignedIn
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
  
  val userName: StateFlow<String?> = authRepository.userName
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
  
  init {
    try {
      FirebaseApp.getInstance()
      Log.d("LoginViewModel", "Firebase inicializado correctamente")
    } catch (e: Exception) {
      Log.e("LoginViewModel", "Firebase NO inicializado", e)
    }
  }
  
  fun onLoginClick(
    username: String,
    password: String
  ) {
    val cleanUsername = username.trim()
    
    if (cleanUsername.isBlank()) {
      _error.value = "Ingresa tu usuario"
      return
    }
    
    if (password.length < 6) {
      _error.value = "La contraseña debe tener al menos 6 caracteres."
      return
    }
    
    viewModelScope.launch {
      _isLoading.value = true
      _error.value = null
      
      authRepository.logIn(
        username = cleanUsername,
        password = password
      ).onSuccess {
        _onSuccess.value = true
      }.onFailure { exception ->
        _error.value = exception.message ?: "No se pudo iniciar sesión."
      }
      
      _isLoading.value = false
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
        _isGoogleSignedIn.value = true
        _onSuccess.value = true
      } else {
        _error.value = "Falló la autenticación con Firebase"
      }
    } catch (e: Exception) {
      _error.value = "Falló la autenticación"
      Log.e("GoogleAuth", "Error en signInWithCredential", e)
      _error.value = when (e) {
        is FirebaseAuthInvalidCredentialsException -> "Credencial inválida: ${e.message}"
        is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese email: ${e.message}"
        is FirebaseNetworkException -> "Error de red: ${e.message}"
        else -> "Falló la autenticación: ${e.javaClass.simpleName} - ${e.message}"
      }
    } finally {
      _isGoogleSignInLoading.value = false
    }
  }
  
  fun resetSuccess() {
    _onSuccess.value = false
  }
  
  fun signOut(context: Context) {
    // Firebase sign out
    auth.signOut()
    
    viewModelScope.launch {
      _isSigningOut.value = true
      try {
        val credentialManager = CredentialManager.create(context)
        val clearRequest = ClearCredentialStateRequest()
        credentialManager.clearCredentialState(clearRequest)
      } catch (e: ClearCredentialException) {
        Log.e("LoginViewModel", "Couldn't clear user credentials: ${e.localizedMessage}")
      } finally {
        _currentUser.value = null
        _isGoogleSignedIn.value = false
        _onSuccess.value = false
        _isSigningOut.value = false
        _signOutCompleted.value = true
      }
    }
  }
  
  fun resetSignOutState() {
    _signOutCompleted.value = false
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