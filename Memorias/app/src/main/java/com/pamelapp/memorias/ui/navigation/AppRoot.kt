package com.pamelapp.memorias.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.pamelapp.memorias.ui.screens.loginscreen.LoginScreen
import com.pamelapp.memorias.ui.screens.loginscreen.LoginViewModel
import com.pamelapp.memorias.ui.screens.registerscreen.RegisterScreen
import com.pamelapp.memorias.ui.screens.welcomescreen.WelcomeScreen

@Composable
fun AppRoot(
  openLogin: Boolean = false,
  loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.provideFactory())
) {
  val isLoggedIn by loginViewModel.isLoggedIn.collectAsState(initial = false)
  
  when (isLoggedIn) {
    null -> SplashScreen()      // DataStore todavía cargando
    false -> UnauthenticatedNav(startAtLogin = openLogin) // sin sesión: Welcome/LogIn/Register
    true -> MemoriasApp()         // con sesión: la app completa
  }
}

@Composable
fun SplashScreen() {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    CircularProgressIndicator()
  }
}

@Composable
fun UnauthenticatedNav(startAtLogin: Boolean = false) {
  val backStack = rememberNavBackStack(
    if (startAtLogin) Routes.LogIn else Routes.Welcome
  )
  
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
          navigateToSwipe = {}
        )
      }
      
      entry<Routes.Register> {
        RegisterScreen(
          navigateToBack = { backStack.removeLastOrNull() }
        )
      }
    }
  )
}