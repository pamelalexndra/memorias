package com.example.pamelapp.ui.screens.swipescreen.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pamelapp.ui.theme.SwipeDelete
import com.example.pamelapp.ui.theme.SwipeFav

@Composable fun SwipeTopBarActions(
    favoritedCount: Int,
    pendingDeleteCount: Int,
    totalPhotos: Int, currentPosition: Int,
    isLoading: Boolean, onFavoritesClick: () -> Unit,
    onSettingsClick: () -> Unit, onDeleteQueueClick: () -> Unit
) {
    IconButton(onClick = onFavoritesClick) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Favoritos",
            tint = Color.White, modifier = Modifier.size(20.dp) ) }
    IconButton(onClick = onSettingsClick) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Configuración",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
    if (favoritedCount > 0) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 8.dp) )
        { Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null, tint = SwipeFav,
            modifier = Modifier.size(14.dp) )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "$favoritedCount",
                color = SwipeFav, fontSize = 12.sp, fontWeight = FontWeight.Medium )
        } }
    if (pendingDeleteCount > 0) {
        Badge(containerColor = SwipeDelete) {
            TextButton( onClick = onDeleteQueueClick, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp) ) {
                Icon( imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp) )
                Spacer(modifier = Modifier.width(4.dp))
                Text( text = "$pendingDeleteCount", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold ) } }
    }
    if (totalPhotos > 0 && !isLoading) {
        Text( text = "$currentPosition / $totalPhotos", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp) )
    }
}