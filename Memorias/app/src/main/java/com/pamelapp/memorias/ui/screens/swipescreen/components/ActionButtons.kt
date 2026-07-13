package com.pamelapp.memorias.ui.screens.swipescreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.pamelapp.memorias.ui.theme.BrownMid
import com.pamelapp.memorias.ui.theme.SwipeDelete
import com.pamelapp.memorias.ui.theme.SwipeFav
import com.pamelapp.memorias.ui.theme.SwipeKeep


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
  icon: ImageVector,
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