package com.example.pamelapp.domain.repository.favoritePhotoRepository

import com.example.pamelapp.data.database.dao.PhotoDAO
import com.example.pamelapp.data.database.entities.toFavoritePhotoEntity
import com.example.pamelapp.data.database.entities.toPhoto
import com.example.pamelapp.domain.model.Photo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavortiePhotoRepositoryImpl(private val photoDAO: PhotoDAO) : FavoritePhotoRepository {

  override fun getFavoritePhotos(): Flow<List<Photo>> {
    return photoDAO.getFavoritePhotos().map { list -> list.map { entity -> entity.toPhoto() } }
  }

  override suspend fun getFavoritePhotosOnce(): List<Photo> {
    return photoDAO.getFavoritePhotosOnce().map { entity -> entity.toPhoto() }
  }

  override suspend fun isFavoritePhoto(photoId: Long): Boolean {
    return photoDAO.isFavoritePhoto(photoId)
  }

  override suspend fun addFavoritePhoto(favoritePhoto: Photo) {
    return photoDAO.insertPhoto(favoritePhoto.toFavoritePhotoEntity())
  }
  
  override suspend fun removeFavoritePhoto(favoritePhoto: Photo) {
    return photoDAO.deletePhoto(favoritePhoto.toFavoritePhotoEntity())
  }

  override suspend fun removeFavoritePhotosByIds(photoIds: Set<Long>) {
    if (photoIds.isEmpty()) return
    photoDAO.deletePhotosByIds(photoIds.toList())
  }

  override suspend fun removeFavoritePhotos() {
    return photoDAO.clearFavoritePhotos()
  }
  
}