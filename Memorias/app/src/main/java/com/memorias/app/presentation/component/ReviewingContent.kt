package com.memorias.app.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memorias.app.domain.model.enums.SwipeAction
import com.memorias.app.presentation.screen.onthisday.OnThisDayUiState
import com.memorias.app.presentation.theme.BrownLight
import com.memorias.app.presentation.theme.BrownMid
import com.memorias.app.presentation.theme.Siena
import com.memorias.app.presentation.theme.SwipeFav

@Composable
fun ReviewingContent(
    state: OnThisDayUiState,
    onSwipe: (SwipeAction) -> Unit,
    onYearSelected: (Int) -> Unit,
    onUndo: () -> Unit,
    onReviewDeleteQueue: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {

        Surface(color = Color.White, shadowElevation = 1.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Memorias", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Siena)
                Spacer(Modifier.weight(1f))

                if (state.sessionFavorited > 0) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = SwipeFav, modifier = Modifier.height(16.dp))
                    Text(" ${state.sessionFavorited}", color = SwipeFav, fontSize = 13.sp)
                    Spacer(Modifier.width(12.dp))
                }

                IconButton(onClick = onUndo, enabled = state.canUndo) {
                    Icon(Icons.Default.Undo, contentDescription = "Deshacer")
                }

                Text(
                    "${state.currentPhotoIndex + 1}/${state.totalPhotosInGroup}",
                    color = BrownMid,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }

        YearTimeline(
            groups = state.groups,
            selectedIndex = state.selectedYearIndex,
            onYearSelected = onYearSelected,
            modifier = Modifier.padding(vertical = 10.dp),
        )

        state.currentPhoto?.let { photo ->
            Text(
                text = photo.yearsAgo.yearsAgoLabel() + " - ${photo.year}",
                color = BrownMid,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 20.dp, bottom = 4.dp),
            )
        }

        Box(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            state.currentPhotoUri()?.let { uri ->
                key(state.currentPhotoIndex, state.selectedYearIndex) {
                    SwipeablePhotoCard(
                        imageModel = uri,
                        badgeLabel = "${state.currentPhoto?.year}",
                        onSwiped = onSwipe,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        Text(
            text = "izquierda conservar  -  arriba favorita  -  derecha borrar",
            color = BrownLight,
            fontSize = 11.sp,
            modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        DeletionQueueBar(
            photoCount = state.deletionQueue.size,
            totalBytes = state.deletionQueue.totalBytes,
            onReviewClick = onReviewDeleteQueue,
        )
    }
}