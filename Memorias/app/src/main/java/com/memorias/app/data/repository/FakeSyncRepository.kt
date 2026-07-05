package com.memorias.app.data.repository

import com.memorias.app.domain.model.Favorite
import com.memorias.app.domain.model.ReviewSession
import com.memorias.app.domain.model.SessionStats
import com.memorias.app.domain.repository.SyncRepository
import com.memorias.app.domain.result.DomainResult

class FakeSyncRepository : SyncRepository {
    override suspend fun syncFavorites(favorites: List<Favorite>): DomainResult<Unit> = DomainResult.Success(Unit)
    override suspend fun fetchRemoteFavorites(): DomainResult<List<Favorite>> = DomainResult.Success(emptyList())
    override suspend fun syncSessions(sessions: List<ReviewSession>): DomainResult<Unit> = DomainResult.Success(Unit)
    override suspend fun fetchRemoteStats(): DomainResult<SessionStats> = DomainResult.Success(SessionStats())
    override suspend fun updateFcmToken(token: String): DomainResult<Unit> = DomainResult.Success(Unit)
}