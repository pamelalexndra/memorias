package com.memorias.app.domain.usecase

import com.memorias.app.domain.model.entities.PhotoFilter
import com.memorias.app.domain.model.entities.PhotoPage
import com.memorias.app.domain.repository.PhotoRepository
import com.memorias.app.domain.result.DomainResult

class LoadNextPhotoPageUseCase @Inject constructor(
    private val photoRepository: PhotoRepository,
) {
    suspend operator fun invoke(
        cursor: Long,
        filter: PhotoFilter = PhotoFilter.None,
        pageSize: Int = 100,
    ): DomainResult<PhotoPage> = photoRepository.getNextPage(cursor, filter, pageSize)
}