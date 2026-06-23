package com.memorias.app.presentation.screen.onthisday

sealed class OnThisDayUiEvent {
    data class LaunchDeleteIntent(val pendingIntent: PendingIntent) : OnThisDayUiEvent()
    data class ShowSnackbar(val message: String) : OnThisDayUiEvent()
    object NavigateToDone : OnThisDayUiEvent()
}