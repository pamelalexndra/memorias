package com.memorias.app.domain.repository

import com.memorias.app.domain.model.entities.Favorite
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {

    fun observeAll(): Flow<DomainResult<List<Favorite>>>
    fun observeByYear(year: Int): Flow<DomainResult<List<Favorite>>>
    fun observeAvailableYears(): Flow<DomainResult<List<Int>>>

    suspend fun getByMonthDay(month: Int, day: Int): DomainResult<List<Favorite>>
    suspend fun getNearestToToday(maxResults: Int = 6): DomainResult<List<Favorite>>

    suspend fun getById(localId: String): Favorite?
    suspend fun isFavorite(photoHash: String): Boolean
    suspend fun add(favorite: Favorite): DomainResult<Favorite>
    suspend fun remove(photoHash: String): DomainResult<Unit>

    suspend fun getPendingUpload(): List<Favorite>
    suspend fun markSynced(localId: String, serverId: String)
    suspend fun markPendingDelete(localId: String)
}