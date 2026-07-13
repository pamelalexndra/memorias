package com.pamelapp.memorias.ui.screens.registerscreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pamelapp.memorias.ui.theme.BrownMid
import com.pamelapp.memorias.ui.theme.CharcoalWarm
import com.pamelapp.memorias.ui.theme.Siena

@Composable
fun CredentialsSection(
  username: String,
  password: String,
  confirmPassword: String,
  usernameAvailable: Boolean?,
  onUsernameChange: (String) -> Unit,
  onPasswordChange: (String) -> Unit,
  onConfirmPasswordChange: (String) -> Unit
) {
  var showPassword by remember { mutableStateOf(false) }
  var showConfirmPassword by remember { mutableStateOf(false) }
  
  Card(
    colors = CardDefaults.cardColors(containerColor = Color.White),
    shape = RoundedCornerShape(12.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.CenterVertically)
    ) {
      Text(
        text = "Credenciales de acceso",
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = CharcoalWarm
      )
      
      HorizontalDivider(color = BrownMid.copy(alpha = 0.3f))
      
      UsernameTextField(
        value = username,
        onValueChange = onUsernameChange,
        isAvailable = usernameAvailable,
        label = "Usuario *"
      )
      
      OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Contraseña *", color = CharcoalWarm) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
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
      
      OutlinedTextField(
        value = confirmPassword,
        onValueChange = onConfirmPasswordChange,
        label = { Text("Confirmar contraseña *", color = CharcoalWarm) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions.Default.copy(
          keyboardType = KeyboardType.Password,
          imeAction = ImeAction.Done
        ),
        isError = confirmPassword.isNotEmpty() && password != confirmPassword,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = Siena,
          focusedLabelColor = CharcoalWarm,
          unfocusedLabelColor = BrownMid,
          focusedTextColor = CharcoalWarm,
          unfocusedTextColor = CharcoalWarm
        ),
        trailingIcon = {
          IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
            Icon(
              if (showConfirmPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
              contentDescription = null,
              tint = BrownMid
            )
          }
        },
        supportingText = {
          when {
            confirmPassword.isNotEmpty() && password != confirmPassword -> {
              Text(text = "✗ Las contraseñas no coinciden", color = Color.Red, fontSize = 12.sp)
            }
            
            confirmPassword.isNotEmpty() && password == confirmPassword -> {
              Text(text = "✓ Las contraseñas coinciden", color = Color.Green, fontSize = 12.sp)
            }
          }
        }
      )
    }
  }
}
