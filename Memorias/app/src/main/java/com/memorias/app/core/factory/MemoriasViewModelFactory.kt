package com.memorias.app.core.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.memorias.app.core.di.AppContainer
import com.memorias.app.presentation.screen.onthisday.OnThisDayViewModel

class MemoriasViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(OnThisDayViewModel::class.java) -> {
                OnThisDayViewModel(
                    getOnThisDayPhotos = container.getOnThisDayPhotosUseCase(),
                    loadNextPage = container.loadNextPhotoPageUseCase(),
                    addFavorite = container.addFavoriteUseCase(),
                    executeDeletion = container.executeDeletionUseCase(),
                    recordSession = container.recordSessionUseCase(),
                    observePreferences = container.observePreferencesUseCase()
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}