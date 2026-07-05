package com.memorias.app.presentation.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.memorias.app.domain.model.enums.DragOutcome
import com.memorias.app.domain.model.enums.SwipeAction
import com.memorias.app.presentation.theme.SwipeDelete
import com.memorias.app.presentation.theme.SwipeFav
import com.memorias.app.presentation.theme.SwipeKeep
import kotlinx.coroutines.launch

@Composable
fun SwipeablePhotoCard(
    imageModel: Any?,
    badgeLabel: String? = null,
    onSwiped: (SwipeAction) -> Unit,
    modifier: Modifier = Modifier,
    swipeThresholdPx: Float = 420f,
) {
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val rightProgress = (offsetX.value / swipeThresholdPx).coerceIn(0f, 1f)
    val leftProgress  = (-offsetX.value / swipeThresholdPx).coerceIn(0f, 1f)
    val upProgress    = (-offsetY.value / swipeThresholdPx).coerceIn(0f, 1f)

    val dominant = when {
        rightProgress >= leftProgress && rightProgress >= upProgress && rightProgress > 0f -> "right"
        leftProgress >= upProgress && leftProgress > 0f -> "left"
        upProgress > 0f -> "up"
        else -> "none"
    }

    val overlayColor = when (dominant) {
        "right" -> SwipeDelete.copy(alpha = rightProgress * 0.45f)
        "left"  -> SwipeKeep.copy(alpha = leftProgress * 0.45f)
        "up"    -> SwipeFav.copy(alpha = upProgress * 0.45f)
        else    -> Color.Transparent
    }

    val activeIcon = when {
        dominant == "right" && rightProgress > 0.25f -> Icons.Default.Delete
        dominant == "left" && leftProgress > 0.25f    -> Icons.Default.Check
        dominant == "up" && upProgress > 0.25f        -> Icons.Default.Favorite
        else                                           -> null
    }

    val activeIconColor = when (dominant) {
        "right" -> SwipeDelete
        "left"  -> SwipeKeep
        else    -> SwipeFav
    }

    fun resolveDrag(): DragOutcome = when {
        offsetX.value > swipeThresholdPx  -> DragOutcome.RIGHT
        offsetX.value < -swipeThresholdPx -> DragOutcome.LEFT
        offsetY.value < -swipeThresholdPx -> DragOutcome.UP
        else                               -> DragOutcome.NONE
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = offsetX.value
                translationY = offsetY.value
                rotationZ = (offsetX.value / 28f).coerceIn(-12f, 12f)
            }
            .pointerInput(Unit) {
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
                            when (resolveDrag()) {
                                DragOutcome.RIGHT -> {
                                    offsetX.animateTo(1400f, spring(stiffness = 700f))
                                    onSwiped(SwipeAction.DELETE)
                                }
                                DragOutcome.LEFT -> {
                                    offsetX.animateTo(-1400f, spring(stiffness = 700f))
                                    onSwiped(SwipeAction.KEEP)
                                }
                                DragOutcome.UP -> {
                                    offsetY.animateTo(-1400f, spring(stiffness = 700f))
                                    onSwiped(SwipeAction.FAVORITE)
                                }
                                DragOutcome.NONE -> {
                                    launch { offsetX.animateTo(0f, spring(stiffness = 380f)) }
                                    launch { offsetY.animateTo(0f, spring(stiffness = 380f)) }
                                }
                            }
                        }
                    },
                )
            }
            .clip(RoundedCornerShape(22.dp)),
    ) {
        AsyncImage(
            model = imageModel,
            contentDescription = badgeLabel,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Box(modifier = Modifier.fillMaxSize().background(overlayColor))

        activeIcon?.let { icon ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.92f),
                    modifier = Modifier.size(76.dp),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = activeIconColor,
                        modifier = Modifier.padding(18.dp).fillMaxSize(),
                    )
                }
            }
        }

        badgeLabel?.let { label ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}