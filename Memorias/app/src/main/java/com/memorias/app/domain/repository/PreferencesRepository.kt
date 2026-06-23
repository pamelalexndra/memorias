package com.memorias.app.domain.repository

import com.memorias.app.domain.model.entities.UserPreferences
import com.memorias.app.domain.model.enums.AppTheme
import com.memorias.app.domain.model.enums.DeletionMode
import com.memorias.app.domain.model.enums.SessionSize
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun observe(): Flow<UserPreferences>
    suspend fun get(): UserPreferences
    suspend fun setDeletionMode(mode: DeletionMode)
    suspend fun setSessionSize(size: SessionSize)
    suspend fun setNotification(enabled: Boolean, hour: Int)
    suspend fun setTheme(theme: AppTheme)
    suspend fun setSwipeHintsVisible(visible: Boolean)
    suspend fun setOnboardingSeen()
}