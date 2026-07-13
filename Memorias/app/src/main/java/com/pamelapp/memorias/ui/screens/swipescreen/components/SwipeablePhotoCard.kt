package com.pamelapp.memorias.ui.screens.swipescreen.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.pamelapp.memorias.domain.model.Photo
import com.pamelapp.memorias.ui.theme.SwipeDelete
import com.pamelapp.memorias.ui.theme.SwipeFav
import com.pamelapp.memorias.ui.theme.SwipeKeep
import kotlinx.coroutines.launch
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView

@Composable
fun SwipeablePhotoCard(
  photo: Photo,
  isFavorite: Boolean,
  onKeep: () -> Unit,
  onDelete: () -> Unit,
  onFavorite: () -> Unit,
  onUndoLastAction: () -> Unit,
) {
  val offsetX = remember(photo.id) { Animatable(0f) }
  val offsetY = remember(photo.id) { Animatable(0f) }
  val threshold = 160f
  val scope = rememberCoroutineScope()
  val view = LocalView.current
  var deleteHapticTriggered by remember(photo.id) { mutableStateOf(false) }

  fun performDeleteHaptic() {
    val feedbackType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      HapticFeedbackConstants.REJECT
    } else {
      HapticFeedbackConstants.LONG_PRESS
    }

    view.performHapticFeedback(feedbackType)
  }

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

            val nextOffsetX = offsetX.value + drag.x
            val nextOffsetY = offsetY.value + drag.y

            if (nextOffsetX > threshold && !deleteHapticTriggered) {
              deleteHapticTriggered = true
              performDeleteHaptic()
            }

            if (nextOffsetX <= threshold) {
              deleteHapticTriggered = false
            }

            scope.launch {
              offsetX.snapTo(nextOffsetX)
              offsetY.snapTo(nextOffsetY)
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
                  launch {
                    offsetX.animateTo(0f, spring(stiffness = 400f))
                  }

                  launch {
                    offsetY.animateTo(0f, spring(stiffness = 400f))
                  }
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

    if (isFavorite) {
      Surface(
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.92f),
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(12.dp)
          .size(42.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Favorite,
          contentDescription = "Foto favorita",
          tint = SwipeFav,
          modifier = Modifier.padding(9.dp)
        )
      }
    }

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