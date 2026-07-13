package com.pamelapp.memorias.data.remote.authRepository

import com.pamelapp.memorias.data.api.KtorClient
import com.pamelapp.memorias.data.api.email.EmailResponseDTO
import com.pamelapp.memorias.data.api.logIn.LogInRequestDTO
import com.pamelapp.memorias.data.api.logIn.LogInResponseDTO
import com.pamelapp.memorias.data.api.register.RegisterRequestDTO
import com.pamelapp.memorias.data.api.register.RegisterResponseDTO
import com.pamelapp.memorias.data.api.username.UsernameResponseDTO
import com.pamelapp.memorias.data.session.SessionManager
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(private val session: SessionManager) : AuthRepository {
  
  override val isLoggedIn: Flow<Boolean> = session.token.map { it != null }
  
  override val userName: Flow<String?> = session.userName
  
  override suspend fun signUp(email: String, username: String, password: String): Result<Boolean> {
    try {
      val request = RegisterRequestDTO(
        username = username,
        correo = email,
        password = password
      )
      
      val response: RegisterResponseDTO = KtorClient.client.post("auth/signUp") {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }.body()
      
      return Result.success(response.success)
    } catch (e: Exception) {
      return Result.failure(e)
    }
  }
  
  override suspend fun logIn(username: String, password: String): Result<Unit> {
    try {
      val request = LogInRequestDTO(
        username = username,
        password = password
      )
      
      val response: LogInResponseDTO = KtorClient.client.post("auth/logIn") {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }.body()
      
      if (!response.success || response.token == null || response.user == null) {
        return Result.failure(Exception(response.message))
      }
      
      session.save(response.token, response.user.username, response.user.id, response.user.correo)
      return Result.success(Unit)
    } catch (e: Exception) {
      return Result.failure(e)
    }
  }
  
  override suspend fun deleteAccount(): Result<Boolean> {
    try {
      val userId = session.userId.first()
        ?: return Result.failure(Exception("No hay sesión activa"))
      
      val token = session.token.first()
        ?: return Result.failure(Exception("No hay token de sesión"))
      
      val response = KtorClient.client.delete("auth/delete-account/$userId") {
        header(HttpHeaders.Authorization, "Bearer $token")
      }
      
      if (response.status.isSuccess()) {
        session.clear()
        return Result.success(true)
      } else {
        return Result.failure(Exception("No se pudo eliminar la cuenta (${response.status})"))
      }
    } catch (e: Exception) {
      return Result.failure(e)
    }
  }
  
  override suspend fun checkUsername(username: String): Result<Boolean> {
    try {
      val response: UsernameResponseDTO = KtorClient.client.get("auth/check-username/${username}") {
      
      }.body()
      
      return Result.success(response.available)
    } catch (e: Exception) {
      return Result.failure(e)
    }
  }
  
  override suspend fun checkEmail(email: String): Result<Boolean> {
    try {
      val response: EmailResponseDTO = KtorClient.client.get("auth/check-email/${email}") {
      
      }.body()
      
      return Result.success(response.available)
    } catch (e: Exception) {
      return Result.failure(e)
    }
  }
  
  override suspend fun logout() {
    session.clear()
  }
}