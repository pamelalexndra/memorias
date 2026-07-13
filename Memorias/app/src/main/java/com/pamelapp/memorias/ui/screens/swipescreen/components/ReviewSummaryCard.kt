package com.pamelapp.memorias.ui.screens.swipescreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pamelapp.memorias.ui.theme.BrownMid
import com.pamelapp.memorias.ui.theme.CharcoalWarm
import com.pamelapp.memorias.ui.theme.SwipeDelete
import com.pamelapp.memorias.ui.theme.SwipeFav
import com.pamelapp.memorias.ui.theme.SwipeKeep

@Composable
fun ReviewSummaryCard(
  totalPhotos: Int,
  favoriteCount: Int,
  pendingDeleteCount: Int,
  freedBytes: Long,
  onReviewDelete: () -> Unit,
  onViewFavorites: () -> Unit,
  onRestart: () -> Unit,
  modifier: Modifier = Modifier
) {
  val formattedSelectedSpace = freedBytes.formatBytesForSummary()
  
  ElevatedCard(
    modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = Color.White
    ),
    elevation = CardDefaults.elevatedCardElevation(
      defaultElevation = 6.dp
    )
  ) {
    Column(
      modifier = Modifier
          .fillMaxWidth()
          .padding(22.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
            .background(
                color = SwipeKeep.copy(alpha = 0.14f),
                shape = CircleShape
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = null,
          tint = SwipeKeep
        )
      }
      
      Spacer(modifier = Modifier.height(14.dp))
      
      Text(
        text = "Revisión completada",
        color = CharcoalWarm,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
      )
      
      Spacer(modifier = Modifier.height(6.dp))
      
      Text(
        text = "¡Terminaste de revisar las memorias de este día!.",
        color = BrownMid,
        fontSize = 13.sp,
        textAlign = TextAlign.Center
      )
      
      Spacer(modifier = Modifier.height(22.dp))
      
      Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF9F3EC),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        SummaryRow(
          icon = Icons.Default.PhotoLibrary,
          label = "Fotos revisadas",
          value = totalPhotos.toString(),
          color = CharcoalWarm
        )
        
        HorizontalDivider(
          color = BrownMid.copy(alpha = 0.12f)
        )
        
        SummaryRow(
          icon = Icons.Default.Favorite,
          label = "Favoritas guardadas",
          value = favoriteCount.toString(),
          color = SwipeFav
        )
        
        HorizontalDivider(
          color = BrownMid.copy(alpha = 0.12f)
        )
        
        SummaryRow(
          icon = Icons.Default.Delete,
          label = "Pendientes por borrar",
          value = pendingDeleteCount.toString(),
          color = SwipeDelete
        )
        
        HorizontalDivider(
          color = BrownMid.copy(alpha = 0.12f)
        )
        
        SummaryRow(
          icon = Icons.Default.Storage,
          label = "Espacio liberado",
          value = if (pendingDeleteCount > 0) formattedSelectedSpace else "0 B",
          color = SwipeKeep
        )
      }
      
      Spacer(modifier = Modifier.height(20.dp))
      
      
      Spacer(modifier = Modifier.height(10.dp))
    }
    
    Spacer(modifier = Modifier.height(10.dp))
    
    OutlinedButton(
      onClick = onRestart,
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp)
    ) {
      Icon(
        imageVector = Icons.Default.RestartAlt,
        contentDescription = null
      )
      
      Spacer(modifier = Modifier.width(8.dp))
      
      Text(
        text = "Volver a revisar"
      )
    }
  }
}

@Composable
private fun SummaryRow(
  icon: ImageVector,
  label: String,
  value: String,
  color: Color
) {
  Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 9.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
          .background(
              color = color.copy(alpha = 0.12f),
              shape = CircleShape
          )
          .padding(8.dp),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = color
      )
    }
    
    Spacer(modifier = Modifier.width(12.dp))
    
    Text(
      text = label,
      color = CharcoalWarm,
      fontSize = 14.sp,
      fontWeight = FontWeight.Medium,
      modifier = Modifier.weight(1f)
    )
    
    Text(
      text = value,
      color = color,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.End
    )
  }
}

private fun Long.formatBytesForSummary(): String {
  return when {
    this >= 1_073_741_824L -> "%.2f GB".format(this / 1_073_741_824f)
    this >= 1_048_576L -> "%.1f MB".format(this / 1_048_576f)
    this >= 1_024L -> "${this / 1_024} KB"
    else -> "$this B"
  }
}