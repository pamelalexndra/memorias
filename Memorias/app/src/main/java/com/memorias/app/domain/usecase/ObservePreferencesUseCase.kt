package com.memorias.app.domain.usecase

import com.memorias.app.domain.model.UserPreferences
import com.memorias.app.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow

class ObservePreferencesUseCase (
    private val preferencesRepository: PreferencesRepository,
) {
    operator fun invoke(): Flow<UserPreferences> = preferencesRepository.observe()
}