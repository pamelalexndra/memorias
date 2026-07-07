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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pamelapp.ui.scaffold.AppScaffold
import com.example.pamelapp.ui.screens.favoritesscreen.FavoritePhotoViewModel
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
  swipeViewModel: SwipeViewModel = viewModel(),
  favoriteViewModel: FavoritePhotoViewModel = viewModel(
    factory = FavoritePhotoViewModel.provideFactory()
  )
) {
  val favoritePhotos by favoriteViewModel.favoritePhotos.collectAsStateWithLifecycle()
  val favoritePhotosIds by favoriteViewModel.favoritePhotosIds.collectAsStateWithLifecycle()
  val state by swipeViewModel.state.collectAsState()
  val context = LocalContext.current

  LaunchedEffect(favoritePhotosIds) {
    swipeViewModel.syncFavoriteIds(favoritePhotosIds)
  }

  val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    Manifest.permission.READ_MEDIA_IMAGES
  } else {
    Manifest.permission.READ_EXTERNAL_STORAGE
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { granted ->
    if (granted) {
      swipeViewModel.loadSettings()
      swipeViewModel.loadPhotos()
      swipeViewModel.loadFavorites()
    } else {
      swipeViewModel.setPermissionDenied()
    }
  }

  LaunchedEffect(Unit) {
    val alreadyGranted = ContextCompat.checkSelfPermission(
      context,
      permission
    ) == PackageManager.PERMISSION_GRANTED

    if (alreadyGranted) {
      swipeViewModel.loadSettings()
      swipeViewModel.loadPhotos()
      swipeViewModel.loadFavorites()
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
        favoritedCount = favoritePhotos.size,
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
              swipeViewModel.loadPhotos()
              swipeViewModel.loadFavorites()
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
            onRestart = {
              swipeViewModel.restartReview()
            }
          )
        }

        photo != null -> {
          val isFavorite = photo.id in favoritePhotosIds

          SwipeContent(
            photo = photo,
            isFavorite = isFavorite,
            showActionButtons = state.showSwipeButtons,
            onKeep = {
              swipeViewModel.onKeep()
            },
            onFavorite = {
              swipeViewModel.onFavorite()
            },
            onDelete = {
              swipeViewModel.onDelete()
            },
            onUndoLastAction = {
              swipeViewModel.undoLastSwipeAction()
            }
          )
        }
      }
    }
  }
}