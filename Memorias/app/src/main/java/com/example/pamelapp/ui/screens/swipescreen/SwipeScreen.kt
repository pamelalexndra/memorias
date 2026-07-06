package com.example.pamelapp.ui.screens.swipescreen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.pamelapp.domain.model.Photo
import com.example.pamelapp.ui.scaffold.AppScaffold
import com.example.pamelapp.ui.theme.BrownLight
import com.example.pamelapp.ui.theme.BrownMid
import com.example.pamelapp.ui.theme.CharcoalWarm
import com.example.pamelapp.ui.theme.CreamWarm
import com.example.pamelapp.ui.theme.Siena
import com.example.pamelapp.ui.theme.SwipeDelete
import com.example.pamelapp.ui.theme.SwipeFav
import com.example.pamelapp.ui.theme.SwipeKeep
import kotlinx.coroutines.launch

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
  val contentResolver = context.contentResolver

  val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    Manifest.permission.READ_MEDIA_IMAGES
  } else {
    Manifest.permission.READ_EXTERNAL_STORAGE
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { granted ->
    if (granted) {
      viewModel.loadSettings(context)
      viewModel.loadPhotos(contentResolver)
      viewModel.loadFavorites(context)
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
      viewModel.loadSettings(context)
      viewModel.loadPhotos(contentResolver)
      viewModel.loadFavorites(context)
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
      IconButton(onClick = navigateToFavorites) {
        Icon(
          Icons.Default.Favorite,
          contentDescription = "Favoritos",
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
      }

      IconButton(onClick = navigateToConfiguration) {
        Icon(
          Icons.Default.Settings,
          contentDescription = "Configuración",
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
      }

      if (state.favorited > 0) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(end = 8.dp)
        ) {
          Icon(
            Icons.Default.Favorite,
            contentDescription = null,
            tint = SwipeFav,
            modifier = Modifier.size(14.dp)
          )

          Spacer(Modifier.width(3.dp))

          Text(
            "${state.favorited}",
            color = SwipeFav,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }

      if (state.pendingDelete.isNotEmpty()) {
        Badge(containerColor = SwipeDelete) {
          TextButton(
            onClick = navigateToDelete,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Icon(
              Icons.Default.DeleteSweep,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )

            Spacer(Modifier.width(4.dp))

            Text(
              "${state.pendingDelete.size}",
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      if (total > 0 && !state.loading) {
        Text(
          text = "$current / $total",
          color = Color.White,
          fontSize = 12.sp,
          modifier = Modifier.padding(start = 8.dp)
        )
      }
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
              viewModel.loadPhotos(contentResolver)
              viewModel.loadFavorites(context)
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
            onKeep = { viewModel.onKeep() },
            onFavorite = { viewModel.onFavorite(context) },
            onDelete = { viewModel.onDelete() },
            onUndoLastAction = { viewModel.undoLastSwipeAction(context) }
          )
        }
      }
    }
  }
}

@Composable
private fun SwipeContent(
  photo: Photo,
  onKeep: () -> Unit,
  onFavorite: () -> Unit,
  onDelete: () -> Unit,
  onUndoLastAction: () -> Unit,
) {
  Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = "Hace ${photo.yearsAgo} año${if (photo.yearsAgo != 1) "s" else ""} · ${photo.year}",
      color = BrownMid,
      fontSize = 13.sp,
      modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
    )

    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp),
      contentAlignment = Alignment.Center,
    ) {
      key(photo.id) {
        SwipeablePhotoCard(
          photo = photo,
          onKeep = onKeep,
          onDelete = onDelete,
          onFavorite = onFavorite,
          onUndoLastAction = onUndoLastAction
        )
      }
    }

    ActionButtons(
      onKeep = onKeep,
      onFavorite = onFavorite,
      onDelete = onDelete,
      onUndoLastAction = onUndoLastAction
    )

    Text(
      text = "← conservar   ↑ favorita   borrar →",
      color = BrownLight,
      fontSize = 11.sp,
      modifier = Modifier.padding(top = 8.dp)
    )

    Text(
      text = "Doble toque para deshacer la última acción",
      color = BrownMid,
      fontSize = 11.sp,
      modifier = Modifier.padding(bottom = 12.dp)
    )
  }
}

