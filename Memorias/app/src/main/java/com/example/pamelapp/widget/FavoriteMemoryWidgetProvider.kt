package com.example.pamelapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.RemoteViews
import com.example.pamelapp.MainActivity
import com.example.pamelapp.R
import com.example.pamelapp.data.database.AppDatabase
import com.example.pamelapp.data.database.entities.toPhoto
import com.example.pamelapp.domain.model.Photo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DateTimeException
import java.time.LocalDate
import kotlin.math.abs

class FavoriteMemoryWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                appWidgetIds.forEach { appWidgetId ->
                    updateSingleWidget(
                        context = appContext,
                        appWidgetManager = appWidgetManager,
                        appWidgetId = appWidgetId
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {

        fun updateAllWidgets(context: Context) {
            val appContext = context.applicationContext
            val appWidgetManager = AppWidgetManager.getInstance(appContext)

            val componentName = ComponentName(
                appContext,
                FavoriteMemoryWidgetProvider::class.java
            )

            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)

            CoroutineScope(Dispatchers.IO).launch {
                widgetIds.forEach { widgetId ->
                    updateSingleWidget(
                        context = appContext,
                        appWidgetManager = appWidgetManager,
                        appWidgetId = widgetId
                    )
                }
            }
        }

        private suspend fun updateSingleWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            try {
                val database = AppDatabase.getDatabase(context)

                val favorites = database
                    .favoritePhotoDAO()
                    .getFavoritePhotosOnce()
                    .map { entity -> entity.toPhoto() }

                val selectedPhoto = selectPhotoForToday(favorites)

                val views = RemoteViews(
                    context.packageName,
                    R.layout.widget_favorite_memory
                )

                views.setOnClickPendingIntent(
                    R.id.widgetRoot,
                    createOpenLoginPendingIntent(context)
                )

                if (selectedPhoto == null) {
                    views.setTextViewText(
                        R.id.widgetTitle,
                        "Memorias"
                    )

                    views.setTextViewText(
                        R.id.widgetSubtitle,
                        "Aún no tienes favoritas"
                    )

                    views.setImageViewResource(
                        R.id.widgetPhoto,
                        R.drawable.widget_placeholder
                    )
                } else {
                    views.setTextViewText(
                        R.id.widgetTitle,
                        "Memorias"
                    )

                    views.setTextViewText(
                        R.id.widgetSubtitle,
                        "${selectedPhoto.day}/${selectedPhoto.month}/${selectedPhoto.year}"
                    )

                    val bitmap = loadBitmapFromPhoto(
                        context = context,
                        photo = selectedPhoto
                    )

                    if (bitmap != null) {
                        views.setImageViewBitmap(
                            R.id.widgetPhoto,
                            bitmap
                        )
                    } else {
                        views.setImageViewResource(
                            R.id.widgetPhoto,
                            R.drawable.widget_placeholder
                        )
                    }
                }

                appWidgetManager.updateAppWidget(
                    appWidgetId,
                    views
                )
            } catch (exception: Exception) {
                showFallbackWidget(
                    context = context,
                    appWidgetManager = appWidgetManager,
                    appWidgetId = appWidgetId
                )
            }
        }

        private fun showFallbackWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(
                context.packageName,
                R.layout.widget_favorite_memory
            )

            views.setTextViewText(
                R.id.widgetTitle,
                "Memorias"
            )

            views.setTextViewText(
                R.id.widgetSubtitle,
                "No se pudo cargar la foto"
            )

            views.setImageViewResource(
                R.id.widgetPhoto,
                R.drawable.widget_placeholder
            )

            views.setOnClickPendingIntent(
                R.id.widgetRoot,
                createOpenLoginPendingIntent(context)
            )

            appWidgetManager.updateAppWidget(
                appWidgetId,
                views
            )
        }

        private fun createOpenLoginPendingIntent(
            context: Context
        ): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("open_login", true)
            }

            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            return PendingIntent.getActivity(
                context,
                1001,
                intent,
                flags
            )
        }

        private fun loadBitmapFromPhoto(
            context: Context,
            photo: Photo
        ) = try {
            val resolver = context.contentResolver

            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            resolver.openInputStream(photo.uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, boundsOptions)
            }

            val targetSize = 512

            val sampleSize = calculateInSampleSize(
                width = boundsOptions.outWidth,
                height = boundsOptions.outHeight,
                targetWidth = targetSize,
                targetHeight = targetSize
            )

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }

            resolver.openInputStream(photo.uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            }
        } catch (exception: Exception) {
            null
        }

        private fun calculateInSampleSize(
            width: Int,
            height: Int,
            targetWidth: Int,
            targetHeight: Int
        ): Int {
            var inSampleSize = 1

            if (height > targetHeight || width > targetWidth) {
                val halfHeight = height / 2
                val halfWidth = width / 2

                while (
                    halfHeight / inSampleSize >= targetHeight &&
                    halfWidth / inSampleSize >= targetWidth
                ) {
                    inSampleSize *= 2
                }
            }

            return inSampleSize.coerceAtLeast(1)
        }

        private fun selectPhotoForToday(
            favorites: List<Photo>
        ): Photo? {
            if (favorites.isEmpty()) return null

            val today = LocalDate.now()

            val sameDayPhotos = favorites.filter { photo ->
                photo.month == today.monthValue && photo.day == today.dayOfMonth
            }

            if (sameDayPhotos.isNotEmpty()) {
                return rotatePhotos(sameDayPhotos)
            }

            val closestDistance = favorites.minOf { photo ->
                distanceFromToday(
                    today = today,
                    month = photo.month,
                    day = photo.day
                )
            }

            val closestPhotos = favorites.filter { photo ->
                distanceFromToday(
                    today = today,
                    month = photo.month,
                    day = photo.day
                ) == closestDistance
            }

            return rotatePhotos(closestPhotos)
        }

        private fun rotatePhotos(
            photos: List<Photo>
        ): Photo {
            val thirtyMinutesMillis = 30 * 60 * 1000L
            val bucket = System.currentTimeMillis() / thirtyMinutesMillis
            val index = (bucket % photos.size).toInt()

            return photos[index]
        }

        private fun distanceFromToday(
            today: LocalDate,
            month: Int,
            day: Int
        ): Int {
            val candidate = try {
                LocalDate.of(today.year, month, day)
            } catch (exception: DateTimeException) {
                return Int.MAX_VALUE
            }

            val directDistance = abs(candidate.dayOfYear - today.dayOfYear)
            val yearLength = today.lengthOfYear()

            return minOf(
                directDistance,
                yearLength - directDistance
            )
        }
    }
}