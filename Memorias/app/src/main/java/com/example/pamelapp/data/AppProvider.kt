package com.example.pamelapp.data

import android.content.Context
import com.example.pamelapp.data.database.AppDatabase
import com.example.pamelapp.domain.repository.favoritePhotoRepository.FavoritePhotoRepository
import com.example.pamelapp.domain.repository.favoritePhotoRepository.FavoritePhotoRepositoryImpl

class AppProvider(context: Context) {
  private val appDatabase = AppDatabase.getDatabase(context)
  
  private val favoritePhotoDAO = appDatabase.favoritePhotoDAO()
  
  private val favoritePhotoRepository: FavoritePhotoRepository =
    FavoritePhotoRepositoryImpl(favoritePhotoDAO)
  
  fun provideFavoritePhotoRepository(): FavoritePhotoRepository{
    return favoritePhotoRepository
  }
}