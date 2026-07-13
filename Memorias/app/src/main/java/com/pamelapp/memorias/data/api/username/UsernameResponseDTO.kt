package com.pamelapp.memorias.data.api.username

import kotlinx.serialization.Serializable

@Serializable
data class UsernameResponseDTO(
  val available: Boolean
)
