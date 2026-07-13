package com.pamelapp.memorias.ui.screens.favoritesscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Surface
import coil3.compose.AsyncImage
import com.pamelapp.memorias.domain.model.Photo
import com.pamelapp.memorias.ui.theme.SwipeDelete

@Composable
fun FavoritePhotoItem(
  photo: Photo,
  isFavorite: Boolean,
  onDelete: () -> Unit
) {
  Box(
    modifier = Modifier
      .aspectRatio(1f)
      .clip(RoundedCornerShape(8.dp))
      .background(Color.Black)
  ) {
    AsyncImage(
      model = photo.uri,
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize()
    )
    
    Box(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(4.dp)
        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(4.dp))
        .padding(horizontal = 4.dp, vertical = 2.dp),
    ) {
      Text(
        text = "${photo.year}",
        color = Color.White,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold
      )
    }
    
    if (isFavorite) {
      Surface(
        onClick = { onDelete() },
        shape = CircleShape,
        color = SwipeDelete,
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(4.dp)
          .size(22.dp),
      ) {
        Icon(
          Icons.Default.Close,
          contentDescription = "Quitar",
          tint = Color.White,
          modifier = Modifier.padding(4.dp)
        )
      }
    }
  }
}