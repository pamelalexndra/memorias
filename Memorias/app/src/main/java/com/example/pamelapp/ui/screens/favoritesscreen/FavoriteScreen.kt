package com.example.pamelapp.ui.screens.favoritesscreen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.pamelapp.domain.model.Photo
import com.example.pamelapp.ui.scaffold.AppScaffold
import com.example.pamelapp.ui.theme.*
/*
@Composable
fun FavoritesScreen(
  navigateToBack: () -> Unit,
  viewModel: FavoriteViewModel = viewModel()
) {
  val favorites by viewModel.favorites.collectAsState()
  val recycleBinMode by viewModel.recycleBinMode.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  var selectedPhotos by remember { mutableStateOf<Set<Photo>>(emptySet()) }
  val context = LocalContext.current
  val totalMb = selectedPhotos.sumOf { it.sizeBytes } / 1_048_576f
  
  val deleteLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.StartIntentSenderForResult()
  ) { result ->
    if (result.resultCode == android.app.Activity.RESULT_OK) {
      viewModel.onDeleteSuccess(selectedPhotos.toList())
      selectedPhotos = emptySet()
    }
  }
  
  LaunchedEffect(Unit) {
    viewModel.loadFavorites(context)
  }
  
  AppScaffold(
    title = "Tus favoritos",
    navigationIcon = {
      IconButton(onClick = { navigateToBack() }) {
        Icon(
          Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Volver",
          tint = Color.White
        )
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(CreamWarm)
        .padding(paddingValues),
    ) {
      if (selectedPhotos.isNotEmpty()) {
        Surface(color = Color.White, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              "${selectedPhotos.size} fotos seleccionadas · ${"%.1f".format(totalMb)} MB",
              color = BrownMid,
              fontSize = 12.sp,
            )
            Button(
              onClick = {
                viewModel.deleteSelected(
                  selectedPhotos.toList(),
                  context.contentResolver,
                  deleteLauncher
                )
              },
              colors = ButtonDefaults.buttonColors(containerColor = SwipeDelete),
              modifier = Modifier.height(36.dp),
              enabled = !isLoading
            ) {
              if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
              } else {
                Icon(
                  if (recycleBinMode) Icons.Default.DeleteSweep else Icons.Default.DeleteForever,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Eliminar", fontSize = 12.sp)
              }
            }
          }
        }
      }
      
      if (favorites.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center,
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              Icons.Default.HeartBroken,
              contentDescription = null,
              tint = BrownLight,
              modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
              "No tienes fotos favoritas",
              color = BrownMid,
              fontSize = 16.sp,
              textAlign = TextAlign.Center
            )
            Text(
              "Desliza hacia arriba en las fotos para marcarlas como favoritas",
              color = BrownLight,
              fontSize = 12.sp,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(top = 8.dp)
            )
          }
        }
      } else {
        LazyVerticalGrid(
          columns = GridCells.Fixed(3),
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          items(favorites, key = { it.id }) { photo ->
            val isSelected = selectedPhotos.contains(photo)
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
              
              if (isSelected) {
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(SwipeFav.copy(alpha = 0.3f))
                )
              }
              
              Checkbox(
                checked = isSelected,
                onCheckedChange = { checked ->
                  selectedPhotos = if (checked) selectedPhotos + photo else selectedPhotos - photo
                },
                modifier = Modifier
                  .align(Alignment.TopStart)
                  .padding(4.dp)
                  .size(24.dp),
                colors = CheckboxDefaults.colors(
                  checkedColor = SwipeFav,
                  uncheckedColor = Color.White
                )
              )
              
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
      }
    }
  }
}*/