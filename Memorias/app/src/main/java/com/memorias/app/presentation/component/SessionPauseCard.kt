package com.memorias.app.presentation.component

import androidx.compose.runtime.Composable
import com.memorias.app.presentation.component.statusindicatos.EmptyState

@Composable
fun SessionPauseCard(reviewed: Int, onContinue: () -> Unit) {
    EmptyState(
        title = "Llevas $reviewed fotos revisadas!",
        subtitle = "Quieres seguir revisando mas recuerdos de este dia?",
        actionLabel = "Seguir revisando",
        onAction = onContinue,
    )
}