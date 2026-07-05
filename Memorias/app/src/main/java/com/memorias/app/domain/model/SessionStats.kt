package com.memorias.app.domain.model

data class SessionStats(
    val totalSessions: Int = 0,
    val totalPhotosReviewed: Int = 0,
    val totalPhotosDeleted: Int = 0,
    val totalPhotosFavorited: Int = 0,
    val totalSpaceFreedBytes: Long = 0L,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val reviewedByYear: Map<Int, Int> = emptyMap()
)