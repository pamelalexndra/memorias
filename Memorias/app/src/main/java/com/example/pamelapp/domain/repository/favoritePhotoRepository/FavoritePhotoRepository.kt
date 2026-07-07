package com.example.pamelapp.domain.repository.favoritePhotoRepository

import com.example.pamelapp.domain.model.Photo
import kotlinx.coroutines.flow.Flow

interface FavoritePhotoRepository {
  fun getFavoritePhotos(): Flow<List<Photo>>

  suspend fun getFavoritePhotosOnce(): List<Photo>

  suspend fun isFavoritePhoto(photoId: Long): Boolean

  suspend fun addFavoritePhoto(favoritePhoto: Photo)
  
  suspend fun removeFavoritePhoto(favoritePhoto: Photo)

  suspend fun removeFavoritePhotosByIds(photoIds: Set<Long>)
  
  suspend fun removeFavoritePhotos()
}