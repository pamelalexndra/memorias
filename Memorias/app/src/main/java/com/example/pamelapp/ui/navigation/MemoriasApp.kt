package com.example.pamelapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.pamelapp.ui.screens.configscreen.ConfigurationScreen
import com.example.pamelapp.ui.screens.deletephotoscreen.DeletePhotoScreen
import com.example.pamelapp.ui.screens.favoritesscreen.FavoritesScreen
import com.example.pamelapp.ui.screens.loginscreen.LoginScreen
import com.example.pamelapp.ui.screens.registerscreen.RegisterScreen
import com.example.pamelapp.ui.screens.swipescreen.SwipeScreen
import com.example.pamelapp.ui.screens.swipescreen.SwipeViewModel
import com.example.pamelapp.ui.screens.welcomescreen.WelcomeScreen

@Composable
fun MemoriasApp(
  initialRoute: Routes = Routes.Welcome
) {
  val swipeViewModel: SwipeViewModel = viewModel()
  val backStack = rememberNavBackStack(initialRoute)

  NavDisplay(
    backStack = backStack,
    onBack = {
      backStack.removeLastOrNull()
    },
    entryProvider = entryProvider {
      entry<Routes.Welcome> {
        WelcomeScreen(
          navigateToLogin = {
            backStack.add(Routes.LogIn)
          },
          navigateToRegister = {
            backStack.add(Routes.Register)
          }
        )
      }

      entry<Routes.LogIn> {
        LoginScreen(
          navigateToBack = {
            backStack.removeLastOrNull()
          },
          navigateToRegister = {
            backStack.add(Routes.Register)
          },
          navigateToSwipe = {
            backStack.clear()
            backStack.add(Routes.PhotosScreen)
          }
        )
      }

      entry<Routes.Register> {
        RegisterScreen(
          navigateToBack = {
            backStack.removeLastOrNull()
          }
        )
      }

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
          navigateToLogin = {
            backStack.clear()
            backStack.add(Routes.Welcome)
          }
        )
      }
    }
  )
}