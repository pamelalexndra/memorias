package com.memorias.app.data.repository

import com.memorias.app.domain.model.PhotoPage
import com.memorias.app.domain.repository.PhotoRepository
import com.memorias.app.domain.result.DomainResult
import com.memorias.app.domain.model.PhotoFilter

class FakePhotoRepository : PhotoRepository {
    override suspend fun getOnThisDayPhotos(filter: PhotoFilter, pageSize: Int) =
        DomainResult.Success(PhotoPage(emptyList(), 0, false))

    override suspend fun getNextPage(cursor: Long, filter: PhotoFilter, pageSize: Int) =
        DomainResult.Success(PhotoPage(emptyList(), 0, false))

    override suspend fun getPhoto(id: Long) =
        DomainResult.Error.NotFound("No implementado en Fake")

    override suspend fun computeHash(mediaStoreId: Long) =
        DomainResult.Success("fake_hash")

    override suspend fun createDeleteRequest(mediaStoreIds: List<Long>) =
        DomainResult.Error.Unknown(Exception("No soportado en Fake"))

    override suspend fun createTrashRequest(mediaStoreIds: List<Long>) =
        DomainResult.Error.Unknown(Exception("No soportado en Fake"))
}