package com.example.pamelapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
  val email: String,
  val username: String,
  val password: String
)

@Serializable
data class LoginRequest(
  val email: String,
  val password: String
)

@Serializable
data class GoogleLoginRequest(
  val idToken: String
)

@Serializable
data class AuthResponse(
  val accessToken: String,
  val refreshToken: String? = null,
  val userId: String? = null,
  val email: String? = null,
  val displayName: String? = null
)

@Serializable
data class RegisterResponse(
  val userId: String? = null,
  val email: String? = null,
  val username: String? = null,
  val message: String? = null
)

@Serializable
data class AvailabilityResponse(
  val available: Boolean
)

@Serializable
data class UserResponse(
  val userId: String? = null,
  val email: String? = null,
  val username: String? = null
)

@Serializable
data class ApiErrorResponse(
  val title: String? = null,
  val detail: String? = null,
  val status: Int? = null,
  val message: String? = null,
  val error: String? = null
)