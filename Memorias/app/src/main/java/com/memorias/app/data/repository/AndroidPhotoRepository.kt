package com.memorias.app.data.repository

import android.content.ContentResolver
import android.content.Context
import com.memorias.app.domain.model.*
import com.memorias.app.domain.repository.PhotoRepository
import com.memorias.app.domain.result.DomainResult
import com.memorias.app.domain.result.runDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidPhotoRepository(
    private val context: Context,
) : PhotoRepository {

    private val resolver: ContentResolver = context.contentResolver

    override suspend fun getOnThisDayPhotos(filter: PhotoFilter, pageSize: Int): DomainResult<PhotoPage> =
        withContext(Dispatchers.IO) {
            runDomain {
                PhotoPage(groups = emptyList(), totalCount = 0, hasMore = false)
            }
        }

    override suspend fun getNextPage(cursor: Long, filter: PhotoFilter, pageSize: Int): DomainResult<PhotoPage> =
        withContext(Dispatchers.IO) {
            runDomain {
                PhotoPage(groups = emptyList(), totalCount = 0, hasMore = false)
            }
        }

    override suspend fun getPhoto(id: Long): DomainResult<Photo> = TODO("Implementar")
    override suspend fun computeHash(mediaStoreId: Long): DomainResult<String> = TODO("Implementar")
    override suspend fun createDeleteRequest(mediaStoreIds: List<Long>) = TODO("Implementar")
    override suspend fun createTrashRequest(mediaStoreIds: List<Long>) = TODO("Implementar")
}