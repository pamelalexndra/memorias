package com.memorias.app.domain.usecase

import com.memorias.app.domain.model.entities.Favorite
import com.memorias.app.domain.model.entities.Photo
import com.memorias.app.domain.model.enums.SyncStatus
import com.memorias.app.domain.repository.FavoriteRepository
import com.memorias.app.domain.repository.PhotoRepository
import com.memorias.app.domain.result.DomainResult
import java.util.UUID

class AddFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val photoRepository: PhotoRepository,
) {
    suspend operator fun invoke(photo: Photo): DomainResult<Favorite> {
        val hash = photoRepository.computeHash(photo.id).getOrNull()
            ?: photo.stableFallbackHash   // fallback estable, nunca basado en Uri

        if (favoriteRepository.isFavorite(hash)) {
            return DomainResult.Error.Unknown(
                IllegalStateException("Esta foto ya es favorita"),
            )
        }

        val favorite = Favorite(
            localId = UUID.randomUUID().toString(),
            mediaStoreId = photo.id,
            photoHash = hash,
            dateTaken = photo.dateTaken,
            yearTaken = photo.year,
            location = photo.location,
            syncStatus = SyncStatus.PENDING_UPLOAD,
        )

        return favoriteRepository.add(favorite)
    }
}