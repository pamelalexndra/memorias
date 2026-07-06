package com.example.pamelapp.ui.screens.swipescreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pamelapp.ui.theme.BrownMid
import com.example.pamelapp.ui.theme.CharcoalWarm
import com.example.pamelapp.ui.theme.Siena
import com.example.pamelapp.ui.theme.SwipeDelete

@Composable fun LoadingState() {
    Column(
        modifier = Modifier .fillMaxSize() .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = Siena)
        Spacer(modifier = Modifier.height(18.dp))
        Text( text = "Buscando tus recuerdos de este día...", color = CharcoalWarm, fontSize = 17.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center )
        Spacer(modifier = Modifier.height(8.dp))
        Text( text = "Revisando cámara, WhatsApp, screenshots e Instagram.", color = BrownMid, fontSize = 13.sp, textAlign = TextAlign.Center )
    }
}

@Composable fun PermissionDeniedState() {
    Column(
        modifier = Modifier .fillMaxSize() .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center ) {
        Icon( imageVector = Icons.Default.NoPhotography, contentDescription = null, modifier = Modifier.size(56.dp), tint = Siena )
        Spacer(modifier = Modifier.height(16.dp))
        Text( text = "Necesitamos permiso para ver tus fotos", color = CharcoalWarm, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center )
        Spacer(modifier = Modifier.height(8.dp))
        Text( text = "Sin ese permiso no podemos encontrar tus recuerdos de años anteriores.", color = BrownMid, fontSize = 13.sp, textAlign = TextAlign.Center )
    }
}

@Composable fun ErrorState( message: String, onRetry: () -> Unit, ) {
    Column(
        modifier = Modifier .fillMaxSize() .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center ) {
        Text( text = "Algo salió mal", color = CharcoalWarm, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center )
        Spacer(modifier = Modifier.height(8.dp))
        Text( text = message, color = BrownMid, fontSize = 13.sp, textAlign = TextAlign.Center )
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onRetry) { Text( text = "Intentar de nuevo", color = Siena )
        }
    }
}

@Composable fun EmptyDayCard() {
    Column(
        modifier = Modifier .fillMaxSize() .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center )
    {
        Icon(
            imageVector = Icons.Default.NoPhotography,
            contentDescription = null,
            modifier = Modifier.size(56.dp), tint = Siena
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text( text = "No hay fotos de este día\nen años anteriores", color = CharcoalWarm, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 17.sp )
        Spacer(modifier = Modifier.height(8.dp))
        Text( text = "Cuando tengas recuerdos con esta fecha, aparecerán aquí.", color = BrownMid, textAlign = TextAlign.Center, fontSize = 13.sp )
    }
}

@Composable fun FinishedState(
    deletedCount: Int,
    freedBytes: Long,
    pendingDeleteCount: Int,
    onReviewDelete: () -> Unit,
    onRestart: () -> Unit, )
{
    val freedMb = freedBytes / 1_048_576f
    Column(
        modifier = Modifier .fillMaxSize() .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center )
    {
        Text( text = "Terminaste tus recuerdos de hoy", color = CharcoalWarm, fontSize = 19.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center )
        Spacer(modifier = Modifier.height(10.dp))
        Text( text = if (pendingDeleteCount > 0) {
            "Tienes $pendingDeleteCount foto${if (pendingDeleteCount != 1) "s" 
            else ""} pendiente${if (pendingDeleteCount != 1) "s" 
            else ""} de revisar antes de borrar." }
        else { "No tienes fotos pendientes de borrar." },
            color = BrownMid, fontSize = 13.sp, textAlign = TextAlign.Center )
        if (deletedCount > 0) { Spacer(modifier = Modifier.height(8.dp))
            Text( text = "Ya borraste $deletedCount foto${if (deletedCount != 1) "s" else ""} · %.1f MB liberados".format(freedMb), color = BrownMid, fontSize = 12.sp, textAlign = TextAlign.Center ) }
        Spacer(modifier = Modifier.height(18.dp))
        if (pendingDeleteCount > 0) { TextButton(onClick = onReviewDelete) { Text( text = "Revisar fotos a borrar", color = SwipeDelete ) } }
        TextButton(onClick = onRestart) { Text( text = "Volver a revisar desde el inicio", color = Siena )
        }
    }
}