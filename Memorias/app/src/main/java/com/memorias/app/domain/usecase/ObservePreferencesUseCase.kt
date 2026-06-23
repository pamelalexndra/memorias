package com.memorias.app.domain.usecase

import com.memorias.app.domain.model.entities.UserPreferences
import com.memorias.app.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow

class ObservePreferencesUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) {
    operator fun invoke(): Flow<UserPreferences> = preferencesRepository.observe()
}