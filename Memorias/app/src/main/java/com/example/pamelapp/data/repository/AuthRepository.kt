package com.example.pamelapp.data.repository

import com.example.pamelapp.data.preferences.SwipePreferences
import com.example.pamelapp.data.remote.AuthApiService
import com.example.pamelapp.data.remote.dto.RegisterResponse
import com.example.pamelapp.data.remote.dto.UserResponse

class AuthRepository(
  private val authApiService: AuthApiService,
  private val preferences: SwipePreferences
) {
  
  suspend fun login(
    email: String,
    password: String
  ): Result<Unit> {
    return authApiService.login(
      email = email.trim(),
      password = password
    ).map { response ->
      preferences.saveAuthSession(
        accessToken = response.accessToken,
        refreshToken = response.refreshToken,
        email = response.email,
        displayName = response.displayName
      )
    }
  }
  
  suspend fun loginWithGoogle(
    idToken: String
  ): Result<Unit> {
    return authApiService.loginWithGoogle(idToken)
      .map { response ->
        preferences.saveAuthSession(
          accessToken = response.accessToken,
          refreshToken = response.refreshToken,
          email = response.email,
          displayName = response.displayName
        )
      }
  }
  
  suspend fun register(
    email: String,
    username: String,
    password: String
  ): Result<RegisterResponse> {
    return authApiService.register(
      email = email.trim(),
      username = username.trim(),
      password = password
    )
  }
  
  suspend fun checkEmailAvailability(
    email: String
  ): Result<Boolean> {
    return authApiService.checkEmailAvailability(email.trim())
  }
  
  suspend fun checkUsernameAvailability(
    username: String
  ): Result<Boolean> {
    return authApiService.checkUsernameAvailability(username.trim())
  }
  
  suspend fun getCurrentUser(): Result<UserResponse> {
    val token = preferences.getAuthToken()
      ?: return Result.failure(Exception("No hay sesión activa."))
    
    return authApiService.getCurrentUser(token)
  }
  
  suspend fun validateSavedSession(): Boolean {
    val token = preferences.getAuthToken() ?: return false
    
    return authApiService.getCurrentUser(token)
      .onFailure {
        preferences.clearAuthSession()
      }
      .isSuccess
  }
  
  fun hasLocalSession(): Boolean {
    return preferences.isLoggedIn()
  }
  
  fun logout() {
    preferences.clearAuthSession()
  }
  
  suspend fun deleteAccount(): Result<Unit> {
    val token = preferences.getAuthToken()
      ?: return Result.failure(Exception("No hay sesión activa."))
    
    return authApiService.deleteAccount(token)
      .onSuccess {
        preferences.clearAuthSession()
      }
  }
  
}