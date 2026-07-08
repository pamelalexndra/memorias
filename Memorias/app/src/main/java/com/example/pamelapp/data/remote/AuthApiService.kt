package com.example.pamelapp.data.remote

import android.util.Log
import com.example.pamelapp.data.remote.dto.AuthResponse
import com.example.pamelapp.data.remote.dto.AvailabilityResponse
import com.example.pamelapp.data.remote.dto.GoogleLoginRequest
import com.example.pamelapp.data.remote.dto.LoginRequest
import com.example.pamelapp.data.remote.dto.RegisterRequest
import com.example.pamelapp.data.remote.dto.RegisterResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class AuthApiService(
    private val baseUrl: String = BASE_URL
) {
    private val client = HttpClient(OkHttp) {
        expectSuccess = true

        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                }
            )
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("KtorAuth", message)
                }
            }
            level = LogLevel.BODY
        }
    }

    suspend fun register(
        email: String,
        username: String,
        password: String
    ): Result<RegisterResponse> = safeCall {
        client.post("$baseUrl/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                RegisterRequest(
                    email = email,
                    username = username,
                    password = password
                )
            )
        }.body()
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<AuthResponse> = safeCall {
        client.post("$baseUrl/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(
                LoginRequest(
                    email = email,
                    password = password
                )
            )
        }.body()
    }

    suspend fun loginWithGoogle(
        idToken: String
    ): Result<AuthResponse> = safeCall {
        client.post("$baseUrl/api/v1/auth/google") {
            contentType(ContentType.Application.Json)
            setBody(
                GoogleLoginRequest(
                    idToken = idToken
                )
            )
        }.body()
    }

    suspend fun checkEmailAvailability(
        email: String
    ): Result<Boolean> = safeCall {
        client.get("$baseUrl/api/v1/auth/check-email") {
            parameter("email", email)
        }.body<AvailabilityResponse>().available
    }

    suspend fun checkUsernameAvailability(
        username: String
    ): Result<Boolean> = safeCall {
        client.get("$baseUrl/api/v1/auth/check-username") {
            parameter("username", username)
        }.body<AvailabilityResponse>().available
    }

    private suspend fun <T> safeCall(
        block: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: ClientRequestException) {
            val body = try {
                exception.response.bodyAsText()
            } catch (_: Exception) {
                null
            }

            val message = when (exception.response.status) {
                HttpStatusCode.Unauthorized -> "Credenciales incorrectas."
                HttpStatusCode.Conflict -> "El correo o usuario ya existe."
                HttpStatusCode.BadRequest -> "Datos inválidos."
                HttpStatusCode.NotFound -> "Servicio de autenticación no encontrado."
                else -> body ?: "Error ${exception.response.status.value}."
            }

            Result.failure(Exception(message))
        } catch (exception: ServerResponseException) {
            Result.failure(Exception("Error del servidor. Intenta más tarde."))
        } catch (exception: Exception) {
            Result.failure(Exception("No se pudo conectar con el servidor."))
        }
    }

    companion object {
        const val BASE_URL = "https://memoriasbackend-production.up.railway.app"
    }
}