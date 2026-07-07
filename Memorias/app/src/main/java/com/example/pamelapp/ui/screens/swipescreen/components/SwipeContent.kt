package com.example.pamelapp.ui.screens.swipescreen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pamelapp.domain.model.Photo
import com.example.pamelapp.ui.screens.swipescreen.components.ActionButtons
import com.example.pamelapp.ui.screens.swipescreen.components.SwipeablePhotoCard
import com.example.pamelapp.ui.theme.BrownLight
import com.example.pamelapp.ui.theme.BrownMid

@Composable
fun SwipeContent(
  photo: Photo,
  isFavorite: Boolean,
  showActionButtons: Boolean,
  onKeep: () -> Unit,
  onFavorite: () -> Unit,
  onDelete: () -> Unit,
  onUndoLastAction: () -> Unit,
) {
  Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {

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
          isFavorite = isFavorite,
          onKeep = onKeep,
          onDelete = onDelete,
          onFavorite = onFavorite,
          onUndoLastAction = onUndoLastAction
        )
      }
    }
    
    if (showActionButtons) {
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
    }
    Text(
      text = "Doble toque para deshacer la última acción",
      color = BrownMid,
      fontSize = 11.sp,
      modifier = Modifier.padding(bottom = 12.dp)
    )
  }
}
