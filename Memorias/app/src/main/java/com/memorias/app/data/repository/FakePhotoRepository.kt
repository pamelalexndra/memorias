package com.memorias.app.data.repository

import com.memorias.app.domain.model.PhotoPage
import com.memorias.app.domain.repository.PhotoRepository
import com.memorias.app.domain.result.DomainResult
import com.memorias.app.domain.model.PhotoFilter

class FakePhotoRepository : PhotoRepository {
    override suspend fun getOnThisDayPhotos(filter: PhotoFilter, pageSize: Int): DomainResult<PhotoPage> {
        return DomainResult.Success(PhotoPage(groups = emptyList(), totalCount = 0, hasMore = false))
    }

}