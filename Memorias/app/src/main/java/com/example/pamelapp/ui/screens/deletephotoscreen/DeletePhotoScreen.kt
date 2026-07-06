package com.example.pamelapp.ui.screens.deletephotoscreen

package com.example.pamelapp.ui.screens.deletephotoscreen

import android.app.Activity
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.runtime.LaunchedEffect
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
import coil3.compose.AsyncImage
import com.example.pamelapp.domain.model.DeleteMode
import com.example.pamelapp.ui.scaffold.AppScaffold
import com.example.pamelapp.ui.screens.swipescreen.SwipeViewModel
import com.example.pamelapp.ui.theme.BrownMid
import com.example.pamelapp.ui.theme.CreamWarm
import com.example.pamelapp.ui.theme.SwipeDelete

@Composable
fun DeletePhotoScreen(
  navigateToBack: () -> Unit,
  viewModel: SwipeViewModel
) {
  val state by viewModel.state.collectAsState()
  val photos = state.pendingDelete
  val deleteMode = state.deleteMode
  val context = LocalContext.current
  val totalMb = photos.sumOf { it.sizeBytes } / 1_048_576f

  LaunchedEffect(Unit) {
    viewModel.loadSettings(context)
  }

  val deleteLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.StartIntentSenderForResult()
  ) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
      viewModel.onDeleteSuccess(context)
      navigateToBack()
    } else {
      viewModel.onDeleteCancelled()
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
          if (photos.isEmpty()) return@Button

          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
              val uris = photos.map { it.uri }

              val pendingIntent = when (deleteMode) {
                DeleteMode.TRASH -> {
                  MediaStore.createTrashRequest(
                    context.contentResolver,
                    uris,
                    true
                  )
                }

                DeleteMode.PERMANENT -> {
                  MediaStore.createDeleteRequest(
                    context.contentResolver,
                    uris
                  )
                }
              }

              deleteLauncher.launch(
                IntentSenderRequest.Builder(pendingIntent.intentSender).build()
              )
            } catch (exception: Exception) {
              viewModel.setError("No se pudo iniciar la eliminación. Inténtalo de nuevo.")
            }
          } else {
            if (deleteMode == DeleteMode.TRASH) {
              viewModel.setError("La papelera solo está disponible desde Android 11.")
            } else {
              viewModel.deletePendingDirectly(
                context = context,
                contentResolver = context.contentResolver,
                onComplete = navigateToBack
              )
            }
          }
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
          if (deleteMode == DeleteMode.TRASH) {
            "Enviar ${photos.size} foto${if (photos.size != 1) "s" else ""} a papelera"
          } else {
            "Borrar ${photos.size} foto${if (photos.size != 1) "s" else ""} permanentemente"
          },
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp
        )
      }
      Spacer(Modifier.height(20.dp))
    }
  }
}