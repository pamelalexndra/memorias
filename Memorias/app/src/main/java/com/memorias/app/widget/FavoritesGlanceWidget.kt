package com.memorias.app.widget

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.memorias.app.MainActivity

class FavoritesGlanceWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        provideContent {
            val prefs        = currentState<Preferences>()
            val collagePath  = prefs[WidgetPreferenceKeys.COLLAGE_FILE]
            val photoCount   = prefs[WidgetPreferenceKeys.PHOTO_COUNT] ?: 0
            val isNearestDay = prefs[WidgetPreferenceKeys.IS_NEAREST_DAY] ?: false
            val nearestDesc  = prefs[WidgetPreferenceKeys.NEAREST_DAY_DESC] ?: ""

            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(androidx.compose.ui.graphics.Color(0xFF1F1A14)))
                        .cornerRadius(20.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                ) {
                    when {
                        photoCount == 0 || collagePath == null -> EmptyContent()
                        else -> CollageContent(
                            collagePath  = collagePath,
                            photoCount   = photoCount,
                            isNearestDay = isNearestDay,
                            nearestDesc  = nearestDesc,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CollageContent(
    collagePath: String,
    photoCount: Int,
    isNearestDay: Boolean,
    nearestDesc: String,
) {
    Column(modifier = GlanceModifier.fillMaxSize().padding(8.dp)) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Memorias",
                style = TextStyle(
                    color      = ColorProvider(androidx.compose.ui.graphics.Color(0xFFF5C842)),
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(GlanceModifier.defaultWeight())
            if (isNearestDay) {
                Text(
                    text = "Cerca del $nearestDesc",
                    style = TextStyle(
                        color    = ColorProvider(androidx.compose.ui.graphics.Color(0xFF8C7A6B)),
                        fontSize = 9.sp,
                    ),
                )
            }
        }

        Spacer(GlanceModifier.height(6.dp))

        val bitmap = BitmapFactory.decodeFile(collagePath)
        if (bitmap != null) {
            Image(
                provider           = ImageProvider(bitmap),
                contentDescription = "$photoCount foto favorita${if (photoCount != 1) "s" else ""}",
                contentScale       = ContentScale.Crop,
                modifier           = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(12.dp),
            )
        }
    }
}

@Composable
private fun EmptyContent() {
    Column(
        modifier            = GlanceModifier.fillMaxSize().padding(16.dp),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text  = "Sin favoritas aun",
            style = TextStyle(
                color      = ColorProvider(androidx.compose.ui.graphics.Color(0xFF8C7A6B)),
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(GlanceModifier.height(6.dp))
        Text(
            text  = "Marca fotos como favoritas en la app",
            style = TextStyle(
                color    = ColorProvider(androidx.compose.ui.graphics.Color(0xFF4A3728)),
                fontSize = 10.sp,
            ),
        )
    }
}

class FavoritesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FavoritesGlanceWidget()
}