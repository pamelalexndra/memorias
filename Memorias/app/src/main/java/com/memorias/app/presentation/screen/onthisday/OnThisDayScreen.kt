package com.memorias.app.presentation.screen.onthisday

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.memorias.app.presentation.component.OnThisDayShimmer
import com.memorias.app.presentation.component.SessionSummaryDialog
import com.memorias.app.presentation.component.StatusIndicators.EmptyState
import com.memorias.app.ui.theme.CreamWarm
import com.memorias.app.presentation.component.ReviewingContent
import com.memorias.app.presentation.component.SessionPauseCard

@Composable
fun OnThisDayScreen(
    viewModel: OnThisDayViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.onDeleteConfirmed()
        } else {
            viewModel.onDeleteCancelled()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is OnThisDayUiEvent.LaunchDeleteIntent -> {
                    deleteLauncher.launch(
                        IntentSenderRequest.Builder(event.pendingIntent.intentSender).build(),
                    )
                }
                is OnThisDayUiEvent.ShowSnackbar -> Unit
                OnThisDayUiEvent.NavigateToDone -> Unit
            }
        }
    }

    Scaffold(containerColor = CreamWarm) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val status = state.status) {
                is OnThisDayUiState.Status.Loading -> OnThisDayShimmer()

                is OnThisDayUiState.Status.Empty -> EmptyState(
                    title = "No hay fotos de este dia",
                    subtitle = "Vuelve mañana, o revisa años anteriores en otra fecha.",
                )

                is OnThisDayUiState.Status.Error -> EmptyState(
                    title = "Algo salió mal",
                    subtitle = status.message,
                    actionLabel = "Reintentar",
                    onAction = { viewModel.loadPhotos() },
                )

                is OnThisDayUiState.Status.PausedAtLimit -> SessionPauseCard(
                    reviewed = state.sessionReviewed,
                    onContinue = viewModel::continueAfterPause,
                )

                is OnThisDayUiState.Status.Confirming -> DeleteConfirmScreen(
                    photos = state.deletionQueue.photos,
                    onRemove = viewModel::removeFromQueue,
                    onCancel = viewModel::dismissConfirmation,
                    onConfirm = viewModel::confirmDeletion,
                )

                is OnThisDayUiState.Status.Done -> SessionSummaryDialog(
                    photosReviewed = state.sessionReviewed,
                    photosDeleted = state.deletionQueue.size,
                    photosFavorited = state.sessionFavorited,
                    spaceFreedBytes = state.deletionQueue.totalBytes,
                    currentStreak = 0,
                    onDismiss = viewModel::startNewSession,
                    onStartNewSession = viewModel::startNewSession,
                )

                is OnThisDayUiState.Status.Reviewing -> ReviewingContent(
                    state = state,
                    onSwipe = viewModel::onSwipe,
                    onYearSelected = viewModel::selectYear,
                    onUndo = viewModel::undoLastAction,
                    onReviewDeleteQueue = viewModel::showDeleteConfirmation,
                )
            }
        }
    }
}

