package com.memorias.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memorias.app.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class FavoritesGlanceWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideContent(context: Context, id: GlanceId, state: Any?) {
        val widgetState = loadState(context)
        provideContent {
            GlanceTheme {
                WidgetContent(widgetState)
            }
        }
    }

    private suspend fun loadState(context: Context): WidgetState = withContext(Dispatchers.IO) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java,
        )
        val repo = entryPoint.favoriteRepository()
        val today = LocalDate.now()

        val exact = repo.getByMonthDay(today.monthValue, today.dayOfMonth)
            .getOrNull()
            .orEmpty()
            .take(MAX_WIDGET_PHOTOS)

        if (exact.isNotEmpty()) {
            return@withContext WidgetState.Loaded(favorites = exact, isNearestDay = false)
        }

        val nearest = repo.getNearestToToday(maxResults = MAX_WIDGET_PHOTOS)
            .getOrNull()
            .orEmpty()

        if (nearest.isEmpty()) {
            return@withContext WidgetState.Loaded(favorites = emptyList())
        }

        val nearestFav = nearest.first()
        val nearDate = java.time.Instant.ofEpochMilli(nearestFav.dateTaken.toEpochMilli())
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        val description = "${nearDate.dayOfMonth}/${nearDate.monthValue}"

        WidgetState.Loaded(
            favorites = nearest,
            isNearestDay = true,
            nearestDayDescription = description,
        )
    }

    companion object {
        const val MAX_WIDGET_PHOTOS = 6
    }
}

@Composable
private fun WidgetContent(state: WidgetState) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF1F1A14)))
            .cornerRadius(20.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        when (state) {
            is WidgetState.Loading -> WidgetLoadingPlaceholder()
            is WidgetState.Loaded -> {
                if (state.favorites.isEmpty()) {
                    WidgetEmptyContent()
                } else {
                    WidgetPhotosContent(state)
                }
            }
        }
    }
}

@Composable
private fun WidgetPhotosContent(state: WidgetState.Loaded) {
    Column(modifier = GlanceModifier.fillMaxSize().padding(12.dp)) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Memorias",
                style = TextStyle(
                    color = ColorProvider(Color(0xFFF5C842)),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(GlanceModifier.defaultWeight())
            if (state.isNearestDay) {
                Text(
                    text = "Cercanas al ${state.nearestDayDescription}",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF8C7A6B)),
                        fontSize = 9.sp,
                    ),
                )
            }
        }

        Spacer(GlanceModifier.height(8.dp))

        LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
            items(state.favorites.chunked(2)) { rowPhotos ->
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    rowPhotos.forEach { favorite ->
                        WidgetPhotoCell(favorite, modifier = GlanceModifier.defaultWeight())
                        if (rowPhotos.size == 1) {
                            Box(modifier = GlanceModifier.defaultWeight().height(80.dp))
                        }
                    }
                }
                Spacer(GlanceModifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun WidgetPhotoCell(favorite: Favorite, modifier: GlanceModifier = GlanceModifier) {
    val uri = mediaStoreUriFromId(favorite.mediaStoreId)
    Box(
        modifier = modifier
            .height(80.dp)
            .padding(horizontal = 2.dp)
            .cornerRadius(10.dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        androidx.glance.Image(
            provider = androidx.glance.ImageProvider(uri),
            contentDescription = "Favorita ${favorite.yearTaken}",
            modifier = GlanceModifier.fillMaxSize().cornerRadius(10.dp),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = GlanceModifier
                .padding(4.dp)
                .cornerRadius(4.dp)
                .background(ColorProvider(Color.Black.copy(alpha = 0.6f))),
        ) {
            Text(
                text = "${favorite.yearTaken}",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = GlanceModifier.padding(horizontal = 4.dp, vertical = 1.dp),
            )
        }
    }
}

@Composable
private fun WidgetLoadingPlaceholder() {
    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Cargando...",
            style = TextStyle(
                color = ColorProvider(Color(0xFF8C7A6B)),
                fontSize = 12.sp,
            ),
        )
    }
}

@Composable
private fun WidgetEmptyContent() {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Sin favoritas aun",
            style = TextStyle(
                color = ColorProvider(Color(0xFF8C7A6B)),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(GlanceModifier.height(6.dp))
        Text(
            text = "Marca fotos como favoritas en la app",
            style = TextStyle(
                color = ColorProvider(Color(0xFF4A3728)),
                fontSize = 10.sp,
            ),
        )
    }
}

class FavoritesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FavoritesGlanceWidget()
}
