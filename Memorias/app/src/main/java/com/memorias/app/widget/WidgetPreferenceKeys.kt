package com.memorias.app.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.memorias.app.MemoriasApplication
import com.memorias.app.domain.model.Favorite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
internal object WidgetPreferenceKeys {
    val COLLAGE_FILE    = stringPreferencesKey("widget_collage_file")
    val PHOTO_COUNT     = intPreferencesKey("widget_photo_count")
    val IS_NEAREST_DAY  = booleanPreferencesKey("widget_is_nearest_day")
    val NEAREST_DAY_DESC = stringPreferencesKey("widget_nearest_day_desc")
    val LAST_UPDATED    = stringPreferencesKey("widget_last_updated")
}

object WidgetUpdater {

    suspend fun update(context: Context) {
        val appContext = context.applicationContext
        val (favorites, isNearestDay, nearestDayDesc) = withContext(Dispatchers.IO) {
            fetchFavorites(appContext)
        }

        val collageFile: File? = withContext(Dispatchers.IO) {
            if (favorites.isEmpty()) {
                clearCollageFile(appContext)
                null

            } else {
                val generator = WidgetBitmapGenerator(appContext)
                val bitmap = generator.generate(favorites)
                if (bitmap != null) saveBitmapToFile(appContext, bitmap) else null
            }
        }

        val manager = GlanceAppWidgetManager(appContext)
        val glanceIds = manager.getGlanceIds(FavoritesGlanceWidget::class.java)

        glanceIds.forEach { glanceId ->
            updateAppWidgetState(appContext, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                prefs.toMutablePreferences().apply {
                    if (collageFile != null) {
                        this[WidgetPreferenceKeys.COLLAGE_FILE] = collageFile.absolutePath
                        this[WidgetPreferenceKeys.PHOTO_COUNT] = favorites.size
                        this[WidgetPreferenceKeys.IS_NEAREST_DAY] = isNearestDay
                        this[WidgetPreferenceKeys.NEAREST_DAY_DESC] = nearestDayDesc
                    } else {
                        remove(WidgetPreferenceKeys.COLLAGE_FILE)
                        this[WidgetPreferenceKeys.PHOTO_COUNT] = 0
                    }
                    this[WidgetPreferenceKeys.LAST_UPDATED] = Instant.now().toString()
                }
            }
            FavoritesGlanceWidget().update(appContext, glanceId)
        }
    }
    private data class FavoriteResult(
        val favorites: List<Favorite>,
        val isNearestDay: Boolean,
        val nearestDayDesc: String,
        )
    private suspend fun fetchFavorites(context: Context): FavoriteResult {

        return try {
            val repo = (context as MemoriasApplication).container.favoriteRepository
            val today = LocalDate.now()
            val exact = repo.getByMonthDay(today.monthValue, today.dayOfMonth)

                .getOrNull().orEmpty().take(WidgetBitmapGenerator.MAX_PHOTOS)

            if (exact.isNotEmpty()) {
                return FavoriteResult(exact, isNearestDay = false, nearestDayDesc = "")
            }

            val nearest = repo.getNearestToToday(maxResults = WidgetBitmapGenerator.MAX_PHOTOS)

                .getOrNull().orEmpty()

            if (nearest.isEmpty()) return FavoriteResult(emptyList(), false, "")

            val nearDate = Instant.ofEpochMilli(nearest.first().dateTaken.toEpochMilli())
                .atZone(ZoneId.systemDefault()).toLocalDate()

            val desc = "${nearDate.dayOfMonth}/${nearDate.monthValue}"

            FavoriteResult(nearest, isNearestDay = true, nearestDayDesc = desc)


        } catch (e: NotImplementedError) {
            FavoriteResult(emptyList(), false, "")
        } catch (e: Exception) {
            FavoriteResult(emptyList(), false, "")
        }

    }
    private fun saveBitmapToFile(context: Context, bitmap: Bitmap): File? = runCatching {

        val dir = File(context.cacheDir, "widget").apply { mkdirs() }

        val file = File(dir, "collage.png")

        FileOutputStream(file).use { out ->

            // PNG sin pérdida, quality ignorado para PNG

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)

        }

        bitmap.recycle()

        file

    }.getOrNull()
    private fun clearCollageFile(context: Context) {
        File(context.cacheDir, "widget/collage.png").delete()
    }
}