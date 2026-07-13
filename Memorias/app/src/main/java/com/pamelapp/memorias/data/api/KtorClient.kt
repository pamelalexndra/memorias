package com.pamelapp.memorias.data.api

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object KtorClient {
  private const val BASE_URL = "https://pdm-backend-1auj.onrender.com/api/"
  
  val client = HttpClient(OkHttp) {
    // Parseo automático de JSON
    install(ContentNegotiation) {
      json(Json {
        ignoreUnknownKeys = true
      })
    }
    
    install(HttpTimeout) {
      requestTimeoutMillis = 60_000
      connectTimeoutMillis = 60_000
      socketTimeoutMillis = 60_000
    }
    
    // Plugin de logging
    install(Logging) {
      logger = object : Logger {
        override fun log(message: String) {
          Log.d("KtorClient", message)
        }
      }
      level = LogLevel.ALL
    }
    
    expectSuccess = false
    
    // Configuración aplicada a todas las peticiones
    defaultRequest {
      url(BASE_URL)
      header(HttpHeaders.Accept, "application/json")
    }
  }
}