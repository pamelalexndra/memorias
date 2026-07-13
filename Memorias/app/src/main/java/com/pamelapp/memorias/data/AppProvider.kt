package com.pamelapp.memorias.data

import android.content.Context
import com.pamelapp.memorias.data.database.AppDatabase
import com.pamelapp.memorias.data.preferences.SwipePreferences
import com.pamelapp.memorias.data.remote.authRepository.AuthRepository
import com.pamelapp.memorias.data.remote.authRepository.AuthRepositoryImpl
import com.pamelapp.memorias.data.session.SessionManager
import com.pamelapp.memorias.domain.repository.favoritePhotoRepository.FavoritePhotoRepository
import com.pamelapp.memorias.domain.repository.favoritePhotoRepository.FavoritePhotoRepositoryImpl

class AppProvider(
  context: Context
) {
  private val appContext = context.applicationContext
  
  private val appDatabase = AppDatabase.getDatabase(appContext)
  
  private val favoritePhotoDAO = appDatabase.favoritePhotoDAO()
  
  private val favoritePhotoRepository: FavoritePhotoRepository =
    FavoritePhotoRepositoryImpl(favoritePhotoDAO)
  
  private val swipePreferences = SwipePreferences(appContext)
  
  private val session = SessionManager(appContext)
  
  private val authRepository: AuthRepository = AuthRepositoryImpl(session)
  
  fun provideFavoritePhotoRepository(): FavoritePhotoRepository {
    return favoritePhotoRepository
  }
  
  fun provideSwipePreferences(): SwipePreferences {
    return swipePreferences
  }
  
  fun provideAuthRepository(): AuthRepository {
    return authRepository
  }
}