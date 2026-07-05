package com.memorias.app.domain.usecase

import com.memorias.app.domain.repository.FavoriteRepository
import com.memorias.app.domain.repository.SyncRepository
import com.memorias.app.domain.result.DomainResult

class SyncFavoritesUseCase (
    private val favoriteRepository: FavoriteRepository,
    private val syncRepository: SyncRepository,
) {
    suspend operator fun invoke(): DomainResult<Unit> {
        val pending = favoriteRepository.getPendingUpload()
        if (pending.isEmpty()) return DomainResult.Success(Unit)
        return syncRepository.syncFavorites(pending)
    }
}