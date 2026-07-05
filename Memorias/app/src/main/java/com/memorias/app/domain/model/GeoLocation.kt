package com.memorias.app.domain.model

data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
    val placeName: String? = null
)