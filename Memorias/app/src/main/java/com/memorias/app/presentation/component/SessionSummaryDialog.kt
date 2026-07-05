package com.memorias.app.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memorias.app.presentation.extension.formatBytes
import com.memorias.app.presentation.theme.BrownMid
import com.memorias.app.presentation.theme.CharcoalWarm
import com.memorias.app.presentation.theme.SwipeDelete
import com.memorias.app.presentation.theme.SwipeFav
import com.memorias.app.presentation.theme.SwipeKeep

@Composable
fun SessionSummaryDialog(
    photosReviewed: Int,
    photosDeleted: Int,
    photosFavorited: Int,
    spaceFreedBytes: Long,
    currentStreak: Int,
    onDismiss: () -> Unit,
    onStartNewSession: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onStartNewSession, modifier = Modifier.fillMaxWidth()) {
                Text("Empezar de nuevo")
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("🎉", fontSize = 40.sp)
                Spacer(Modifier.height(8.dp))
                Text("¡Sesión completa!", fontWeight = FontWeight.Bold, fontSize = 19.sp, color = CharcoalWarm)
            }
        },
        text = {
            Column {
                Text(
                    text = "Revisaste $photosReviewed foto${if (photosReviewed != 1) "s" else ""} de hoy en años pasados.",
                    color = BrownMid,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    StatPill(label = "Eliminadas", value = "$photosDeleted", color = SwipeDelete, modifier = Modifier.weight(1f))
                    StatPill(label = "Favoritas", value = "$photosFavorited", color = SwipeFav, modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(10.dp))

                if (spaceFreedBytes > 0) {
                    InfoRow(
                        icon = Icons.Default.Storage,
                        label = "Espacio liberado",
                        value = spaceFreedBytes.formatBytes(),
                        color = SwipeKeep,
                    )
                    Spacer(Modifier.height(8.dp))
                }

                if (currentStreak > 0) {
                    InfoRow(
                        icon = Icons.Default.LocalFireDepartment,
                        label = "Racha actual",
                        value = "$currentStreak día${if (currentStreak != 1) "s" else ""}",
                        color = androidx.compose.ui.graphics.Color(0xFFF5C842),
                    )
                }
            }
        },
    )
}

