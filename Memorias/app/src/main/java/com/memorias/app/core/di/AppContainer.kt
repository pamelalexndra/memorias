package com.memorias.app.core.di

import android.content.Context
import com.memorias.app.data.repository.AndroidPhotoRepository
import com.memorias.app.data.repository.FakeSyncRepository
import com.memorias.app.domain.repository.FavoriteRepository
import com.memorias.app.domain.repository.PhotoRepository
import com.memorias.app.domain.repository.PreferencesRepository
import com.memorias.app.domain.repository.SessionRepository
import com.memorias.app.domain.repository.SyncRepository
import com.memorias.app.domain.usecase.AddFavoriteUseCase
import com.memorias.app.domain.usecase.ExecuteDeletionUseCase
import com.memorias.app.domain.usecase.GetOnThisDayPhotosUseCase
import com.memorias.app.domain.usecase.LoadNextPhotoPageUseCase
import com.memorias.app.domain.usecase.ObserveFavoritesUseCase
import com.memorias.app.domain.usecase.ObservePreferencesUseCase
import com.memorias.app.domain.usecase.ObserveStatsUseCase
import com.memorias.app.domain.usecase.RecordSessionUseCase
import com.memorias.app.domain.usecase.RemoveFavoriteUseCase
import com.memorias.app.domain.usecase.SyncFavoritesUseCase
import com.memorias.app.domain.usecase.SyncSessionsUseCase
import com.memorias.app.domain.usecase.UpdatePreferencesUseCase

class AppContainer(context: Context) {

    private val appContext: Context = context.applicationContext
    private val syncRepository: SyncRepository by lazy {
        FakeSyncRepository()
    }

    val photoRepository: PhotoRepository by lazy {
        AndroidPhotoRepository(appContext)
    }

    // reemplazar con RoomFavoriteRepository(favoriteDao, appContext)
    val favoriteRepository: FavoriteRepository by lazy {
        throw NotImplementedError(
            "FavoriteRepository no implementado. "
        )
    }

    // reemplazar con DataStorePreferencesRepository(appContext)
    val preferencesRepository: PreferencesRepository by lazy {
        throw NotImplementedError("PreferencesRepository no implementado.")
    }

    // reemplazar con AuthRepositoryImpl(apiService, tokenManager)
  /*  val authRepository: AuthRepository by lazy {
        throw NotImplementedError("AuthRepository no implementado.")
    }

    // reemplazar con RemoteSyncRepository(apiService, ...)
    val syncRepository: SyncRepository by lazy {
        throw NotImplementedError("SyncRepository no implementado.")
    } */

    // reemplazar con RoomSessionRepository(sessionDao)
    val sessionRepository: SessionRepository by lazy {
        throw NotImplementedError("SessionRepository no implementado.")
    }

    fun getOnThisDayPhotosUseCase() =
        GetOnThisDayPhotosUseCase(photoRepository)

    fun loadNextPhotoPageUseCase() =
        LoadNextPhotoPageUseCase(photoRepository)

    fun addFavoriteUseCase() =
        AddFavoriteUseCase(favoriteRepository, photoRepository)

    fun removeFavoriteUseCase() =
        RemoveFavoriteUseCase(favoriteRepository, photoRepository)

    fun observeFavoritesUseCase() =
        ObserveFavoritesUseCase(favoriteRepository)

    fun executeDeletionUseCase() =
        ExecuteDeletionUseCase(photoRepository, preferencesRepository)

    fun recordSessionUseCase() =
        RecordSessionUseCase(sessionRepository)

    fun syncFavoritesUseCase() =
        SyncFavoritesUseCase(favoriteRepository, syncRepository)

    fun syncSessionsUseCase() =
        SyncSessionsUseCase(sessionRepository, syncRepository)

    fun observeStatsUseCase() =
        ObserveStatsUseCase(sessionRepository)

    fun observePreferencesUseCase() =
        ObservePreferencesUseCase(preferencesRepository)

    fun updatePreferencesUseCase() =
        UpdatePreferencesUseCase(preferencesRepository)

   // fun loginWithGoogleUseCase() =
     //   LoginWithGoogleUseCase(authRepository)
}
