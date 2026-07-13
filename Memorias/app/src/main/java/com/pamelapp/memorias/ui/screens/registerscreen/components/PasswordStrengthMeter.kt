package com.pamelapp.memorias.ui.screens.registerscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PasswordValidationResult(
  val hasMinLength: Boolean,
  val hasUpperLower: Boolean,
  val hasNumber: Boolean,
  val hasSymbol: Boolean
)

@Composable
fun PasswordStrengthMeter(password: String) {
  val validation = validatePassword(password)
  val score = calculatePasswordScore(validation, password)
  val strengthColor = when (score) {
    0 -> Color.Transparent
    in 1..2 -> Color(0xFFEF4444)
    in 3..4 -> Color(0xFFEAB308)
    5 -> Color(0xFF22C55E)
    else -> Color.Transparent
  }
  val strengthText = when (score) {
    0 -> ""
    in 1..2 -> "Débil"
    in 3..4 -> "Media"
    5 -> "Fuerte"
    else -> ""
  }
  val textColor = when (score) {
    0 -> Color(0xFF6B7280)
    in 1..2 -> Color(0xFF991B1B)
    in 3..4 -> Color(0xFF854D0E)
    5 -> Color(0xFF166534)
    else -> Color(0xFF6B7280)
  }
  
  Column(modifier = Modifier.fillMaxWidth()) {
    Spacer(modifier = Modifier.height(8.dp))
    
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(8.dp)
        .clip(RoundedCornerShape(4.dp))
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(Color(0xFFE5E7EB))
      )
      Box(
        modifier = Modifier
          .fillMaxWidth((score / 5f).coerceIn(0f, 1f))
          .height(8.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(strengthColor)
      )
    }
    
    Spacer(modifier = Modifier.height(4.dp))
    
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text("Fortaleza:", fontSize = 12.sp, color = Color(0xFF4B5563))
      Text(
        strengthText,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = textColor
      )
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Card(
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Text(
          text = "La contraseña debe contener:",
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = Color(0xFF374151)
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        RequirementItem(
          text = "Mínimo 8 caracteres",
          isValid = validation.hasMinLength
        )
        RequirementItem(
          text = "Mayúsculas y minúsculas",
          isValid = validation.hasUpperLower
        )
        RequirementItem(
          text = "Al menos un número",
          isValid = validation.hasNumber
        )
        RequirementItem(
          text = "Al menos un símbolo (@$!%*?&#)",
          isValid = validation.hasSymbol
        )
      }
    }
  }
}

@Composable
private fun RequirementItem(text: String, isValid: Boolean) {
  Row(
    modifier = Modifier.padding(vertical = 2.dp),
    horizontalArrangement = Arrangement.Start
  ) {
    Text(
      text = if (isValid) "✓" else "○",
      fontSize = 12.sp,
      color = if (isValid) Color(0xFF22C55E) else Color(0xFF9CA3AF)
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = text,
      fontSize = 12.sp,
      color = if (isValid) Color(0xFF22C55E) else Color(0xFF6B7280)
    )
  }
}

private fun validatePassword(password: String): PasswordValidationResult {
  return PasswordValidationResult(
    hasMinLength = password.length >= 8,
    hasUpperLower = password.any { it.isUpperCase() } && password.any { it.isLowerCase() },
    hasNumber = password.any { it.isDigit() },
    hasSymbol = password.any { it in "!@#$%^&*()_+-=[]{}|;:,.<>?/" }
  )
}

private fun calculatePasswordScore(validation: PasswordValidationResult, password: String): Int {
  var score = 0
  if (validation.hasMinLength) score += 1
  if (validation.hasUpperLower) score += 1
  if (validation.hasNumber) score += 1
  if (validation.hasSymbol) score += 1
  if (password.length >= 12) score += 1
  return score
}