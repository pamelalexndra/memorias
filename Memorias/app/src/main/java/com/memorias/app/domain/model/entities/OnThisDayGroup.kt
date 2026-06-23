package com.memorias.app.domain.model.entities

data class OnThisDayGroup(
    val year: Int,
    val yearsAgo: Int,
    val photos: List<Photo>,
    val coverPhotoId: Long = photos.first().id,
    ) {
    val photoCount: Int get() = photos.size
    val totalSizeBytes: Long get() = photos.sumOf { it.sizeBytes }
}