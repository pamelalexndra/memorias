package com.memorias.app.domain.repository

import com.memorias.app.domain.model.ReviewSession
import com.memorias.app.domain.model.SessionStats
import com.memorias.app.domain.result.DomainResult
import kotlinx.coroutines.flow.Flow

interface SessionRepository {

    suspend fun save(session: ReviewSession): DomainResult<Unit>

    fun observeAll(): Flow<DomainResult<List<ReviewSession>>>
    suspend fun getStats(): DomainResult<SessionStats>

    suspend fun hadSessionToday(): Boolean
    suspend fun getCurrentStreak(): Int

    suspend fun getPendingUpload(): List<ReviewSession>
    suspend fun markSynced(sessionId: String)
}