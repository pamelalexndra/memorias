package com.pamelapp.memorias.ui.screens.favoritesscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pamelapp.memorias.ui.scaffold.AppScaffold
import com.pamelapp.memorias.ui.theme.BrownLight
import com.pamelapp.memorias.ui.theme.BrownMid
import com.pamelapp.memorias.ui.theme.CreamWarm
import com.pamelapp.memorias.ui.screens.favoritesscreen.components.FavoritePhotoItem
import com.pamelapp.memorias.ui.theme.Siena

@Composable
fun FavoritesScreen(
  navigateToBack: () -> Unit,
  viewModel: FavoritePhotoViewModel = viewModel(factory = FavoritePhotoViewModel.provideFactory())
) {
  
  val favoritePhotos by viewModel.favoritePhotos.collectAsStateWithLifecycle()
  
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
      
      if (favoritePhotos.isEmpty()) {
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
        Text(
          text = "${if (favoritePhotos.size == 1) "Tu favorita" else "Tus favoritas"}: ${favoritePhotos.size}",
          modifier = Modifier.padding(vertical = 8.dp)
        )
        LazyVerticalGrid(
          columns = GridCells.Fixed(3),
          modifier = Modifier
            .fillMaxSize()
            .weight(1f)
            .padding(horizontal = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          items(favoritePhotos) { photo ->
            FavoritePhotoItem(
              photo = photo,
              isFavorite = true,
              onDelete = { viewModel.deleteFavoritePhoto(photo) }
            )
          }
        }
        Button(
          onClick = { viewModel.clearFavoritePhotos() },
          colors = ButtonDefaults.buttonColors(
            containerColor = Siena,
            contentColor = CreamWarm
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
        ) {
          Icon(
            Icons.Default.Delete,
            contentDescription = "Eliminar",
            tint = Color.White
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Eliminar favoritas")
        }
      }
    }
  }
}