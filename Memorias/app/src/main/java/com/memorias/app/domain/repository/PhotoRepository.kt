package com.memorias.app.domain.repository


import android.app.PendingIntent
import com.memorias.app.domain.model.PhotoFilter
import com.memorias.app.domain.model.PhotoPage
import com.memorias.app.domain.model.*
import com.memorias.app.domain.result.DomainResult

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