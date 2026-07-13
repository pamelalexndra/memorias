package com.pamelapp.memorias.data.api.logIn

import kotlinx.serialization.Serializable

@Serializable
data class LogInRequestDTO(
  val username: String,
  val password: String
)

@Serializable
data class LogInResponseDTO(
  val success: Boolean,
  val message: String,
  val token: String? = null,
  val user: LogInUser? = null
)

@Serializable
data class LogInUser(
  val id: Int,
  val username: String,
  val correo: String
)