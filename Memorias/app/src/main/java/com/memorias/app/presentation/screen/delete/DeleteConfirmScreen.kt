package com.memorias.app.presentation.screen.delete

import com.memorias.app.domain.model.Photo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memorias.app.presentation.extension.formatBytes
import com.memorias.app.presentation.theme.BrownMid
import com.memorias.app.presentation.theme.CharcoalWarm
import com.memorias.app.presentation.theme.Siena
import com.memorias.app.presentation.theme.SwipeDelete

@Composable
fun DeleteConfirmScreen(
    photos: List<Photo>,
    onRemove: (Photo) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    val totalBytes = photos.sumOf { it.sizeBytes }

    Column(modifier = Modifier.fillMaxSize().padding(top = 0.dp)) {

        Surface(color = Color.White, shadowElevation = 1.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Siena)
                }
                Column(Modifier.weight(1f)) {
                    Text("Confirmar eliminacion", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = CharcoalWarm)
                    Text("${photos.size} fotos - ${totalBytes.formatBytes()}", color = BrownMid, fontSize = 12.sp)
                }
            }
        }

        Text(
            text = "Toca una foto para quitarla de la seleccion",
            color = BrownMid,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(photos, key = { it.id }) { photo ->
                PhotoGridCell(photo = photo, onRemove = { onRemove(photo) })
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onConfirm,
            colors = ButtonDefaults.buttonColors(containerColor = SwipeDelete),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(52.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = photos.isNotEmpty(),
        ) {
            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "Eliminar ${photos.size} foto${if (photos.size != 1) "s" else ""}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            )
        }
        Spacer(Modifier.height(20.dp))
    }
}
