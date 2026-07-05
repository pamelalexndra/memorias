package com.memorias.app.presentation.navigation

import kotlinx.serialization.Serializable

sealed class Route {
    @Serializable
    data object OnThisDay : Route()

    @Serializable
    data object Favorites : Route()

    @Serializable
    data object Settings : Route()

    // @Serializable
    // data object Login : Route()
}