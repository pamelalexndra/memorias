package com.example.pamelapp.ui.screens.swipescreen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pamelapp.ui.scaffold.AppScaffold
import com.example.pamelapp.ui.screens.swipescreen.components.EmptyDayCard
import com.example.pamelapp.ui.screens.swipescreen.components.ErrorState
import com.example.pamelapp.ui.screens.swipescreen.components.FinishedState
import com.example.pamelapp.ui.screens.swipescreen.components.LoadingState
import com.example.pamelapp.ui.screens.swipescreen.components.PermissionDeniedState
import com.example.pamelapp.ui.screens.swipescreen.components.SwipeContent
import com.example.pamelapp.ui.screens.swipescreen.components.SwipeTopBarActions
import com.example.pamelapp.ui.theme.CreamWarm

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SwipeScreen(
  navigateToBack: () -> Unit,
  navigateToDelete: () -> Unit,
  navigateToFavorites: () -> Unit,
  navigateToConfiguration: () -> Unit,
  viewModel: SwipeViewModel = viewModel()
) {
  val state by viewModel.state.collectAsState()
  val context = LocalContext.current

  val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    Manifest.permission.READ_MEDIA_IMAGES
  } else {
    Manifest.permission.READ_EXTERNAL_STORAGE
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { granted ->
    if (granted) {
      viewModel.loadSettings()
      viewModel.loadPhotos()
      viewModel.loadFavorites()
    } else {
      viewModel.setPermissionDenied()
    }
  }

  LaunchedEffect(Unit) {
    val alreadyGranted = ContextCompat.checkSelfPermission(
      context,
      permission
    ) == PackageManager.PERMISSION_GRANTED

    if (alreadyGranted) {
      viewModel.loadSettings()
      viewModel.loadPhotos()
      viewModel.loadFavorites()
    } else {
      permissionLauncher.launch(permission)
    }
  }

  val total = state.photos.size
  val current = (state.currentIndex + 1).coerceAtMost(total)
  val photo = state.photos.getOrNull(state.currentIndex)

  AppScaffold(
    title = "Memorias",
    actions = {
      SwipeTopBarActions(
        favoritedCount = state.favorited,
        pendingDeleteCount = state.pendingDelete.size,
        totalPhotos = total,
        currentPosition = current,
        isLoading = state.loading,
        onFavoritesClick = navigateToFavorites,
        onSettingsClick = navigateToConfiguration,
        onDeleteQueueClick = navigateToDelete
      )
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(CreamWarm)
        .padding(paddingValues),
      contentAlignment = Alignment.Center
    ) {
      when {
        state.loading -> {
          LoadingState()
        }

        state.permissionDenied -> {
          PermissionDeniedState()
        }

        state.error != null -> {
          ErrorState(
            message = state.error ?: "Ocurrió un error",
            onRetry = {
              viewModel.loadPhotos()
              viewModel.loadFavorites()
            }
          )
        }

        state.photos.isEmpty() -> {
          EmptyDayCard()
        }

        state.currentIndex >= state.photos.size -> {
          FinishedState(
            deletedCount = state.deletedCount,
            freedBytes = state.freedBytes,
            pendingDeleteCount = state.pendingDelete.size,
            onReviewDelete = navigateToDelete,
            onRestart = { viewModel.restartReview() }
          )
        }

        photo != null -> {
          SwipeContent(
            photo = photo,
            showActionButtons = state.showSwipeButtons,
            onKeep = { viewModel.onKeep() },
            onFavorite = { viewModel.onFavorite() },
            onDelete = { viewModel.onDelete() },
            onUndoLastAction = { viewModel.undoLastSwipeAction() }
          )
        }
      }
    }
  }
}