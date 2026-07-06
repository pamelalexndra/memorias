package com.example.pamelapp

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import androidx.core.content.edit
import com.example.pamelapp.ui.navigation.MemoriasApp

/*
private val Siena = Color(0xFFC2743A)
private val CreamWarm = Color(0xFFFDF6ED)
private val SandSoft = Color(0xFFF5E8D5)
private val CharcoalWarm = Color(0xFF1F1A14)
private val BrownMid = Color(0xFF8C7A6B)
private val BrownLight = Color(0xFFD4C4B0)
private val SwipeKeep = Color(0xFF22C55E)
private val SwipeDelete = Color(0xFFEF4444)
private val SwipeFav = Color(0xFFE8527A)

// MODELO
data class Photo(
  val id: Long,
  val uri: Uri,
  val dateTaken: Long,
  val year: Int,
  val sizeBytes: Long,
  val displayName: String,
)

enum class Screen { SWIPE, DELETE_CONFIRM, DONE, FAVORITES, SETTINGS }

data class UiState(
  val loading: Boolean = true,
  val permissionDenied: Boolean = false,
  val photos: List<Photo> = emptyList(),     // todas las fotos del día
  val favoritePhotos: List<Photo> = emptyList(), // fotos favoritas
  val currentIndex: Int = 0,
  val pendingDelete: List<Photo> = emptyList(),
  val favorited: Int = 0,
  val screen: Screen = Screen.SWIPE,
  val deletedCount: Int = 0,
  val freedBytes: Long = 0L,
  val error: String? = null,
  val recycleBinMode: Boolean = false, // false = borrar permanentemente, true = enviar a papelera
)

// VIEWMODEL
class MemoriasViewModel : ViewModel() {
  
  private val _state = MutableStateFlow(UiState())
  val state: StateFlow<UiState> = _state.asStateFlow()
  
  // SharedPreferences para guardar favoritos y configuración
  private val prefsKey = "memorias_prefs"
  private val favoritesKey = "favorites"
  
  @RequiresApi(Build.VERSION_CODES.O)
  fun loadPhotos(contentResolver: ContentResolver) {
    viewModelScope.launch {
      _state.update { it.copy(loading = true, error = null) }
      try {
        val photos = queryOnThisDayPhotos(contentResolver)
        _state.update { it.copy(loading = false, photos = photos, currentIndex = 0) }
      } catch (e: Exception) {
        _state.update { it.copy(loading = false, error = e.message) }
      }
    }
  }
  
  fun loadFavorites(context: android.content.Context) {
    viewModelScope.launch {
      val prefs = context.getSharedPreferences(prefsKey, android.content.Context.MODE_PRIVATE)
      val favoriteIds = prefs.getStringSet(favoritesKey, emptySet()) ?: emptySet()
      val favoritePhotos = _state.value.photos.filter { favoriteIds.contains(it.id.toString()) }
      _state.update { it.copy(favoritePhotos = favoritePhotos) }
    }
  }
  
  fun toggleFavorite(photo: Photo, context: android.content.Context) {
    val prefs = context.getSharedPreferences(prefsKey, android.content.Context.MODE_PRIVATE)
    val favoriteIds = prefs.getStringSet(favoritesKey, emptySet())?.toMutableSet() ?: mutableSetOf()
    
    if (favoriteIds.contains(photo.id.toString())) {
      favoriteIds.remove(photo.id.toString())
    } else {
      favoriteIds.add(photo.id.toString())
      _state.update { it.copy(favorited = it.favorited + 1) }
    }
    
    prefs.edit { putStringSet(favoritesKey, favoriteIds) }
    loadFavorites(context)
  }
  
  fun isFavorite(photo: Photo, context: android.content.Context): Boolean {
    val prefs = context.getSharedPreferences(prefsKey, android.content.Context.MODE_PRIVATE)
    val favoriteIds = prefs.getStringSet(favoritesKey, emptySet()) ?: emptySet()
    return favoriteIds.contains(photo.id.toString())
  }
  
  fun onKeep() = advancePhoto()
  
  fun onDelete() {
    val photo = currentPhoto() ?: return
    _state.update { it.copy(pendingDelete = it.pendingDelete + photo) }
    advancePhoto()
  }
  
  fun onFavorite(context: android.content.Context) {
    val photo = currentPhoto() ?: return
    toggleFavorite(photo, context)
    advancePhoto()
  }
  
  fun showDeleteConfirm() {
    if (_state.value.pendingDelete.isEmpty()) return
    _state.update { it.copy(screen = Screen.DELETE_CONFIRM) }
  }
  
  fun backToSwipe() {
    _state.update { it.copy(screen = Screen.SWIPE) }
  }
  
  fun removeFromQueue(photo: Photo) {
    _state.update { it.copy(pendingDelete = it.pendingDelete - photo) }
  }
  
  fun onDeleteSuccess() {
    val deleted = _state.value.pendingDelete
    val freed = deleted.sumOf { it.sizeBytes }
    _state.update {
      it.copy(
        pendingDelete = emptyList(),
        screen = Screen.DONE,
        deletedCount = it.deletedCount + deleted.size,
        freedBytes = it.freedBytes + freed,
      )
    }
  }
  
  fun reset() {
    _state.update { UiState(loading = false, photos = it.photos) }
  }
  
  fun setPermissionDenied() {
    _state.update { it.copy(loading = false, permissionDenied = true) }
  }
  
  fun navigateToFavorites() {
    _state.update { it.copy(screen = Screen.FAVORITES) }
  }
  
  fun navigateToSettings() {
    _state.update { it.copy(screen = Screen.SETTINGS) }
  }
  
  fun setRecycleBinMode(enabled: Boolean) {
    _state.update { it.copy(recycleBinMode = enabled) }
  }
  
  fun deletePhotosPermanently(
    photos: List<Photo>,
    contentResolver: ContentResolver,
    onComplete: () -> Unit
  ) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        photos.forEach { photo ->
          contentResolver.delete(photo.uri, null, null)
        }
      }
      onComplete()
    }
  }
  
  fun moveToRecycleBin(
    photos: List<Photo>,
    contentResolver: ContentResolver,
    onComplete: () -> Unit
  ) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          val uris = photos.map { it.uri }
          MediaStore.createTrashRequest(contentResolver, uris, true)
        } else {
          // Versiones anteriores, mover a una carpeta de papelera
          photos.forEach { photo ->
            contentResolver.delete(photo.uri, null, null)
          }
        }
      }
      onComplete()
    }
  }
  
  // Actualizar en el ViewModel
  fun deleteFavorites(
    favorites: List<Photo>,
    contentResolver: ContentResolver,
    recycleBinMode: Boolean,
    onComplete: () -> Unit
  ) {
    viewModelScope.launch {
      val freed = favorites.sumOf { it.sizeBytes }
      withContext(Dispatchers.IO) {
        if (recycleBinMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          // Enviar a papelera
          val uris = favorites.map { it.uri }
          MediaStore.createTrashRequest(contentResolver, uris, true)
        } else {
          // Borrar permanentemente
          favorites.forEach { photo ->
            contentResolver.delete(photo.uri, null, null)
          }
        }
      }
      _state.update {
        it.copy(
          deletedCount = it.deletedCount + favorites.size,
          freedBytes = it.freedBytes + freed,
          favoritePhotos = emptyList()
        )
      }
      onComplete()
    }
  }
  
  fun clearFavorites(context: android.content.Context) {
    val prefs = context.getSharedPreferences(prefsKey, android.content.Context.MODE_PRIVATE)
    prefs.edit { remove(favoritesKey) }
    _state.update { it.copy(favoritePhotos = emptyList(), favorited = 0) }
  }
  
  fun logout() {
    // Limpiar datos de sesión
    _state.update {
      UiState(
        loading = false,
        photos = it.photos,
        screen = Screen.SWIPE
      )
    }
  }
  
  private fun advancePhoto() {
    _state.update { s ->
      val next = s.currentIndex + 1
      if (next >= s.photos.size) {
        if (s.pendingDelete.isNotEmpty()) {
          s.copy(screen = Screen.DELETE_CONFIRM)
        } else {
          s.copy(screen = Screen.DONE)
        }
      } else {
        s.copy(currentIndex = next)
      }
    }
  }
  
  fun currentPhoto(): Photo? {
    val s = _state.value
    return s.photos.getOrNull(s.currentIndex)
  }
  
  // ── MediaStore query ──────────────────────────────────────────────────────
  @RequiresApi(Build.VERSION_CODES.O)
  private suspend fun queryOnThisDayPhotos(cr: ContentResolver): List<Photo> =
    withContext(Dispatchers.IO) {
      val today = LocalDate.now()
      val thisMonth = today.monthValue
      val thisDay = today.dayOfMonth
      val thisYear = today.year
      
      val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATE_TAKEN,
        MediaStore.Images.Media.SIZE,
      )
      
      val results = mutableListOf<Photo>()
      
      cr.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        "${MediaStore.Images.Media.DATE_TAKEN} IS NOT NULL",
        null,
        "${MediaStore.Images.Media.DATE_TAKEN} DESC",
      )?.use { cursor ->
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
        val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
        val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
        
        while (cursor.moveToNext()) {
          val dateTaken = cursor.getLong(dateCol)
          val local = Instant.ofEpochMilli(dateTaken).atZone(ZoneId.systemDefault()).toLocalDate()
          
          if (local.monthValue != thisMonth) continue
          if (local.dayOfMonth != thisDay) continue
          if (local.year >= thisYear) continue
          
          val id = cursor.getLong(idCol)
          val uri = ContentUris.withAppendedId(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
          )
          results.add(
            Photo(
              id = id,
              uri = uri,
              dateTaken = dateTaken,
              year = local.year,
              sizeBytes = cursor.getLong(sizeCol),
              displayName = cursor.getString(nameCol) ?: "foto",
            )
          )
        }
      }
      
      results
    }
}
*/
// ACTIVITY
class MainActivity : ComponentActivity() {
  @RequiresApi(Build.VERSION_CODES.R)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme() {
        MemoriasApp()
      }
    }
  }
}
/*
// ROOT COMPOSABLE — maneja permisos y navegación entre pantallas
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun MemoriasApp(vm: MemoriasViewModel = viewModel()) {
  val state by vm.state.collectAsState()
  val context = LocalContext.current
  val cr = context.contentResolver
  
  val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
    Manifest.permission.READ_MEDIA_IMAGES
  else
    Manifest.permission.READ_EXTERNAL_STORAGE
  
  val permLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { granted ->
    if (granted) {
      vm.loadPhotos(cr)
      vm.loadFavorites(context)
    } else vm.setPermissionDenied()
  }
  
  val deleteLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.StartIntentSenderForResult()
  ) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
      vm.onDeleteSuccess()
    }
  }
  
  LaunchedEffect(Unit) {
    val alreadyGranted = ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED
    if (alreadyGranted) {
      vm.loadPhotos(cr)
      vm.loadFavorites(context)
    } else permLauncher.launch(permission)
  }
  
  when {
    state.permissionDenied -> PermissionDeniedScreen()
    
    state.loading -> LoadingScreen()
    
    state.screen == Screen.SWIPE -> SwipeScreen(
      state = state,
      onKeep = vm::onKeep,
      onDelete = vm::onDelete,
      onFavorite = { vm.onFavorite(context) },
      onReviewDelete = vm::showDeleteConfirm,
      onFavoritesClick = vm::navigateToFavorites,
      onSettingsClick = vm::navigateToSettings,
    )
    
    state.screen == Screen.FAVORITES -> FavoritesScreen(
      favorites = state.favoritePhotos,
      onBack = vm::backToSwipe,
      onDeleteSelected = { photos ->
        if (state.recycleBinMode) {
          vm.moveToRecycleBin(photos, cr) {
            vm.loadFavorites(context)
          }
        } else {
          vm.deletePhotosPermanently(photos, cr) {
            vm.loadFavorites(context)
          }
        }
      },
      recycleBinMode = state.recycleBinMode,
    )
    
    state.screen == Screen.SETTINGS -> SettingsScreen(
      recycleBinMode = state.recycleBinMode,
      onRecycleBinModeChange = vm::setRecycleBinMode,
      onClearFavorites = { vm.clearFavorites(context) },
      onDeleteAccount = {
        // Implementar lógica de borrado de cuenta
        vm.logout()
      },
      onLogout = vm::logout,
      onBack = vm::backToSwipe,
    )
    
    state.screen == Screen.DELETE_CONFIRM -> DeleteConfirmScreen(
      photos = state.pendingDelete,
      onRemove = vm::removeFromQueue,
      onCancel = vm::backToSwipe,
      onConfirm = {
        val uris = state.pendingDelete.map { it.uri }
        val pendingIntent = MediaStore.createDeleteRequest(cr, uris)
        deleteLauncher.launch(
          IntentSenderRequest.Builder(pendingIntent.intentSender).build()
        )
      },
    )
    
    state.screen == Screen.DONE -> DoneScreen(
      deleted = state.deletedCount,
      favorited = state.favorited,
      reviewed = state.photos.size,
      freedBytes = state.freedBytes,
      onRepeat = vm::reset,
    )
  }
}

// PANTALLA PRINCIPAL — SWIPE
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SwipeScreen(
  state: UiState,
  onKeep: () -> Unit,
  onDelete: () -> Unit,
  onFavorite: () -> Unit,
  onReviewDelete: () -> Unit,
  onFavoritesClick: () -> Unit,
  onSettingsClick: () -> Unit,
) {
  val total = state.photos.size
  val current = state.currentIndex + 1
  val photo = state.photos.getOrNull(state.currentIndex)
  
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CreamWarm)
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      SwipeHeader(
        current = current,
        total = total,
        pendingDeleteCount = state.pendingDelete.size,
        favoritedCount = state.favorited,
        onReviewDelete = onReviewDelete,
        onFavoritesClick = onFavoritesClick,
        onSettingsClick = onSettingsClick,
      )
      
      photo?.let {
        Text(
          text = "Hace ${LocalDate.now().year - it.year} año${if (LocalDate.now().year - it.year != 1) "s" else ""} · ${it.year}",
          color = BrownMid,
          fontSize = 13.sp,
          modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
        )
      }
      
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
      ) {
        if (photo != null) {
          key(state.currentIndex) {
            SwipeablePhotoCard(
              photo = photo,
              onKeep = onKeep,
              onDelete = onDelete,
              onFavorite = onFavorite,
            )
          }
        } else {
          EmptyDayCard()
        }
      }
      
      if (photo != null) {
        ActionButtons(
          onKeep = onKeep,
          onFavorite = onFavorite,
          onDelete = onDelete,
        )
      }
      
      Text(
        text = "← conservar   ↑ favorita   borrar →",
        color = BrownLight,
        fontSize = 11.sp,
        modifier = Modifier.padding(vertical = 12.dp),
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeHeader(
  current: Int,
  total: Int,
  pendingDeleteCount: Int,
  favoritedCount: Int,
  onReviewDelete: () -> Unit,
  onFavoritesClick: () -> Unit,
  onSettingsClick: () -> Unit,
) {
  Surface(
    tonalElevation = 2.dp,
    color = Color.White,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Memorias",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = Siena,
      )
      
      Spacer(Modifier.weight(1f))
      
      // Botón de favoritos
      IconButton(onClick = onFavoritesClick) {
        Icon(
          Icons.Default.Favorite,
          contentDescription = "Favoritos",
          tint = SwipeFav,
          modifier = Modifier.size(20.dp)
        )
      }
      
      // Botón de configuración
      IconButton(onClick = onSettingsClick) {
        Icon(
          Icons.Default.Settings,
          contentDescription = "Configuración",
          tint = BrownMid,
          modifier = Modifier.size(20.dp)
        )
      }
      
      if (favoritedCount > 0) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(end = 12.dp),
        ) {
          Icon(
            Icons.Default.Favorite, contentDescription = null,
            tint = SwipeFav, modifier = Modifier.size(14.dp)
          )
          Spacer(Modifier.width(3.dp))
          Text(
            "$favoritedCount",
            color = SwipeFav,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }
      
      if (pendingDeleteCount > 0) {
        Badge(
          containerColor = SwipeDelete,
        ) {
          TextButton(
            onClick = onReviewDelete,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          ) {
            Icon(
              Icons.Default.DeleteSweep, contentDescription = null,
              tint = Color.White, modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
              "$pendingDeleteCount",
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
      
      Spacer(Modifier.width(10.dp))
      
      Text(
        "$current / $total",
        color = BrownMid,
        fontSize = 12.sp,
      )
    }
  }
}

// PANTALLA DE FAVORITOS
@Composable
fun FavoritesScreen(
  favorites: List<Photo>,
  onBack: () -> Unit,
  onDeleteSelected: (List<Photo>) -> Unit,
  recycleBinMode: Boolean,
) {
  var selectedPhotos by remember { mutableStateOf<Set<Photo>>(emptySet()) }
  val context = LocalContext.current
  val totalMb = selectedPhotos.sumOf { it.sizeBytes } / 1_048_576f
  
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(CreamWarm),
  ) {
    Surface(color = Color.White, tonalElevation = 2.dp) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Siena)
        }
        Column(Modifier.weight(1f)) {
          Text(
            "Tus favoritos",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = CharcoalWarm,
          )
          if (selectedPhotos.isNotEmpty()) {
            Text(
              "${selectedPhotos.size} fotos seleccionadas · ${"%.1f".format(totalMb)} MB",
              color = BrownMid,
              fontSize = 12.sp,
            )
          }
        }
        if (selectedPhotos.isNotEmpty()) {
          Button(
            onClick = { onDeleteSelected(selectedPhotos.toList()) },
            colors = ButtonDefaults.buttonColors(containerColor = SwipeDelete),
            modifier = Modifier.height(36.dp),
          ) {
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
    
    if (favorites.isEmpty()) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            Icons.Default.FavoriteBorder,
            contentDescription = null,
            tint = BrownLight,
            modifier = Modifier.size(64.dp)
          )
          Spacer(Modifier.height(16.dp))
          Text(
            "No tienes fotos favoritas aún",
            color = BrownMid,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
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
          .weight(1f)
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
              modifier = Modifier.fillMaxSize(),
            )
            
            // Overlay de selección
            if (isSelected) {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(SwipeFav.copy(alpha = 0.3f)),
              )
            }
            
            // Checkbox de selección
            Checkbox(
              checked = isSelected,
              onCheckedChange = { checked ->
                selectedPhotos = if (checked) {
                  selectedPhotos + photo
                } else {
                  selectedPhotos - photo
                }
              },
              modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
                .size(24.dp),
              colors = CheckboxDefaults.colors(
                checkedColor = SwipeFav,
                uncheckedColor = Color.White,
              )
            )
            
            // Año
            Box(
              modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp)
                .background(
                  Color.Black.copy(alpha = 0.55f),
                  RoundedCornerShape(4.dp),
                )
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

// PANTALLA DE CONFIGURACIÓN - CORREGIDA
@Composable
fun SettingsScreen(
  recycleBinMode: Boolean,
  onRecycleBinModeChange: (Boolean) -> Unit,
  onClearFavorites: () -> Unit,
  onDeleteAccount: () -> Unit,
  onLogout: () -> Unit,
  onBack: () -> Unit,
) {
  var showClearFavoritesDialog by remember { mutableStateOf(false) }
  var showDeleteAccountDialog by remember { mutableStateOf(false) }
  var showLogoutDialog by remember { mutableStateOf(false) }
  
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(CreamWarm)
  ) {
    Surface(color = Color.White, tonalElevation = 2.dp) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Siena)
        }
        Text(
          "Configuración",
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp,
          color = CharcoalWarm,
          modifier = Modifier.padding(start = 8.dp)
        )
      }
    }
    
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
          Column(
            modifier = Modifier.padding(16.dp)
          ) {
            Text(
              "Método de eliminación",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = CharcoalWarm
            )
            Spacer(Modifier.height(8.dp))
            
            // Radio button para enviar a papelera
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onRecycleBinModeChange(true) }
                .padding(vertical = 8.dp)
            ) {
              RadioButton(
                selected = recycleBinMode,
                onClick = { onRecycleBinModeChange(true) },
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
            
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            
            // Radio button para borrar permanentemente
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onRecycleBinModeChange(false) }
                .padding(vertical = 8.dp)
            ) {
              RadioButton(
                selected = !recycleBinMode,
                onClick = { onRecycleBinModeChange(false) },
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
          Column(
            modifier = Modifier.padding(16.dp)
          ) {
            Text(
              "Gestión de datos",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = CharcoalWarm
            )
            Spacer(Modifier.height(8.dp))
            
            // Opción: Borrar todas las favoritas
            SettingsOption(
              icon = Icons.Default.Favorite,
              title = "Borrar todas las favoritas",
              subtitle = "Eliminará todas las fotos marcadas como favoritas",
              iconColor = SwipeFav,
              onClick = { showClearFavoritesDialog = true }
            )
            
            HorizontalDivider(
              modifier = Modifier.padding(vertical = 8.dp),
              thickness = DividerDefaults.Thickness,
              color = DividerDefaults.color
            )
            
            // Opción: Borrar cuenta
            SettingsOption(
              icon = Icons.Default.Person,
              title = "Borrar cuenta",
              subtitle = "Eliminará permanentemente tu cuenta y todos los datos",
              iconColor = SwipeDelete,
              onClick = { showDeleteAccountDialog = true }
            )
            
            HorizontalDivider(
              modifier = Modifier.padding(vertical = 8.dp),
              thickness = DividerDefaults.Thickness,
              color = DividerDefaults.color
            )
            
            // Opción: Cerrar sesión
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
  
  // Diálogos de confirmación
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
            onClearFavorites()
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
        TextButton(
          onClick = {
            onDeleteAccount()
            showDeleteAccountDialog = false
          }
        ) {
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
        TextButton(
          onClick = {
            onLogout()
            showLogoutDialog = false
          }
        ) {
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
    Icon(
      icon,
      contentDescription = null,
      tint = iconColor,
      modifier = Modifier.size(24.dp)
    )
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

// TARJETA DESLIZABLE
@Composable
fun SwipeablePhotoCard(
  photo: Photo,
  onKeep: () -> Unit,
  onDelete: () -> Unit,
  onFavorite: () -> Unit,
) {
  val offsetX = remember { Animatable(0f) }
  val offsetY = remember { Animatable(0f) }
  val threshold = 160f
  val scope = rememberCoroutineScope()
  
  val overlayColor = when {
    offsetX.value > 80f -> SwipeDelete.copy(alpha = (offsetX.value / threshold).coerceIn(0f, 0.5f))
    offsetX.value < -80f -> SwipeKeep.copy(
      alpha = ((-offsetX.value) / threshold).coerceIn(
        0f,
        0.5f
      )
    )
    
    offsetY.value < -80f -> SwipeFav.copy(alpha = ((-offsetY.value) / threshold).coerceIn(0f, 0.5f))
    else -> Color.Transparent
  }
  
  val overlayIcon = when {
    offsetX.value > 80f -> Icons.Default.Delete
    offsetX.value < -80f -> Icons.Default.Check
    offsetY.value < -80f -> Icons.Default.Favorite
    else -> null
  }
  
  fun triggerAction(action: () -> Unit) {
    scope.launch {
      action()
    }
  }
  
  Box(
    modifier = Modifier
      .fillMaxSize()
      .graphicsLayer {
        translationX = offsetX.value
        translationY = offsetY.value
        rotationZ = (offsetX.value / 20f).coerceIn(-15f, 15f)
      }
      .pointerInput(Unit) {
        detectDragGestures(
          onDragEnd = {
            scope.launch {
              when {
                offsetX.value > threshold -> {
                  offsetX.animateTo(1200f, spring(stiffness = 800f))
                  triggerAction(onDelete)
                }
                
                offsetX.value < -threshold -> {
                  offsetX.animateTo(-1200f, spring(stiffness = 800f))
                  triggerAction(onKeep)
                }
                
                offsetY.value < -threshold -> {
                  offsetY.animateTo(-1200f, spring(stiffness = 800f))
                  triggerAction(onFavorite)
                }
                
                else -> {
                  launch { offsetX.animateTo(0f, spring(stiffness = 400f)) }
                  launch { offsetY.animateTo(0f, spring(stiffness = 400f)) }
                }
              }
            }
          },
          onDrag = { change, drag ->
            change.consume()
            scope.launch {
              offsetX.snapTo(offsetX.value + drag.x)
              offsetY.snapTo(offsetY.value + drag.y)
            }
          },
        )
      }
      .clip(RoundedCornerShape(20.dp)),
  ) {
    AsyncImage(
      model = photo.uri,
      contentDescription = photo.displayName,
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize(),
    )
    
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(overlayColor),
    )
    
    overlayIcon?.let { icon ->
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        Surface(
          shape = CircleShape,
          color = Color.White.copy(alpha = 0.9f),
          modifier = Modifier.size(72.dp),
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = when {
              offsetX.value > 80f -> SwipeDelete
              offsetX.value < -80f -> SwipeKeep
              else -> SwipeFav
            },
            modifier = Modifier
              .padding(16.dp)
              .fillMaxSize(),
          )
        }
      }
    }
    
    Box(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(12.dp)
        .background(
          Color.Black.copy(alpha = 0.6f),
          RoundedCornerShape(8.dp),
        )
        .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
      Text(
        text = "${photo.year}",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
      )
    }
  }
}

// BOTONES DE ACCIÓN
@Composable
fun ActionButtons(
  onKeep: () -> Unit,
  onFavorite: () -> Unit,
  onDelete: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 48.dp, vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    ActionButton(
      onClick = onKeep,
      icon = Icons.Default.Check,
      color = SwipeKeep,
      size = 56,
    )
    ActionButton(
      onClick = onFavorite,
      icon = Icons.Default.Favorite,
      color = SwipeFav,
      size = 48,
    )
    ActionButton(
      onClick = onDelete,
      icon = Icons.Default.Delete,
      color = SwipeDelete,
      size = 56,
    )
  }
}

@Composable
fun ActionButton(
  onClick: () -> Unit,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  color: Color,
  size: Int,
) {
  Surface(
    onClick = onClick,
    shape = CircleShape,
    color = color.copy(alpha = 0.12f),
    modifier = Modifier.size(size.dp),
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = color,
        modifier = Modifier.size((size * 0.45).dp),
      )
    }
  }
}

// PANTALLA DE CONFIRMACIÓN DE BORRADO
@Composable
fun DeleteConfirmScreen(
  photos: List<Photo>,
  onRemove: (Photo) -> Unit,
  onCancel: () -> Unit,
  onConfirm: () -> Unit,
) {
  val totalMb = photos.sumOf { it.sizeBytes } / 1_048_576f
  
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(CreamWarm),
  ) {
    Surface(color = Color.White, tonalElevation = 2.dp) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(onClick = onCancel) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Siena)
        }
        Column(Modifier.weight(1f)) {
          Text(
            "Confirmar eliminación",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = CharcoalWarm,
          )
          Text(
            "${photos.size} fotos · ${"%.1f".format(totalMb)} MB",
            color = BrownMid,
            fontSize = 12.sp,
          )
        }
      }
    }
    
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
            modifier = Modifier.fillMaxSize(),
          )
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(SwipeDelete.copy(alpha = 0.25f)),
          )
          Surface(
            onClick = { onRemove(photo) },
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
              modifier = Modifier.padding(4.dp),
            )
          }
          Box(
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(4.dp)
              .background(
                Color.Black.copy(alpha = 0.55f),
                RoundedCornerShape(4.dp),
              )
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
      onClick = onConfirm,
      colors = ButtonDefaults.buttonColors(containerColor = SwipeDelete),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .height(52.dp),
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

// PANTALLA DE SESIÓN COMPLETADA
@Composable
fun DoneScreen(
  deleted: Int,
  favorited: Int,
  reviewed: Int,
  freedBytes: Long,
  onRepeat: () -> Unit,
) {
  val freedMb = freedBytes / 1_048_576f
  
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(CreamWarm)
      .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text("🎉", fontSize = 56.sp)
    Spacer(Modifier.height(16.dp))
    Text(
      "¡Sesión completa!",
      fontWeight = FontWeight.Bold,
      fontSize = 24.sp,
      color = CharcoalWarm,
    )
    Spacer(Modifier.height(8.dp))
    Text(
      "Revisaste $reviewed foto${if (reviewed != 1) "s" else ""} de hoy en años pasados.",
      color = BrownMid,
      textAlign = TextAlign.Center,
    )
    
    Spacer(Modifier.height(28.dp))
    
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      StatCard("Eliminadas", "$deleted", SwipeDelete, Modifier.weight(1f))
      StatCard("Favoritas", "$favorited", SwipeFav, Modifier.weight(1f))
    }
    
    if (freedBytes > 0) {
      Spacer(Modifier.height(10.dp))
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = SwipeKeep.copy(alpha = 0.12f),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(Icons.Default.Storage, contentDescription = null, tint = SwipeKeep)
          Spacer(Modifier.width(10.dp))
          Column {
            Text("Espacio liberado", color = BrownMid, fontSize = 12.sp)
            Text(
              "${"%.1f".format(freedMb)} MB",
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
              color = SwipeKeep,
            )
          }
        }
      }
    }
    
    Spacer(Modifier.height(32.dp))
    Button(
      onClick = onRepeat,
      colors = ButtonDefaults.buttonColors(containerColor = Siena),
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp),
      shape = RoundedCornerShape(14.dp),
    ) {
      Icon(Icons.Default.Refresh, contentDescription = null)
      Spacer(Modifier.width(8.dp))
      Text("Empezar de nuevo", fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
  }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = color.copy(alpha = 0.12f),
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(value, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = color)
      Text(label, color = BrownMid, fontSize = 12.sp)
    }
  }
}

@Composable
fun LoadingScreen() {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CreamWarm),
    contentAlignment = Alignment.Center,
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      CircularProgressIndicator(color = Siena)
      Spacer(Modifier.height(16.dp))
      Text("Buscando tus recuerdos...", color = BrownMid)
    }
  }
}

@Composable
fun EmptyDayCard() {
  Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text("📷", fontSize = 48.sp)
    Spacer(Modifier.height(16.dp))
    Text(
      "No hay fotos de\neste día en años anteriores",
      color = BrownMid,
      textAlign = TextAlign.Center,
      fontSize = 16.sp,
    )
  }
}

@Composable
fun PermissionDeniedScreen() {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CreamWarm),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier = Modifier.padding(32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Icon(
        Icons.Default.PhotoLibrary,
        contentDescription = null,
        tint = BrownLight,
        modifier = Modifier.size(64.dp),
      )
      Spacer(Modifier.height(16.dp))
      Text(
        "Necesitamos acceso a tu galería",
        fontWeight = FontWeight.Bold,
        color = CharcoalWarm,
        textAlign = TextAlign.Center,
        fontSize = 18.sp,
      )
      Spacer(Modifier.height(8.dp))
      Text(
        "Ve a Ajustes → Aplicaciones → Memorias → Permisos → Imágenes y actívalo.",
        color = BrownMid,
        textAlign = TextAlign.Center,
      )
    }
  }
}

 */