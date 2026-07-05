package com.memorias.app.domain.usecase

import com.memorias.app.domain.model.ReviewSession
import com.memorias.app.domain.model.SessionStats
import com.memorias.app.domain.repository.SessionRepository
import com.memorias.app.domain.result.DomainResult
import kotlinx.coroutines.flow.Flow

class ObserveStatsUseCase (
    private val sessionRepository: SessionRepository,
) {
    operator fun invoke(): Flow<DomainResult<List<ReviewSession>>> = sessionRepository.observeAll()
    suspend fun getStats(): DomainResult<SessionStats> = sessionRepository.getStats()
}