package com.memorias.app.domain.model.entities

data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
    val placeName: String? = null
)