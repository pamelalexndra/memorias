package com.memorias.app.domain.model.entities

sealed class PhotoFilter {
    object None : PhotoFilter()
    object BlurryOnly : PhotoFilter()
    object NoFaces : PhotoFilter()
    object Screenshots : PhotoFilter()
    data class ByYear(val year: Int) : PhotoFilter()
    data class ByLocation(
        val latitude: Double,
        val longitude: Double,
        val radiusKm: Double = 5.0,
        ) : PhotoFilter()
}