package com.example.pamelapp.domain.repository.favoritePhotoRepository

import com.example.pamelapp.data.database.dao.PhotoDAO
import com.example.pamelapp.data.database.entities.toFavoritePhotoEntity
import com.example.pamelapp.data.database.entities.toPhoto
import com.example.pamelapp.domain.model.Photo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavortiePhotoRepositoryImpl(private val photoDAO: PhotoDAO) : FavoritePhotoRepository {
  
  override fun getFavoritePhotos(): Flow<List<Photo>> {
    return photoDAO.getFavoritePhotos().map { list -> list.map { it.toPhoto() } }
  }
  
  override suspend fun addFavoritePhoto(favoritePhoto: Photo) {
    return photoDAO.insertPhoto(favoritePhoto.toFavoritePhotoEntity())
  }
  
  override suspend fun removeFavoritePhoto(favoritePhoto: Photo) {
    return photoDAO.deletePhoto(favoritePhoto.toFavoritePhotoEntity())
  }
  
  override suspend fun removeFavoritePhotos() {
    return photoDAO.clearFavoritePhotos()
  }
  
}