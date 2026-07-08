package com.example.pamelapp.data.remote

import android.util.Log
import com.example.pamelapp.BuildConfig
import com.example.pamelapp.data.remote.dto.ApiErrorResponse
import com.example.pamelapp.data.remote.dto.AuthResponse
import com.example.pamelapp.data.remote.dto.AvailabilityResponse
import com.example.pamelapp.data.remote.dto.GoogleLoginRequest
import com.example.pamelapp.data.remote.dto.LoginRequest
import com.example.pamelapp.data.remote.dto.RegisterRequest
import com.example.pamelapp.data.remote.dto.RegisterResponse
import com.example.pamelapp.data.remote.dto.UserResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class AuthApiService(
    private val baseUrl: String = BuildConfig.BACKEND_BASE_URL
) {
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val client = HttpClient(OkHttp) {
        expectSuccess = true

        install(ContentNegotiation) {
            json(jsonParser)
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

    suspend fun getCurrentUser(
        accessToken: String
    ): Result<UserResponse> = safeCall {
        client.get("$baseUrl/api/v1/users/me") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }.body()
    }
    private suspend fun <T> safeCall(
        block: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: ResponseException) {
            val status = exception.response.status

            val rawBody = try {
                exception.response.bodyAsText()
            } catch (_: Exception) {
                null
            }

            val serverMessage = extractServerMessage(rawBody)

            val message = when {
                rawBody?.contains("Application not found", ignoreCase = true) == true -> {
                    "No se encontró el backend en Railway. Revisa que BACKEND_BASE_URL tenga la URL correcta."
                }

                rawBody?.contains("Not Found", ignoreCase = true) == true &&
                        baseUrl.contains("tu-backend", ignoreCase = true) -> {
                    "La app todavía tiene la URL de ejemplo. Cambia BACKEND_BASE_URL por tu URL real de Railway."
                }

                status == HttpStatusCode.BadRequest -> {
                    serverMessage ?: "Revisa los datos ingresados."
                }

                status == HttpStatusCode.Unauthorized -> {
                    serverMessage ?: "Correo o contraseña incorrectos."
                }

                status == HttpStatusCode.Forbidden -> {
                    serverMessage ?: "No tienes permiso para realizar esta acción."
                }

                status == HttpStatusCode.Conflict -> {
                    serverMessage ?: "Ese correo o usuario ya está registrado."
                }

                status == HttpStatusCode.NotFound -> {
                    serverMessage ?: "No se encontró la ruta del servidor. Revisa la URL del backend."
                }

                status.value in 500..599 -> {
                    "El servidor tuvo un problema. Intenta más tarde."
                }

                else -> {
                    serverMessage ?: "Ocurrió un error inesperado. Código: ${status.value}."
                }
            }

            Result.failure(Exception(message))
        } catch (exception: java.net.UnknownHostException) {
            Result.failure(
                Exception("No se encontró el servidor. Revisa la URL del backend o tu conexión a internet.")
            )
        } catch (exception: java.net.ConnectException) {
            Result.failure(
                Exception("No se pudo conectar con el backend. Revisa que el servidor esté activo.")
            )
        } catch (exception: java.net.SocketTimeoutException) {
            Result.failure(
                Exception("El servidor tardó demasiado en responder. Intenta nuevamente.")
            )
        } catch (exception: Exception) {
            Result.failure(
                Exception("No se pudo conectar con el servidor. Revisa tu internet o intenta más tarde.")
            )
        }
    }

    private fun extractServerMessage(
        rawBody: String?
    ): String? {
        if (rawBody.isNullOrBlank()) return null

        return try {
            val apiError = jsonParser.decodeFromString<ApiErrorResponse>(rawBody)

            apiError.detail
                ?: apiError.message
                ?: apiError.error
                ?: apiError.title
        } catch (_: Exception) {
            when {
                rawBody.contains("Application not found", ignoreCase = true) -> {
                    "No se encontró la aplicación del backend."
                }

                rawBody.length <= 120 -> {
                    rawBody
                }

                else -> {
                    null
                }
            }
        }
    }
}