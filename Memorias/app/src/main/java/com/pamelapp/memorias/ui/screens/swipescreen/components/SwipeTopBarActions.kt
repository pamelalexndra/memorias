package com.pamelapp.memorias.ui.screens.swipescreen.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pamelapp.memorias.ui.theme.SwipeDelete

@Composable
fun SwipeTopBarActions(
  favoritedCount: Int,
  pendingDeleteCount: Int,
  totalPhotos: Int,
  currentPosition: Int,
  isLoading: Boolean,
  onFavoritesClick: () -> Unit,
  onSettingsClick: () -> Unit,
  onDeleteQueueClick: () -> Unit
) {
  IconButton(onClick = onFavoritesClick) {
    BadgedBox(
      badge = {
        if (favoritedCount > 0) {
          Badge(
            containerColor = Color.Red,
            contentColor = Color.White
          ) {
            Text(
              text = if (favoritedCount > 99) "99+" else favoritedCount.toString()
            )
          }
        }
      }
    ) {
      Icon(
        imageVector = Icons.Default.Favorite,
        contentDescription = "Favoritos",
        tint = Color.White, modifier = Modifier.size(30.dp)
      )
    }
  }
  IconButton(onClick = onSettingsClick) {
    Icon(
      imageVector = Icons.Default.Settings,
      contentDescription = "Configuración",
      tint = Color.White,
      modifier = Modifier.size(30.dp)
    )
  }
  
  
  
  if (pendingDeleteCount > 0) {
    Badge(containerColor = SwipeDelete) {
      TextButton(
        onClick = onDeleteQueueClick,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
      ) {
        Icon(
          imageVector = Icons.Default.DeleteSweep,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "$pendingDeleteCount",
          color = Color.White,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
  
  Text(
    text = if (totalPhotos == 0) "" else "$currentPosition / $totalPhotos",
    color = Color.White,
    fontSize = 12.sp,
    modifier = Modifier.padding(start = 8.dp)
  )
  
}