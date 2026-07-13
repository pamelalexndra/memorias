package com.pamelapp.memorias.ui.screens.registerscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pamelapp.memorias.ui.scaffold.AppScaffold
import com.pamelapp.memorias.ui.screens.registerscreen.components.CredentialsSection
import com.pamelapp.memorias.ui.screens.registerscreen.components.PasswordStrengthMeter
import com.pamelapp.memorias.ui.screens.registerscreen.components.PersonalInfoSection
import com.pamelapp.memorias.ui.theme.BrownMid
import com.pamelapp.memorias.ui.theme.CreamWarm
import com.pamelapp.memorias.ui.theme.Siena

@Composable
fun RegisterScreen(
  navigateToBack: () -> Unit,
  viewModel: RegisterViewModel = viewModel(
    factory = RegisterViewModel.provideFactory()
  )
) {
  
  val isLoading by viewModel.isLoading.collectAsState()
  val email by viewModel.email.collectAsState()
  val username by viewModel.username.collectAsState()
  val password by viewModel.password.collectAsState()
  val confirmPassword by viewModel.confirmPassword.collectAsState()
  val emailAvailable by viewModel.emailAvailable.collectAsState()
  val usernameAvailable by viewModel.usernameAvailable.collectAsState()
  val error by viewModel.error.collectAsState()

  val isFormValid =
    email.isNotBlank() &&
            email.contains("@") &&
            username.isNotBlank() &&
            username.length >= 3 &&
            password.length >= 6 &&
            password == confirmPassword &&
            emailAvailable != false &&
            usernameAvailable != false
  
  LaunchedEffect(Unit) {
    viewModel.onSuccess.collect { success ->
      if (success) {
        viewModel.resetSuccess()
        navigateToBack()
      }
    }
  }
  
  AppScaffold(
    title = "Registro de usuario",
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
        .padding(paddingValues)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
          .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
      ) {
        PersonalInfoSection(
          email = email,
          emailAvailable = emailAvailable,
          onEmailChange = { viewModel.updateEmail(it) }
        )
        
        CredentialsSection(
          username = username,
          password = password,
          confirmPassword = confirmPassword,
          usernameAvailable = usernameAvailable,
          onUsernameChange = { viewModel.updateUsername(it) },
          onPasswordChange = { viewModel.updatePassword(it) },
          onConfirmPasswordChange = { viewModel.updateConfirmPassword(it) }
        )
        
        if (password.isNotEmpty()) {
          PasswordStrengthMeter(password = password)
        }
        
        if (error != null) {
          Text(
            text = error!!,
            color = Color.Red,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
        }

        if (!isFormValid && error == null) {
          Text(
            text = when {
              email.isBlank() || !email.contains("@") -> "Ingresa un correo válido."
              username.length < 3 -> "El usuario debe tener al menos 3 caracteres."
              password.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
              password != confirmPassword -> "Las contraseñas no coinciden."
              emailAvailable == false -> "Ese correo no está disponible."
              usernameAvailable == false -> "Ese usuario no está disponible."
              else -> ""
            },
            color = BrownMid,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
        }

        Button(
          onClick = { viewModel.onRegisterClick() },
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isFormValid && !isLoading) Siena else BrownMid
          ),
          enabled = isFormValid && !isLoading
        ) {
          if (isLoading == true) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
          } else {
            Text("Registrar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
          }
        }
        
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center
        ) {
          Text(text = "¿Ya tienes cuenta? ", fontSize = 14.sp, color = BrownMid)
          Text(
            text = "Inicia sesión aquí",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Siena,
            modifier = Modifier.clickable { navigateToBack() }
          )
        }
      }
    }
  }
}