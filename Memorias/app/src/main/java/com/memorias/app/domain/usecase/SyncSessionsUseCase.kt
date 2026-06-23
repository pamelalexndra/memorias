package com.memorias.app.domain.usecase

import com.memorias.app.domain.repository.SessionRepository
import com.memorias.app.domain.repository.SyncRepository
import com.memorias.app.domain.result.DomainResult

class SyncSessionsUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val syncRepository: SyncRepository,
) {
    suspend operator fun invoke(): DomainResult<Unit> {
        val pending = sessionRepository.getPendingUpload()
        if (pending.isEmpty()) return DomainResult.Success(Unit)

        return syncRepository.syncSessions(pending).onSuccess {
            pending.forEach { sessionRepository.markSynced(it.id) }
        }
    }
}