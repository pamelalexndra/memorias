package com.pamelapp.memorias.data.api.email

import kotlinx.serialization.Serializable

@Serializable
data class EmailResponseDTO(
  val available: Boolean
)