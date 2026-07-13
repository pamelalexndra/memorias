package com.pamelapp.memorias.data.remote.authRepository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
  val isLoggedIn: Flow<Boolean>
  val userName: Flow<String?>
  suspend fun signUp(email: String, username: String, password: String): Result<Boolean>
  suspend fun logIn(username: String, password: String):Result<Unit>
  suspend fun deleteAccount(): Result<Boolean>
  suspend fun checkUsername(username:String): Result<Boolean>
  suspend fun checkEmail(email:String): Result<Boolean>
  suspend fun logout()
}
