package com.memorias.app.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memorias.app.ui.theme.BrownMid
import com.memorias.app.ui.theme.SandSoft
import com.memorias.app.ui.theme.Siena

@Composable
fun YearPill(
    year: Int,
    photoCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) Siena else SandSoft,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "$year",
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else BrownMid,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
            )
            Text(
                text = "$photoCount foto${if (photoCount != 1) "s" else ""}",
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f) else BrownMid.copy(alpha = 0.8f),
                fontSize = 10.sp,
            )
        }
    }
}
