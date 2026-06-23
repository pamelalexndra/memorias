package com.memorias.app.domain.model.entities

import java.time.Instant
import java.time.LocalDate

data class ReviewSession(
    val id: String,
    val date: LocalDate,
    val photosReviewed: Int,
    val photosDeleted: Int,
    val photosFavorited: Int,
    val photosKept: Int,
    val spaceFreedBytes: Long,
    val durationMs: Long,
    val completedAt: Instant = Instant.now(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD
)