@Composable
fun SwipeablePhotoCard(
  photo: Photo,
  onKeep: () -> Unit,
  onDelete: () -> Unit,
  onFavorite: () -> Unit,
  onUndoLastAction: () -> Unit,
) {
  val offsetX = remember(photo.id) { Animatable(0f) }
  val offsetY = remember(photo.id) { Animatable(0f) }
  val threshold = 160f
  val scope = rememberCoroutineScope()

  val overlayColor = when {
    offsetX.value > 80f -> SwipeDelete.copy(
      alpha = (offsetX.value / threshold).coerceIn(0f, 0.5f)
    )

    offsetX.value < -80f -> SwipeKeep.copy(
      alpha = ((-offsetX.value) / threshold).coerceIn(0f, 0.5f)
    )

    offsetY.value < -80f -> SwipeFav.copy(
      alpha = ((-offsetY.value) / threshold).coerceIn(0f, 0.5f)
    )

    else -> Color.Transparent
  }

  val overlayIcon = when {
    offsetX.value > 80f -> Icons.Default.Delete
    offsetX.value < -80f -> Icons.Default.Check
    offsetY.value < -80f -> Icons.Default.Favorite
    else -> null
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .graphicsLayer {
        translationX = offsetX.value
        translationY = offsetY.value
        rotationZ = (offsetX.value / 20f).coerceIn(-15f, 15f)
      }
      .pointerInput(photo.id) {
        detectTapGestures(
          onDoubleTap = {
            onUndoLastAction()
          }
        )
      }
      .pointerInput(photo.id) {
        detectDragGestures(
          onDrag = { change, drag ->
            change.consume()

            scope.launch {
              offsetX.snapTo(offsetX.value + drag.x)
              offsetY.snapTo(offsetY.value + drag.y)
            }
          },
          onDragEnd = {
            scope.launch {
              when {
                offsetX.value > threshold -> {
                  offsetX.animateTo(1200f, spring(stiffness = 800f))
                  onDelete()
                }

                offsetX.value < -threshold -> {
                  offsetX.animateTo(-1200f, spring(stiffness = 800f))
                  onKeep()
                }

                offsetY.value < -threshold -> {
                  offsetY.animateTo(-1200f, spring(stiffness = 800f))
                  onFavorite()
                }

                else -> {
                  launch { offsetX.animateTo(0f, spring(stiffness = 400f)) }
                  launch { offsetY.animateTo(0f, spring(stiffness = 400f)) }
                }
              }
            }
          }
        )
      }
      .clip(RoundedCornerShape(20.dp)),
  ) {
    AsyncImage(
      model = photo.uri,
      contentDescription = photo.displayName,
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize(),
    )

    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(overlayColor)
    )

    overlayIcon?.let { icon ->
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Surface(
          shape = CircleShape,
          color = Color.White.copy(alpha = 0.9f),
          modifier = Modifier.size(72.dp)
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = when {
              offsetX.value > 80f -> SwipeDelete
              offsetX.value < -80f -> SwipeKeep
              else -> SwipeFav
            },
            modifier = Modifier
              .padding(16.dp)
              .fillMaxSize(),
          )
        }
      }
    }

    Box(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(12.dp)
        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
        .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
      Text(
        text = "${photo.year}",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
      )
    }
  }
}

@Composable
fun ActionButtons(
  onKeep: () -> Unit,
  onFavorite: () -> Unit,
  onDelete: () -> Unit,
  onUndoLastAction: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 34.dp, vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    ActionButton(
      onClick = onKeep,
      icon = Icons.Default.Check,
      color = SwipeKeep,
      size = 56,
      contentDescription = "Conservar"
    )

    ActionButton(
      onClick = onUndoLastAction,
      icon = Icons.Default.Refresh,
      color = BrownMid,
      size = 46,
      contentDescription = "Deshacer"
    )

    ActionButton(
      onClick = onFavorite,
      icon = Icons.Default.Favorite,
      color = SwipeFav,
      size = 48,
      contentDescription = "Favorita"
    )

    ActionButton(
      onClick = onDelete,
      icon = Icons.Default.Delete,
      color = SwipeDelete,
      size = 56,
      contentDescription = "Borrar"
    )
  }
}

