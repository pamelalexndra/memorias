package com.pamelapp.memorias.data.api.register

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDTO(
  val username: String,
  val correo: String,
  val password: String
)

@Serializable
data class RegisterResponseDTO(
  val success: Boolean,
  val message: String,
  val user: User? = null
)

@Serializable
data class User(
  @SerialName("id_usuario") val idUsuario: Int,
  val usuario: String,
  val correo: String
)