package com.memorias.app.domain.usecase

import com.memorias.app.domain.model.ReviewSession
import com.memorias.app.domain.model.enums.SyncStatus
import com.memorias.app.domain.repository.SessionRepository
import com.memorias.app.domain.result.DomainResult
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class RecordSessionUseCase (
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(
        photosReviewed: Int,
        photosDeleted: Int,
        photosFavorited: Int,
        photosKept: Int,
        spaceFreedBytes: Long,
        durationMs: Long,
    ): DomainResult<ReviewSession> {
        val session = ReviewSession(
            id = UUID.randomUUID().toString(),
            date = LocalDate.now(),
            photosReviewed = photosReviewed,
            photosDeleted = photosDeleted,
            photosFavorited = photosFavorited,
            photosKept = photosKept,
            spaceFreedBytes = spaceFreedBytes,
            durationMs = durationMs,
            completedAt = Instant.now(),
            syncStatus = SyncStatus.PENDING_UPLOAD,
        )

        return sessionRepository.save(session).map { session }
    }
}