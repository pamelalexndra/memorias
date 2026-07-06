package com.example.pamelapp.ui.screens.registerscreen.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.pamelapp.ui.theme.*

@Composable
fun UsernameTextField(
  value: String,
  onValueChange: (String) -> Unit,
  isAvailable: Boolean?,
  label: String
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text(label, color = CharcoalWarm) },
    modifier = Modifier.fillMaxWidth(),
    singleLine = true,
    isError = isAvailable == false,
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
    supportingText = {
      when (isAvailable) {
        true -> Text(text = "✓ Usuario disponible", color = Color.Green, fontSize = 12.sp)
        false -> Text(text = "✗ Usuario no disponible", color = Color.Red, fontSize = 12.sp)
        null -> Unit
      }
    }
  )
}