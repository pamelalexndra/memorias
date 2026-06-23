package com.memorias.app.domain.usecase

import com.memorias.app.domain.repository.FavoriteRepository
import com.memorias.app.domain.result.DomainResult
import kotlinx.coroutines.flow.Flow

class ObserveFavoritesUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
) {
    operator fun invoke(): Flow<DomainResult<List<Favorite>>> = favoriteRepository.observeAll()
    operator fun invoke(year: Int): Flow<DomainResult<List<Favorite>>> = favoriteRepository.observeByYear(year)
    fun observeAvailableYears(): Flow<DomainResult<List<Int>>> = favoriteRepository.observeAvailableYears()
}