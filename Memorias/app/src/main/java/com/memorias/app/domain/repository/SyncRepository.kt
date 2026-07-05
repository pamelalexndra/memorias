package com.memorias.app.domain.repository

import com.memorias.app.domain.model.Favorite
import com.memorias.app.domain.model.ReviewSession
import com.memorias.app.domain.model.SessionStats
import com.memorias.app.domain.result.DomainResult

interface SyncRepository {
    suspend fun syncFavorites(favorites: List<Favorite>): DomainResult<Unit>
    suspend fun fetchRemoteFavorites(): DomainResult<List<Favorite>>
    suspend fun syncSessions(sessions: List<ReviewSession>): DomainResult<Unit>
    suspend fun fetchRemoteStats(): DomainResult<SessionStats>
    suspend fun updateFcmToken(token: String): DomainResult<Unit>
}