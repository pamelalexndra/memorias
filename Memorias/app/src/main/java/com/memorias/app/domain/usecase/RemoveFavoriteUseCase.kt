package com.memorias.app.domain.usecase

import com.memorias.app.domain.model.Favorite
import com.memorias.app.domain.model.Photo
import com.memorias.app.domain.repository.FavoriteRepository
import com.memorias.app.domain.repository.PhotoRepository
import com.memorias.app.domain.result.DomainResult

class RemoveFavoriteUseCase (
    private val favoriteRepository: FavoriteRepository,
    private val photoRepository: PhotoRepository,
) {
    suspend operator fun invoke(photo: Photo): DomainResult<Unit> {
        val hash = photoRepository.computeHash(photo.id).getOrNull()
            ?: photo.stableFallbackHash
        return favoriteRepository.remove(hash)
    }

    suspend operator fun invoke(favorite: Favorite): DomainResult<Unit> =
        favoriteRepository.remove(favorite.photoHash)
}