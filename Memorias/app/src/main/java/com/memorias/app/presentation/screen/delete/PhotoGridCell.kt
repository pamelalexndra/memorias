package com.memorias.app.presentation.screen.delete

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import coil3.compose.AsyncImage
import com.memorias.app.domain.model.Photo
import com.memorias.app.presentation.theme.SwipeDelete
import com.memorias.app.util.toMediaStoreUri


@Composable
fun PhotoGridCell(photo: Photo, onRemove: () -> Unit) {
    Box(modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp))) {
        AsyncImage(
            model = photo.toMediaStoreUri(),
            contentDescription = photo.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)))

        Surface(
            onClick = onRemove,
            shape = CircleShape,
            color = SwipeDelete,
            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(22.dp),
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Quitar",
                tint = Color.White,
                modifier = Modifier.padding(4.dp),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp)
                .clip(RoundedCornerShape(4.dp)),
        ) {
            Surface(color = Color.Black.copy(alpha = 0.55f), shape = RoundedCornerShape(4.dp)) {
                Text(
                    "${photo.year}",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }
        }
    }
}
