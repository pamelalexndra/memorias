package com.memorias.app.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.memorias.app.presentation.screen.onthisday.OnThisDayScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Routes.OnThisDay)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {

            entry<Routes.OnThisDay> {
                OnThisDayScreen()
            }

            entry<Routes.Favorites> {
                // FavoritesScreen(onBack = { backStack.removeLastOrNull() })
            }

            // Pantalla de Ajustes
            entry<Routes.Settings> {
                // SettingsScreen(onBack = { backStack.removeLastOrNull() })
            }
        },
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(400)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(400)
            )
        },
        popTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(400)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(400)
            )
        }
    )
}