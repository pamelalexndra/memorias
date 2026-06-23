package com.memorias.app.domain.repository


import android.app.PendingIntent
import com.memorias.app.domain.model.entities.PhotoFilter
import com.memorias.app.domain.model.entities.PhotoPage
import com.memorias.app.domain.model.*

interface PhotoRepository {

    suspend fun getOnThisDayPhotos(
        filter: PhotoFilter = PhotoFilter.None,
        pageSize: Int = 100,
    ): DomainResult<PhotoPage>

    suspend fun getNextPage(
        cursor: Long,
        filter: PhotoFilter = PhotoFilter.None,
        pageSize: Int = 100,
    ): DomainResult<PhotoPage>

    suspend fun getPhoto(id: Long): DomainResult<Photo>

    suspend fun computeHash(mediaStoreId: Long): DomainResult<String>

    suspend fun createDeleteRequest(mediaStoreIds: List<Long>): DomainResult<PendingIntent>

    suspend fun createTrashRequest(mediaStoreIds: List<Long>): DomainResult<PendingIntent>
}