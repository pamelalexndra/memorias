package com.example.pamelapp.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.pamelapp.ui.screens.deletephotoscreen.DeletePhotoScreen
import com.example.pamelapp.ui.screens.favoritesscreen.FavoritesScreen
import com.example.pamelapp.ui.screens.loginscreen.LoginScreen
import com.example.pamelapp.ui.screens.registerscreen.RegisterScreen
import com.example.pamelapp.ui.screens.swipescreen.SwipeScreen
import com.example.pamelapp.ui.screens.welcomescreen.WelcomeScreen
import com.example.pamelapp.ui.screens.configscreen.ConfigurationScreen
import com.example.pamelapp.ui.screens.deletephotoscreen.DeletePhotoViewModel
import com.example.pamelapp.ui.screens.swipescreen.SwipeViewModel

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun MemoriasApp() {
  val backStack = rememberNavBackStack(Routes.Welcome)
  val context = LocalContext.current
  
  // Verificar permisos al inicio
  val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
    Manifest.permission.READ_MEDIA_IMAGES
  else
    Manifest.permission.READ_EXTERNAL_STORAGE
  
  LaunchedEffect(Unit) {
    val alreadyGranted = ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED
    if (alreadyGranted) {
      // Los ViewModels se inicializan en sus respectivas screens
    }
  }
  
  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider = entryProvider {
      entry<Routes.Welcome> {
        WelcomeScreen(
          navigateToLogin = { backStack.add(Routes.LogIn) },
          navigateToRegister = { backStack.add(Routes.Register) }
        )
      }
      entry<Routes.LogIn> {
        LoginScreen(
          navigateToBack = { backStack.removeLastOrNull() },
          navigateToRegister = { backStack.add(Routes.Register) },
          navigateToSwipe = { backStack.add(Routes.PhotosScreen) }
        )
      }
      entry<Routes.Register> {
        RegisterScreen(
          navigateToBack = { backStack.removeLastOrNull() }
        )
      }
      entry<Routes.PhotosScreen> {
        SwipeScreen(
          navigateToBack = { backStack.removeLastOrNull() },
          navigateToDelete = { backStack.add(Routes.DeletePhoto) },
          navigateToFavorites = { backStack.add(Routes.Favorites) },
          navigateToConfiguration = { backStack.add(Routes.Configuration) }
        )
      }
      entry<Routes.Favorites> {
        FavoritesScreen(
          navigateToBack = { backStack.removeLastOrNull() }
        )
      }
      entry<Routes.DeletePhoto> {
        val swipeViewModel: SwipeViewModel = viewModel()
        val deletePhotoViewModel: DeletePhotoViewModel = viewModel()
        
        LaunchedEffect(Unit) {
          deletePhotoViewModel.updatePendingDelete(swipeViewModel.getPendingDelete())
        }
        DeletePhotoScreen(
          navigateToBack = { backStack.removeLastOrNull() }
        )
      }
      entry<Routes.Configuration> {
        ConfigurationScreen(
          navigateToBack = { backStack.removeLastOrNull() }
        )
      }
    }
  )
}