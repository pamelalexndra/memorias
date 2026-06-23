package com.memorias.app.domain.repository

interface SessionRepository {

    suspend fun save(session: ReviewSession): DomainResult<Unit>

    fun observeAll(): Flow<DomainResult<List<ReviewSession>>>
    suspend fun getStats(): DomainResult<SessionStats>

    suspend fun hadSessionToday(): Boolean
    suspend fun getCurrentStreak(): Int

    suspend fun getPendingUpload(): List<ReviewSession>
    suspend fun markSynced(sessionId: String)
}