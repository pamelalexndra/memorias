package com.memorias.app.domain.usecase

import com.memorias.app.domain.model.PhotoFilter
import com.memorias.app.domain.model.PhotoPage
import com.memorias.app.domain.repository.PhotoRepository
import com.memorias.app.domain.result.DomainResult

class LoadNextPhotoPageUseCase (
    private val photoRepository: PhotoRepository,
) {
    suspend operator fun invoke(
        cursor: Long,
        filter: PhotoFilter = PhotoFilter.None,
        pageSize: Int = 100,
    ): DomainResult<PhotoPage> = photoRepository.getNextPage(cursor, filter, pageSize)
}