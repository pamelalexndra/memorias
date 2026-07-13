package com.pamelapp.memorias.ui.screens.loginscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pamelapp.memorias.ui.scaffold.AppScaffold
import com.pamelapp.memorias.ui.theme.BrownMid
import com.pamelapp.memorias.ui.theme.CharcoalWarm
import com.pamelapp.memorias.ui.theme.CreamWarm
import com.pamelapp.memorias.ui.theme.Siena

@Composable
fun LoginScreen(
  navigateToBack: () -> Unit,
  navigateToRegister: () -> Unit,
  navigateToSwipe: () -> Unit,
  viewModel: LoginViewModel = viewModel(
    factory = LoginViewModel.provideFactory()
  )
) {
  var username by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  
  val isLoading by viewModel.isLoading.collectAsState()
  val isGoogleSignInLoading by viewModel.isGoogleSignInLoading.collectAsState()
  val error by viewModel.error.collectAsState()
  val onSuccess by viewModel.onSuccess.collectAsState()
  
  val context = LocalContext.current
  
  LaunchedEffect(onSuccess) {
    if (onSuccess) {
      viewModel.resetSuccess()
      navigateToSwipe()
    }
  }
  
  AppScaffold(
    title = "Iniciar sesión",
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
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(CreamWarm)
        .padding(paddingValues)
        .padding(horizontal = 20.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(
        modifier = Modifier
          .widthIn(max = 380.dp)
          .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Text(
          text = "Bienvenida de nuevo",
          color = CharcoalWarm,
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center
        )
        
        Text(
          text = "Ingresa con tu cuenta para continuar.",
          color = BrownMid,
          fontSize = 14.sp,
          textAlign = TextAlign.Center
        )
        
        OutlinedTextField(
          value = username,
          onValueChange = { username = it },
          label = {
            Text(text = "Usuario")
          },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
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
          onValueChange = { password = it },
          label = {
            Text(text = "Contraseña")
          },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          visualTransformation = if (passwordVisible) {
            VisualTransformation.None
          } else {
            PasswordVisualTransformation()
          },
          trailingIcon = {
            IconButton(
              onClick = {
                passwordVisible = !passwordVisible
              }
            ) {
              Icon(
                imageVector = if (passwordVisible) {
                  Icons.Outlined.VisibilityOff
                } else {
                  Icons.Outlined.Visibility
                },
                contentDescription = null,
                tint = BrownMid
              )
            }
          },
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
          ),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Siena,
            focusedLabelColor = CharcoalWarm,
            unfocusedLabelColor = BrownMid,
            focusedTextColor = CharcoalWarm,
            unfocusedTextColor = CharcoalWarm
          )
        )
        
        Button(
          onClick = {
            viewModel.onLoginClick(
              username = username,
              password = password
            )
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
          enabled = !isLoading &&
                  !isGoogleSignInLoading &&
                  username.isNotBlank() &&
                  password.length >= 6,
          colors = ButtonDefaults.buttonColors(
            containerColor = Siena,
            disabledContainerColor = BrownMid
          ),
          shape = RoundedCornerShape(14.dp)
        ) {
          if (isLoading) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              color = Color.White
            )
          } else {
            Text(
              text = "Iniciar sesión",
              color = Color.White,
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
        
        HorizontalDivider(
          color = BrownMid.copy(alpha = 0.25f)
        )
        
        Button(
          onClick = {
            viewModel.onGoogleSignInClick(context)
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            disabledContainerColor = Color.White.copy(alpha = 0.6f)
          ),
          shape = RoundedCornerShape(14.dp),
          enabled = !isLoading && !isGoogleSignInLoading
        ) {
          if (isGoogleSignInLoading) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              color = CharcoalWarm
            )
          } else {
            Text(
              text = "Continuar con Google",
              fontSize = 16.sp,
              fontWeight = FontWeight.Medium,
              color = CharcoalWarm
            )
          }
        }
        
        if (error != null) {
          Text(
            text = error ?: "",
            color = Color.Red,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
        }
        
        Text(
          text = "¿No tienes cuenta? Regístrate aquí",
          color = Siena,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.clickable {
            navigateToRegister()
          }
        )
      }
    }
  }
}