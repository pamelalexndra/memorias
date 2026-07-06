package com.example.pamelapp.ui.screens.loginscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pamelapp.ui.scaffold.AppScaffold
import com.example.pamelapp.ui.theme.*

@Composable
fun LoginScreen(
  navigateToBack: () -> Unit,
  navigateToRegister: () -> Unit,
  navigateToSwipe: () -> Unit,
  viewModel: LoginViewModel = viewModel()
) {
  
  val isLoading by viewModel.isLoading.collectAsState()
  val username by viewModel.username.collectAsState()
  val password by viewModel.password.collectAsState()
  val error by viewModel.error.collectAsState()
  var showPassword by remember { mutableStateOf(false) }
  val context = LocalContext.current
  
  LaunchedEffect(Unit) {
    viewModel.onSuccess.collect { success ->
      if (success) navigateToSwipe()
    }
  }
  
  AppScaffold(
    title = "Iniciar sesión",
    navigationIcon = {
      IconButton(onClick = { navigateToBack() }) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(CreamWarm)
        .padding(paddingValues),
      contentAlignment = Alignment.Center
    ) {
      Column(
        modifier = Modifier.width(300.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
      ) {
        
        OutlinedTextField(
          value = username,
          onValueChange = { viewModel.updateUsername(it) },
          label = { Text("Usuario", color = CharcoalWarm) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          isError = error != null,
          keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
          ),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Siena,
            focusedLabelColor = CharcoalWarm,
            unfocusedLabelColor = BrownMid,
            focusedTextColor = CharcoalWarm,
            unfocusedTextColor = CharcoalWarm
          )
        )
        
        OutlinedTextField(
          value = password,
          onValueChange = { viewModel.updatePassword(it) },
          label = { Text("Contraseña", color = CharcoalWarm) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
          ),
          isError = error != null,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Siena,
            focusedLabelColor = CharcoalWarm,
            unfocusedLabelColor = BrownMid,
            focusedTextColor = CharcoalWarm,
            unfocusedTextColor = CharcoalWarm
          ),
          trailingIcon = {
            IconButton(onClick = { showPassword = !showPassword }) {
              Icon(
                if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                contentDescription = null,
                tint = BrownMid
              )
            }
          }
        )
        
        if (error != null) {
          Text(error!!, color = Color.Red, fontSize = 14.sp)
        }
        
        Button(
          onClick = { viewModel.onLoginClick() },
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(containerColor = Siena),
          enabled = !isLoading
        ) {
          if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
          } else {
            Text(
              "Iniciar sesión",
              color = Color.White,
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
        
        Button(
          onClick = { viewModel.onGoogleSignInClick(context) },
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color.White),
          shape = RoundedCornerShape(28.dp),
          enabled = !isLoading
        ) {
          Text(
            "Iniciar sesión con Google",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = CharcoalWarm
          )
        }
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
          Text(text = "¿No tienes cuenta? ", fontSize = 14.sp, color = BrownMid)
          Text(
            text = "Regístrate",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Siena,
            modifier = Modifier.clickable { navigateToRegister() }
          )
        }
      }
    }
  }
}

