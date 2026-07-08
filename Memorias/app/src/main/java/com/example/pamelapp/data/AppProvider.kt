package com.example.pamelapp.data

import android.content.Context
import com.example.pamelapp.data.database.AppDatabase
import com.example.pamelapp.data.preferences.SwipePreferences
import com.example.pamelapp.data.remote.AuthApiService
import com.example.pamelapp.domain.repository.favoritePhotoRepository.FavoritePhotoRepository
import com.example.pamelapp.domain.repository.favoritePhotoRepository.FavoritePhotoRepositoryImpl

class AppProvider(context: Context) {

  private val appContext = context.applicationContext
  private val appDatabase = AppDatabase.getDatabase(context)

  private val favoritePhotoDAO = appDatabase.favoritePhotoDAO()

  private val favoritePhotoRepository: FavoritePhotoRepository =
    FavoritePhotoRepositoryImpl(favoritePhotoDAO)

  private val swipePreferences = SwipePreferences(appContext)
  val authApiService: AuthApiService = AuthApiService()

  fun provideFavoritePhotoRepository(): FavoritePhotoRepository {
    return favoritePhotoRepository
  }

  fun provideSwipePreferences(): SwipePreferences {
    return swipePreferences
  }

}