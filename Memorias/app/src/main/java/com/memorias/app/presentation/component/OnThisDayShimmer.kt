package com.memorias.app.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.memorias.app.presentation.component.statusindicatos.shimmerEffect

@Composable
fun OnThisDayShimmer(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) {
                Box(modifier = Modifier.width(72.dp).height(48.dp).shimmerEffect())
            }
        }
        Spacer(Modifier.height(20.dp))
        Box(modifier = Modifier.fillMaxWidth().height(420.dp).shimmerEffect(20.dp))
    }
}