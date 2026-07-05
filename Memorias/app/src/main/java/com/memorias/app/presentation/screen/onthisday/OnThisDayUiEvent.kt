package com.memorias.app.presentation.screen.onthisday

import android.app.PendingIntent

sealed class OnThisDayUiEvent {
    data class LaunchDeleteIntent(val pendingIntent: PendingIntent) : OnThisDayUiEvent()
    data class ShowSnackbar(val message: String) : OnThisDayUiEvent()
    object NavigateToDone : OnThisDayUiEvent()
}