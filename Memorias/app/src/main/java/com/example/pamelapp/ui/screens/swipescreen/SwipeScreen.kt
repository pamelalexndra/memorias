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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButtonDefaults.Icon
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
import com.example.pamelapp.data.Photo
import com.example.pamelapp.ui.scaffold.AppScaffold
import com.example.pamelapp.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
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
  val cr = context.contentResolver
  
  val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
    Manifest.permission.READ_MEDIA_IMAGES
  else
    Manifest.permission.READ_EXTERNAL_STORAGE
  
  val permLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { granted ->
    if (granted) {
      viewModel.loadPhotos(cr)
      viewModel.loadFavorites(context)
    } else {
      viewModel.setPermissionDenied()
    }
  }
  
  LaunchedEffect(Unit) {
    val alreadyGranted = ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED
    if (alreadyGranted) {
      viewModel.loadPhotos(cr)
      viewModel.loadFavorites(context)
    } else {
      permLauncher.launch(permission)
    }
  }
  
  val total = state.photos.size
  val current = state.currentIndex + 1
  val photo = state.photos.getOrNull(state.currentIndex)
  
  AppScaffold(
    title = "Memorias",
    actions = {
      IconButton(onClick = { navigateToFavorites() }) {
        Icon(
          Icons.Default.Favorite,
          contentDescription = "Favoritos",
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
      }
      IconButton(onClick = { navigateToConfiguration() }) {
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
            onClick = {navigateToDelete()},
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
      Text(
        text = if (total == 0) "" else "$current / $total",
        color = Color.White,
        fontSize = 12.sp,
        modifier = Modifier.padding(start = 8.dp)
      )
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(CreamWarm)
        .padding(paddingValues)
    ) {
      Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        photo?.let {
          Text(
            text = "Hace ${LocalDate.now().year - it.year} año${if (LocalDate.now().year - it.year != 1) "s" else ""} · ${it.year}",
            color = BrownMid,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
          )
        }
        
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
          contentAlignment = Alignment.Center,
        ) {
          if (photo != null) {
            key(state.currentIndex) {
              SwipeablePhotoCard(
                photo = photo,
                onKeep = { viewModel.onKeep() },
                onDelete = { viewModel.onDelete() },
                onFavorite = { viewModel.onFavorite(context) },
              )
            }
          } else {
            EmptyDayCard()
          }
        }
        
        if (photo != null) {
          ActionButtons(
            onKeep = { viewModel.onKeep() },
            onFavorite = { viewModel.onFavorite(context) },
            onDelete = { viewModel.onDelete() },
          )
        }
        
        Text(
          text = "← conservar   ↑ favorita   borrar →",
          color = BrownLight,
          fontSize = 11.sp,
          modifier = Modifier.padding(vertical = 12.dp)
        )
      }
    }
  }
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun SwipeablePhotoCard(
  photo: Photo,
  onKeep: () -> Unit,
  onDelete: () -> Unit,
  onFavorite: () -> Unit,
) {
  val offsetX = remember { Animatable(0f) }
  val offsetY = remember { Animatable(0f) }
  val threshold = 160f
  val scope = rememberCoroutineScope()
  
  val overlayColor = when {
    offsetX.value > 80f -> SwipeDelete.copy(alpha = (offsetX.value / threshold).coerceIn(0f, 0.5f))
    offsetX.value < -80f -> SwipeKeep.copy(
      alpha = ((-offsetX.value) / threshold).coerceIn(
        0f,
        0.5f
      )
    )
    
    offsetY.value < -80f -> SwipeFav.copy(alpha = ((-offsetY.value) / threshold).coerceIn(0f, 0.5f))
    else -> Color.Transparent
  }
  
  val overlayIcon = when {
    offsetX.value > 80f -> Icons.Default.Delete
    offsetX.value < -80f -> Icons.Default.Check
    offsetY.value < -80f -> Icons.Default.Favorite
    else -> null
  }
  
  fun triggerAction(action: () -> Unit) {
    scope.launch { action() }
  }
  
  Box(
    modifier = Modifier
      .fillMaxSize()
      .graphicsLayer {
        translationX = offsetX.value
        translationY = offsetY.value
        rotationZ = (offsetX.value / 20f).coerceIn(-15f, 15f)
      }
      .pointerInput(Unit) {
        detectDragGestures(
          onDragEnd = {
            scope.launch {
              when {
                offsetX.value > threshold -> {
                  offsetX.animateTo(1200f, spring(stiffness = 800f))
                  triggerAction(onDelete)
                }
                
                offsetX.value < -threshold -> {
                  offsetX.animateTo(-1200f, spring(stiffness = 800f))
                  triggerAction(onKeep)
                }
                
                offsetY.value < -threshold -> {
                  offsetY.animateTo(-1200f, spring(stiffness = 800f))
                  triggerAction(onFavorite)
                }
                
                else -> {
                  launch { offsetX.animateTo(0f, spring(stiffness = 400f)) }
                  launch { offsetY.animateTo(0f, spring(stiffness = 400f)) }
                }
              }
            }
          },
          onDrag = { change, drag ->
            change.consume()
            scope.launch {
              offsetX.snapTo(offsetX.value + drag.x)
              offsetY.snapTo(offsetY.value + drag.y)
            }
          },
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
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
fun ActionButtons(onKeep: () -> Unit, onFavorite: () -> Unit, onDelete: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 48.dp, vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    ActionButton(onClick = onKeep, icon = Icons.Default.Check, color = SwipeKeep, size = 56)
    ActionButton(onClick = onFavorite, icon = Icons.Default.Favorite, color = SwipeFav, size = 48)
    ActionButton(onClick = onDelete, icon = Icons.Default.Delete, color = SwipeDelete, size = 56)
  }
}

@Composable
fun ActionButton(
  onClick: () -> Unit,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  color: Color,
  size: Int
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
        contentDescription = null,
        tint = color,
        modifier = Modifier.size((size * 0.45).dp)
      )
    }
  }
}

@Composable
fun EmptyDayCard() {
  Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = Icons.Default.NoPhotography,
      contentDescription = "Camara",
      modifier= Modifier.size(48.dp),
      tint = Siena
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      "No hay fotos de\neste día en años anteriores",
      color = BrownMid,
      textAlign = TextAlign.Center,
      fontSize = 16.sp
    )
  }
}