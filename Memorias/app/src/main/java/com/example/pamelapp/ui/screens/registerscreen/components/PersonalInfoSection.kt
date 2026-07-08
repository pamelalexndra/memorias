package com.example.pamelapp.ui.screens.registerscreen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pamelapp.ui.theme.*

@Composable
fun PersonalInfoSection(
  email: String,
  emailAvailable: Boolean?,
  onEmailChange: (String) -> Unit
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Text(
        text = "Información personal",
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = CharcoalWarm
      )
      
      Spacer(modifier = Modifier.height(8.dp))
      HorizontalDivider(color = BrownMid.copy(alpha = 0.3f))
      Spacer(modifier = Modifier.height(16.dp))
      
      EmailTextField(
        value = email,
        onValueChange = onEmailChange,
        isAvailable = emailAvailable,
        label = "Correo electrónico *"
      )
    }
  }
}