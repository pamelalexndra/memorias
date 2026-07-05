package com.memorias.app.presentation.screen.onthisday

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.memorias.MemoriasApplication
import com.memorias.app.domain.model.enums.SwipeAction
import com.memorias.core.factory.MemoriasViewModelFactory
import com.memorias.domain.model.SwipeAction
import com.memorias.presentation.component.DeletionQueueBar
import com.memorias.presentation.component.EmptyState
import com.memorias.presentation.component.OnThisDayShimmer
import com.memorias.presentation.component.SessionSummaryDialog
import com.memorias.presentation.component.SwipeablePhotoCard
import com.memorias.presentation.component.YearTimeline
import com.memorias.presentation.extension.yearsAgoLabel
import com.memorias.presentation.screen.deleteconfirm.DeleteConfirmScreen
import com.memorias.presentation.theme.BrownLight
import com.memorias.presentation.theme.BrownMid
import com.memorias.presentation.theme.CreamWarm
import com.memorias.presentation.theme.Siena
import com.memorias.presentation.theme.SwipeFav

@Composable
fun OnThisDayScreen(
    viewModel: OnThisDayViewModel = run {
        val context = LocalContext.current
        val container = (context.applicationContext as MemoriasApplication).container
        viewModel(factory = MemoriasViewModelFactory(container))
    },
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
                    subtitle = "Vuelve otro dia, o revisa anios anteriores en otra fecha.",
                )

                is OnThisDayUiState.Status.Error -> EmptyState(
                    title = "Algo salio mal",
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

@Composable
private fun ReviewingContent(
    state: OnThisDayUiState,
    onSwipe: (SwipeAction) -> Unit,
    onYearSelected: (Int) -> Unit,
    onUndo: () -> Unit,
    onReviewDeleteQueue: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {

        Surface(color = Color.White, shadowElevation = 1.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Memorias", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Siena)
                Spacer(Modifier.weight(1f))

                if (state.sessionFavorited > 0) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = SwipeFav, modifier = Modifier.height(16.dp))
                    Text(" ${state.sessionFavorited}", color = SwipeFav, fontSize = 13.sp)
                    Spacer(Modifier.width(12.dp))
                }

                IconButton(onClick = onUndo, enabled = state.canUndo) {
                    Icon(Icons.Default.Undo, contentDescription = "Deshacer")
                }

                Text(
                    "${state.currentPhotoIndex + 1}/${state.totalPhotosInGroup}",
                    color = BrownMid,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }

        YearTimeline(
            groups = state.groups,
            selectedIndex = state.selectedYearIndex,
            onYearSelected = onYearSelected,
            modifier = Modifier.padding(vertical = 10.dp),
        )

        state.currentPhoto?.let { photo ->
            Text(
                text = photo.yearsAgo.yearsAgoLabel() + " - ${photo.year}",
                color = BrownMid,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 20.dp, bottom = 4.dp),
            )
        }

        Box(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            state.currentPhotoUri()?.let { uri ->
                key(state.currentPhotoIndex, state.selectedYearIndex) {
                    SwipeablePhotoCard(
                        imageModel = uri,
                        badgeLabel = "${state.currentPhoto?.year}",
                        onSwiped = onSwipe,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        Text(
            text = "izquierda conservar  -  arriba favorita  -  derecha borrar",
            color = BrownLight,
            fontSize = 11.sp,
            modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        DeletionQueueBar(
            photoCount = state.deletionQueue.size,
            totalBytes = state.deletionQueue.totalBytes,
            onReviewClick = onReviewDeleteQueue,
        )
    }
}

@Composable
private fun SessionPauseCard(reviewed: Int, onContinue: () -> Unit) {
    EmptyState(
        title = "Llevas $reviewed fotos revisadas!",
        subtitle = "Quieres seguir revisando mas recuerdos de este dia?",
        actionLabel = "Seguir revisando",
        onAction = onContinue,
    )
}