package com.example.pamelapp.ui.screens.swipescreen.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pamelapp.ui.theme.BrownMid
import com.example.pamelapp.ui.theme.Siena

@Composable
fun YearFilterChips(
  availableYears: List<Int>,
  selectedYear: Int?,
  onYearSelected: (Int?) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState())
      .padding(horizontal = 16.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    
    AssistChip(
      onClick = { onYearSelected(null) },
      label = { Text("Todos") },
      colors = AssistChipDefaults.assistChipColors(
        containerColor = if (selectedYear == null) Siena else BrownMid.copy(alpha = 0.1f),
        labelColor = if (selectedYear == null) Color.White else BrownMid
      )
    )
    
    
    availableYears.forEach { year ->
      AssistChip(
        onClick = { onYearSelected(year) },
        label = { Text(year.toString()) },
        colors = AssistChipDefaults.assistChipColors(
          containerColor = if (selectedYear == year) Siena else BrownMid.copy(alpha = 0.1f),
          labelColor = if (selectedYear == year) Color.White else BrownMid
        )
      )
    }
  }
}