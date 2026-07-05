package com.memorias.app.widget

import com.memorias.app.domain.model.Favorite
sealed class WidgetState {
    object Loading : WidgetState()
    data class Loaded(
        val favorites: List<Favorite>,
        val isNearestDay: Boolean = false,
        val nearestDayDescription: String = "",
    ) : WidgetState()
}
