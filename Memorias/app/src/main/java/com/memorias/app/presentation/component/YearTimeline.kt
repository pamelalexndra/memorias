package com.memorias.app.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.memorias.app.domain.model.OnThisDayGroup

@Composable
fun YearTimeline(
    groups: List<OnThisDayGroup>,
    selectedIndex: Int,
    onYearSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(groups.size) { index ->
            val group = groups[index]
            YearPill(
                year = group.year,
                photoCount = group.photoCount,
                isSelected = index == selectedIndex,
                onClick = { onYearSelected(index) },
            )
        }
    }
}