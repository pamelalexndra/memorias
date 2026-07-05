package com.memorias.app.domain.usecase

import com.memorias.app.domain.model.enums.AppTheme
import com.memorias.app.domain.model.enums.DeletionMode
import com.memorias.app.domain.model.enums.SessionSize
import com.memorias.app.domain.repository.PreferencesRepository

class UpdatePreferencesUseCase (
    private val preferencesRepository: PreferencesRepository,
) {
    suspend fun setDeletionMode(mode: DeletionMode) = preferencesRepository.setDeletionMode(mode)
    suspend fun setSessionSize(size: SessionSize) = preferencesRepository.setSessionSize(size)
    suspend fun setNotification(enabled: Boolean, hour: Int) = preferencesRepository.setNotification(enabled, hour)
    suspend fun setTheme(theme: AppTheme) = preferencesRepository.setTheme(theme)
    suspend fun setSwipeHintsVisible(visible: Boolean) = preferencesRepository.setSwipeHintsVisible(visible)
}