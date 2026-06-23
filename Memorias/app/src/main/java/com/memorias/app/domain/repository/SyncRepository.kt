package com.memorias.app.domain.repository

import com.memorias.app.domain.model.entities.Favorite
import com.memorias.app.domain.model.entities.ReviewSession
import com.memorias.app.domain.model.entities.SessionStats

interface SyncRepository {
    suspend fun syncFavorites(favorites: List<Favorite>): DomainResult<Unit>
    suspend fun fetchRemoteFavorites(): DomainResult<List<Favorite>>
    suspend fun syncSessions(sessions: List<ReviewSession>): DomainResult<Unit>
    suspend fun fetchRemoteStats(): DomainResult<SessionStats>
    suspend fun updateFcmToken(token: String): DomainResult<Unit>
}