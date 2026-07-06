package com.example.pamelapp.ui.screens.configscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pamelapp.ui.scaffold.AppScaffold
import com.example.pamelapp.ui.theme.*

@Composable
fun ConfigurationScreen(
  navigateToBack: () -> Unit,
  viewModel: ConfigurationViewModel = viewModel()
) {
  
  val recycleBinMode by viewModel.recycleBinMode.collectAsState()
  val context = LocalContext.current
  
  var showClearFavoritesDialog by remember { mutableStateOf(false) }
  var showDeleteAccountDialog by remember { mutableStateOf(false) }
  var showLogoutDialog by remember { mutableStateOf(false) }
  
  AppScaffold(
    title = "Configuración",
    navigationIcon = {
      IconButton(onClick = { navigateToBack() }) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(CreamWarm)
        .padding(paddingValues),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              "Método de eliminación",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = CharcoalWarm
            )
            Spacer(Modifier.height(8.dp))
            
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.setRecycleBinMode(true) }
                .padding(vertical = 8.dp)
            ) {
              RadioButton(
                selected = recycleBinMode,
                onClick = { viewModel.setRecycleBinMode(true) },
                colors = RadioButtonDefaults.colors(selectedColor = SwipeKeep)
              )
              Spacer(Modifier.width(12.dp))
              Column(Modifier.weight(1f)) {
                Text(
                  "Enviar a la papelera",
                  fontWeight = FontWeight.Medium,
                  fontSize = 14.sp,
                  color = CharcoalWarm
                )
                Text(
                  "Las fotos se enviarán a la papelera del sistema (se pueden recuperar)",
                  fontSize = 11.sp,
                  color = BrownMid
                )
              }
            }
            
            HorizontalDivider()
            
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.setRecycleBinMode(false) }
                .padding(vertical = 8.dp)
            ) {
              RadioButton(
                selected = !recycleBinMode,
                onClick = { viewModel.setRecycleBinMode(false) },
                colors = RadioButtonDefaults.colors(selectedColor = SwipeDelete)
              )
              Spacer(Modifier.width(12.dp))
              Column(Modifier.weight(1f)) {
                Text(
                  "Borrar permanentemente",
                  fontWeight = FontWeight.Medium,
                  fontSize = 14.sp,
                  color = CharcoalWarm
                )
                Text(
                  "⚠️ Las fotos se eliminarán definitivamente sin posibilidad de recuperación",
                  fontSize = 11.sp,
                  color = SwipeDelete
                )
              }
            }
          }
        }
      }
      
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              "Gestión de datos",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = CharcoalWarm
            )
            Spacer(Modifier.height(8.dp))
            
            SettingsOption(
              icon = Icons.Default.Favorite,
              title = "Borrar todas las favoritas",
              subtitle = "Eliminará todas las fotos marcadas como favoritas",
              iconColor = SwipeFav,
              onClick = { showClearFavoritesDialog = true }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            SettingsOption(
              icon = Icons.Default.Person,
              title = "Borrar cuenta",
              subtitle = "Eliminará permanentemente tu cuenta y todos los datos",
              iconColor = SwipeDelete,
              onClick = { showDeleteAccountDialog = true }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            SettingsOption(
              icon = Icons.AutoMirrored.Filled.Logout,
              title = "Cerrar sesión",
              subtitle = "Salir de la aplicación",
              iconColor = BrownMid,
              onClick = { showLogoutDialog = true }
            )
          }
        }
      }
    }
  }
  
  if (showClearFavoritesDialog) {
    AlertDialog(
      onDismissRequest = { showClearFavoritesDialog = false },
      title = { Text("Borrar favoritas") },
      text = {
        Text(
          if (recycleBinMode) {
            "¿Estás seguro de que quieres enviar todas las fotos favoritas a la papelera?"
          } else {
            "¿Estás seguro de que quieres borrar permanentemente todas las fotos favoritas? Esta acción no se puede deshacer."
          }
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            viewModel.clearFavorites(context)
            showClearFavoritesDialog = false
          }
        ) {
          Text(
            if (recycleBinMode) "Enviar a papelera" else "Borrar permanentemente",
            color = if (recycleBinMode) SwipeKeep else SwipeDelete
          )
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearFavoritesDialog = false }) {
          Text("Cancelar")
        }
      }
    )
  }
  
  if (showDeleteAccountDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteAccountDialog = false },
      title = { Text("Borrar cuenta") },
      text = { Text("Esta acción eliminará permanentemente tu cuenta y todos tus datos. ¿Estás seguro?") },
      confirmButton = {
        TextButton(onClick = {
          viewModel.deleteAccount()
          showDeleteAccountDialog = false
        }) {
          Text("Borrar cuenta", color = SwipeDelete)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteAccountDialog = false }) {
          Text("Cancelar")
        }
      }
    )
  }
  
  if (showLogoutDialog) {
    AlertDialog(
      onDismissRequest = { showLogoutDialog = false },
      title = { Text("Cerrar sesión") },
      text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
      confirmButton = {
        TextButton(onClick = {
          viewModel.logout()
          showLogoutDialog = false
        }) {
          Text("Cerrar sesión")
        }
      },
      dismissButton = {
        TextButton(onClick = { showLogoutDialog = false }) {
          Text("Cancelar")
        }
      }
    )
  }
}

@Composable
fun SettingsOption(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  iconColor: Color,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
    Spacer(Modifier.width(16.dp))
    Column(Modifier.weight(1f)) {
      Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = CharcoalWarm)
      Text(subtitle, fontSize = 11.sp, color = BrownMid)
    }
    Icon(
      Icons.Default.ChevronRight,
      contentDescription = null,
      tint = BrownLight,
      modifier = Modifier.size(20.dp)
    )
  }
}