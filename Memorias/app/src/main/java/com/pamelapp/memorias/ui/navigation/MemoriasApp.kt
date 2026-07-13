package com.pamelapp.memorias.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.pamelapp.memorias.ui.screens.configscreen.ConfigurationScreen
import com.pamelapp.memorias.ui.screens.deletephotoscreen.DeletePhotoScreen
import com.pamelapp.memorias.ui.screens.favoritesscreen.FavoritesScreen
import com.pamelapp.memorias.ui.screens.loginscreen.LoginScreen
import com.pamelapp.memorias.ui.screens.registerscreen.RegisterScreen
import com.pamelapp.memorias.ui.screens.swipescreen.SwipeScreen
import com.pamelapp.memorias.ui.screens.swipescreen.SwipeViewModel
import com.pamelapp.memorias.ui.screens.welcomescreen.WelcomeScreen

@Composable
fun MemoriasApp(
  initialRoute: Routes = Routes.PhotosScreen
) {
  
  val swipeViewModel: SwipeViewModel = viewModel()
  val backStack = rememberNavBackStack(initialRoute)
  
  NavDisplay(
    backStack = backStack,
    onBack = {
      backStack.removeLastOrNull()
    },
    entryProvider = entryProvider {
      
      entry<Routes.PhotosScreen> {
        SwipeScreen(
          navigateToBack = {
            backStack.removeLastOrNull()
          },
          navigateToDelete = {
            backStack.add(Routes.DeletePhoto)
          },
          navigateToFavorites = {
            backStack.add(Routes.Favorites)
          },
          navigateToConfiguration = {
            backStack.add(Routes.Configuration)
          },
          swipeViewModel = swipeViewModel
        )
      }
      
      entry<Routes.Favorites> {
        FavoritesScreen(
          navigateToBack = {
            backStack.removeLastOrNull()
          }
        )
      }
      
      entry<Routes.DeletePhoto> {
        DeletePhotoScreen(
          navigateToBack = {
            backStack.removeLastOrNull()
          },
          viewModel = swipeViewModel
        )
      }
      
      entry<Routes.Configuration> {
        ConfigurationScreen(
          navigateToBack = {
            backStack.removeLastOrNull()
          },
          navigateToLogin = {}
        )
      }
    }
  )
}