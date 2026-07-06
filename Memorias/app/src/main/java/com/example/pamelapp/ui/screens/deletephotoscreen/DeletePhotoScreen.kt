package com.example.pamelapp.ui.screens.deletephotoscreen

import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.pamelapp.data.Photo
import com.example.pamelapp.ui.scaffold.AppScaffold
import com.example.pamelapp.ui.theme.*

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun DeletePhotoScreen(
  navigateToBack: () -> Unit,
  viewModel: DeletePhotoViewModel = viewModel()
) {
  
  val photos by viewModel.pendingDelete.collectAsState()
  val context = LocalContext.current
  val totalMb = photos.sumOf { it.sizeBytes } / 1_048_576f
  
  val deleteLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.StartIntentSenderForResult()
  ) { result ->
    if (result.resultCode == android.app.Activity.RESULT_OK) {
      viewModel.onDeleteConfirmed()
      navigateToBack()
    }
  }
  
  AppScaffold(
    title = "Confirmar eliminación",
    navigationIcon = {
      IconButton(onClick = { navigateToBack() }) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(CreamWarm)
        .padding(paddingValues),
    ) {
      Text(
        text = "Toca una foto para quitarla de la selección",
        color = BrownMid,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 10.dp),
      )
      
      LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        items(photos, key = { it.id }) { photo ->
          Box(
            modifier = Modifier
              .aspectRatio(1f)
              .clip(RoundedCornerShape(8.dp)),
          ) {
            AsyncImage(
              model = photo.uri,
              contentDescription = null,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(SwipeDelete.copy(alpha = 0.25f))
            )
            Surface(
              onClick = { viewModel.removeFromQueue(photo) },
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
            Box(
              modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp)
                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp),
            ) {
              Text(
                "${photo.year}",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
      
      Spacer(Modifier.height(12.dp))
      Button(
        onClick = {
          val uris = photos.map { it.uri }
          val pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, uris)
          deleteLauncher.launch(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
        },
        colors = ButtonDefaults.buttonColors(containerColor = SwipeDelete),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp)
          .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        enabled = photos.isNotEmpty(),
      ) {
        Icon(
          Icons.Default.DeleteForever,
          contentDescription = null,
          modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
          "Eliminar ${photos.size} foto${if (photos.size != 1) "s" else ""}",
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp
        )
      }
      Spacer(Modifier.height(20.dp))
    }
  }
}