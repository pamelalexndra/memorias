package com.memorias.app.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class Routes : NavKey {
    @Serializable
    data object OnThisDay : Routes()

    @Serializable
    data object Favorites : Routes()

    @Serializable
    data object Settings : Routes()
}