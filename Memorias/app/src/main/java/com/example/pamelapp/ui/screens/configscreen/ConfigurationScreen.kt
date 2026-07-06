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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.pamelapp.domain.model.DeleteMode
import com.example.pamelapp.ui.scaffold.AppScaffold
import com.example.pamelapp.ui.theme.BrownLight
import com.example.pamelapp.ui.theme.BrownMid
import com.example.pamelapp.ui.theme.CharcoalWarm
import com.example.pamelapp.ui.theme.CreamWarm
import com.example.pamelapp.ui.theme.SwipeDelete
import com.example.pamelapp.ui.theme.SwipeFav
import com.example.pamelapp.ui.theme.SwipeKeep

@Composable
fun ConfigurationScreen(
  navigateToBack: () -> Unit,
  viewModel: ConfigurationViewModel = viewModel()
) {
  val deleteMode by viewModel.deleteMode.collectAsState()
  val showSwipeButtons by viewModel.showSwipeButtons.collectAsState()

  val recycleBinMode = deleteMode == DeleteMode.TRASH
  val context = LocalContext.current

  var showClearFavoritesDialog by remember { mutableStateOf(false) }
  var showDeleteAccountDialog by remember { mutableStateOf(false) }
  var showLogoutDialog by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    viewModel.loadSettings(context)
  }

  AppScaffold(
    title = "Configuración",
    navigationIcon = {
      IconButton(onClick = navigateToBack) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Volver",
          tint = Color.White
        )
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
              text = "Método de eliminación",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = CharcoalWarm
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  viewModel.setDeleteMode(context, DeleteMode.TRASH)
                }
                .padding(vertical = 8.dp)
            ) {
              RadioButton(
                selected = recycleBinMode,
                onClick = {
                  viewModel.setDeleteMode(context, DeleteMode.TRASH)
                },
                colors = RadioButtonDefaults.colors(selectedColor = SwipeKeep)
              )

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Enviar a la papelera",
                  fontWeight = FontWeight.Medium,
                  fontSize = 14.sp,
                  color = CharcoalWarm
                )

                Text(
                  text = "Las fotos se enviarán a la papelera del sistema y se pueden recuperar.",
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
                .clickable {
                  viewModel.setDeleteMode(context, DeleteMode.PERMANENT)
                }
                .padding(vertical = 8.dp)
            ) {
              RadioButton(
                selected = !recycleBinMode,
                onClick = {
                  viewModel.setDeleteMode(context, DeleteMode.PERMANENT)
                },
                colors = RadioButtonDefaults.colors(selectedColor = SwipeDelete)
              )

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Borrar permanentemente",
                  fontWeight = FontWeight.Medium,
                  fontSize = 14.sp,
                  color = CharcoalWarm
                )

                Text(
                  text = "⚠️ Las fotos se eliminarán definitivamente sin posibilidad de recuperación.",
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
              text = "Experiencia de uso",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = CharcoalWarm
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsSwitchOption(
              title = "Mostrar botones en el swipe",
              subtitle = if (showSwipeButtons) {
                "Ten presente siempre un recordatorio de cómo usar el swipe. Izquierda: conservar - Derecha: eliminar - Arriba: favoritos - Doble toque: revertir acción"
              } else {
                "Los botones están ocultos. Puedes seguir usando gestos y doble toque."
              },
              checked = showSwipeButtons,
              onCheckedChange = { checked ->
                viewModel.setShowSwipeButtons(context, checked)
              }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
              text = "Puedes ocultar los botones cuando ya te acostumbres a usar los gestos.",
              fontSize = 11.sp,
              color = BrownMid
            )
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
              text = "Gestión de datos",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = CharcoalWarm
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsOption(
              icon = Icons.Default.Favorite,
              title = "Quitar todas las favoritas",
              subtitle = "Las fotos seguirán en tu teléfono, solo se quitará la marca de favorita.",
              iconColor = SwipeFav,
              onClick = {
                showClearFavoritesDialog = true
              }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsOption(
              icon = Icons.Default.Person,
              title = "Borrar cuenta",
              subtitle = "Eliminará permanentemente tu cuenta y todos los datos.",
              iconColor = SwipeDelete,
              onClick = {
                showDeleteAccountDialog = true
              }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsOption(
              icon = Icons.AutoMirrored.Filled.Logout,
              title = "Cerrar sesión",
              subtitle = "Salir de la aplicación.",
              iconColor = BrownMid,
              onClick = {
                showLogoutDialog = true
              }
            )
          }
        }
      }
    }
  }

  if (showClearFavoritesDialog) {
    AlertDialog(
      onDismissRequest = {
        showClearFavoritesDialog = false
      },
      title = {
        Text(text = "Quitar favoritas")
      },
      text = {
        Text(
          text = "¿Quieres quitar la marca de favorita de todas las fotos guardadas en la app? Las fotos no se eliminarán del teléfono."
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
            text = "Quitar favoritas",
            color = SwipeFav
          )
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            showClearFavoritesDialog = false
          }
        ) {
          Text(text = "Cancelar")
        }
      }
    )
  }

  if (showDeleteAccountDialog) {
    AlertDialog(
      onDismissRequest = {
        showDeleteAccountDialog = false
      },
      title = {
        Text(text = "Borrar cuenta")
      },
      text = {
        Text(
          text = "Esta acción eliminará permanentemente tu cuenta y todos tus datos. ¿Estás seguro?"
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            viewModel.deleteAccount()
            showDeleteAccountDialog = false
          }
        ) {
          Text(
            text = "Borrar cuenta",
            color = SwipeDelete
          )
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            showDeleteAccountDialog = false
          }
        ) {
          Text(text = "Cancelar")
        }
      }
    )
  }

  if (showLogoutDialog) {
    AlertDialog(
      onDismissRequest = {
        showLogoutDialog = false
      },
      title = {
        Text(text = "Cerrar sesión")
      },
      text = {
        Text(text = "¿Estás seguro de que quieres cerrar sesión?")
      },
      confirmButton = {
        TextButton(
          onClick = {
            viewModel.logout()
            showLogoutDialog = false
          }
        ) {
          Text(text = "Cerrar sesión")
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            showLogoutDialog = false
          }
        ) {
          Text(text = "Cancelar")
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
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = iconColor,
      modifier = Modifier.size(24.dp)
    )

    Spacer(modifier = Modifier.width(16.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = CharcoalWarm
      )

      Text(
        text = subtitle,
        fontSize = 11.sp,
        color = BrownMid
      )
    }

    Icon(
      imageVector = Icons.Default.ChevronRight,
      contentDescription = null,
      tint = BrownLight,
      modifier = Modifier.size(20.dp)
    )
  }
}

@Composable
fun SettingsSwitchOption(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable {
        onCheckedChange(!checked)
      }
      .padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = CharcoalWarm
      )

      Text(
        text = subtitle,
        fontSize = 11.sp,
        color = BrownMid
      )
    }

    Spacer(modifier = Modifier.width(16.dp))

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange
    )
  }
}