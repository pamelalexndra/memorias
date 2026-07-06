package com.example.pamelapp.domain.repository

import com.example.pamelapp.domain.model.Photo

interface FavoritesRepository {
    suspend fun getFavoriteIds(): Set<Long>
    suspend fun addFavorite(photo: Photo)
    suspend fun removeFavorite(photoId: Long)
    suspend fun removeFavorites(photoIds: Set<Long>)
    suspend fun isFavorite(photoId: Long): Boolean
}