@Composable
fun ActionButton(
  onClick: () -> Unit,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  color: Color,
  size: Int,
  contentDescription: String,
) {
  Surface(
    onClick = onClick,
    shape = CircleShape,
    color = color.copy(alpha = 0.12f),
    modifier = Modifier.size(size.dp)
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = color,
        modifier = Modifier.size((size * 0.45).dp)
      )
    }
  }
}

@Composable
fun LoadingState() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 28.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    CircularProgressIndicator(color = Siena)

    Spacer(Modifier.height(18.dp))

    Text(
      text = "Buscando tus recuerdos de este día...",
      color = CharcoalWarm,
      fontSize = 17.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(8.dp))

    Text(
      text = "Revisando cámara, WhatsApp, screenshots e Instagram.",
      color = BrownMid,
      fontSize = 13.sp,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
fun PermissionDeniedState() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 28.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = Icons.Default.NoPhotography,
      contentDescription = null,
      modifier = Modifier.size(56.dp),
      tint = Siena
    )

    Spacer(Modifier.height(16.dp))

    Text(
      text = "Necesitamos permiso para ver tus fotos",
      color = CharcoalWarm,
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(8.dp))

    Text(
      text = "Sin ese permiso no podemos encontrar tus recuerdos de años anteriores.",
      color = BrownMid,
      fontSize = 13.sp,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
fun ErrorState(
  message: String,
  onRetry: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 28.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = "Algo salió mal",
      color = CharcoalWarm,
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(8.dp))

    Text(
      text = message,
      color = BrownMid,
      fontSize = 13.sp,
      textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(16.dp))

    TextButton(onClick = onRetry) {
      Text("Intentar de nuevo", color = Siena)
    }
  }
}

@Composable
fun EmptyDayCard() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 28.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = Icons.Default.NoPhotography,
      contentDescription = null,
      modifier = Modifier.size(56.dp),
      tint = Siena
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      "No hay fotos de este día\nen años anteriores",
      color = CharcoalWarm,
      textAlign = TextAlign.Center,
      fontWeight = FontWeight.Bold,
      fontSize = 17.sp
    )

    Spacer(Modifier.height(8.dp))

    Text(
      "Cuando tengas recuerdos con esta fecha, aparecerán aquí.",
      color = BrownMid,
      textAlign = TextAlign.Center,
      fontSize = 13.sp
    )
  }
}

@Composable
fun FinishedState(
  deletedCount: Int,
  freedBytes: Long,
  pendingDeleteCount: Int,
  onReviewDelete: () -> Unit,
  onRestart: () -> Unit,
) {
  val freedMb = freedBytes / 1_048_576f

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 28.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = "Terminaste tus recuerdos de hoy",
      color = CharcoalWarm,
      fontSize = 19.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(10.dp))

    Text(
      text = if (pendingDeleteCount > 0) {
        "Tienes $pendingDeleteCount foto${if (pendingDeleteCount != 1) "s" else ""} pendiente${if (pendingDeleteCount != 1) "s" else ""} de revisar antes de borrar."
      } else {
        "No tienes fotos pendientes de borrar."
      },
      color = BrownMid,
      fontSize = 13.sp,
      textAlign = TextAlign.Center
    )

    if (deletedCount > 0) {
      Spacer(Modifier.height(8.dp))

      Text(
        text = "Ya borraste $deletedCount foto${if (deletedCount != 1) "s" else ""} · %.1f MB liberados".format(freedMb),
        color = BrownMid,
        fontSize = 12.sp,
        textAlign = TextAlign.Center
      )
    }

    Spacer(Modifier.height(18.dp))

    if (pendingDeleteCount > 0) {
      TextButton(onClick = onReviewDelete) {
        Text("Revisar fotos a borrar", color = SwipeDelete)
      }
    }

    TextButton(onClick = onRestart) {
      Text("Volver a revisar desde el inicio", color = Siena)
    }
  }
}