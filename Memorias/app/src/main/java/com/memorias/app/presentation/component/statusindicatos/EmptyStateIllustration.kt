package com.memorias.app.presentation.component.statusindicatos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memorias.app.presentation.theme.Siena

@Composable
private fun EmptyStateIllustration() {
    Box(contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(120.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(28.dp)))
        Box(modifier = Modifier.size(64.dp).background(Siena.copy(alpha = 0.18f), RoundedCornerShape(50)))
        Text("Sin fotos", fontSize = 13.sp, color = Siena, fontWeight = FontWeight.Bold)
    }
}