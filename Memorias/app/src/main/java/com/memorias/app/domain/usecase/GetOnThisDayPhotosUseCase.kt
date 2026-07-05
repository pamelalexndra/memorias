package com.memorias.app.domain.usecase

import com.memorias.app.domain.model.PhotoFilter
import com.memorias.app.domain.model.PhotoPage
import com.memorias.app.domain.repository.PhotoRepository
import com.memorias.app.domain.result.DomainResult


class GetOnThisDayPhotosUseCase (
    private val photoRepository: PhotoRepository,
) {
    suspend operator fun invoke(
        filter: PhotoFilter = PhotoFilter.None,
        pageSize: Int = 100,
    ): DomainResult<PhotoPage> =
        photoRepository.getOnThisDayPhotos(filter, pageSize).map { page ->
            page.copy(
                groups = page.groups
                    .filter { it.photos.isNotEmpty() }
                    .sortedByDescending { it.year },
            )
        }
}