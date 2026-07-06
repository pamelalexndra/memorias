package com.example.pamelapp.data.media

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.example.pamelapp.domain.model.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


@RequiresApi(Build.VERSION_CODES.O)
private suspend fun queryOnThisDayPhotos(contentResolver: ContentResolver): List<Photo> =
    withContext(Dispatchers.IO) {
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()

        val projection = buildList {
            add(MediaStore.Images.Media._ID)
            add(MediaStore.Images.Media.DISPLAY_NAME)
            add(MediaStore.Images.Media.DATE_TAKEN)
            add(MediaStore.Images.Media.DATE_ADDED)
            add(MediaStore.Images.Media.DATE_MODIFIED)
            add(MediaStore.Images.Media.SIZE)
            add(MediaStore.Images.Media.MIME_TYPE)
            add(MediaStore.Images.Media.WIDTH)
            add(MediaStore.Images.Media.HEIGHT)
        }.toTypedArray()

        val results = mutableListOf<Photo>()

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateTakenCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val dateModifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val displayName = cursor.getStringOrNullSafe(nameCol) ?: "foto"

                val dateTakenMillis = cursor.getLongOrNullSafe(dateTakenCol)
                val dateAddedSeconds = cursor.getLongOrNullSafe(dateAddedCol)
                val dateModifiedSeconds = cursor.getLongOrNullSafe(dateModifiedCol)

                val matchedInstant = resolveBestDateForOnThisDay(
                    displayName = displayName,
                    dateTakenMillis = dateTakenMillis,
                    dateAddedSeconds = dateAddedSeconds,
                    dateModifiedSeconds = dateModifiedSeconds,
                    today = today,
                    zone = zone
                ) ?: continue

                val local = matchedInstant.atZone(zone).toLocalDate()

                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                results.add(
                    Photo(
                        id = id,
                        uri = uri,
                        dateTaken = matchedInstant,
                        year = local.year,
                        month = local.monthValue,
                        day = local.dayOfMonth,
                        sizeBytes = cursor.getLongOrNullSafe(sizeCol) ?: 0L,
                        displayName = displayName,
                        mimeType = cursor.getStringOrNullSafe(mimeCol) ?: "image/*",
                        width = cursor.getIntOrZero(widthCol),
                        height = cursor.getIntOrZero(heightCol),
                    )
                )
            }
        }

        results
            .distinctBy { it.id }
            .sortedByDescending { it.dateTaken }
    }

@RequiresApi(Build.VERSION_CODES.O)
private fun resolveBestDateForOnThisDay(
    displayName: String,
    dateTakenMillis: Long?,
    dateAddedSeconds: Long?,
    dateModifiedSeconds: Long?,
    today: LocalDate,
    zone: ZoneId
): Instant? {
    val candidateInstants = listOfNotNull(
        dateTakenMillis
            ?.takeIf { it > 0L }
            ?.let { Instant.ofEpochMilli(it) },

        extractDateFromFileName(displayName)
            ?.atStartOfDay(zone)
            ?.toInstant(),

        dateModifiedSeconds
            ?.takeIf { it > 0L }
            ?.let { Instant.ofEpochSecond(it) },

        dateAddedSeconds
            ?.takeIf { it > 0L }
            ?.let { Instant.ofEpochSecond(it) },
    )

    return candidateInstants.firstOrNull { instant ->
        val local = instant.atZone(zone).toLocalDate()

        local.monthValue == today.monthValue &&
                local.dayOfMonth == today.dayOfMonth &&
                local.year < today.year
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun extractDateFromFileName(fileName: String): LocalDate? {
    val patterns = listOf(
        // WhatsApp: IMG-20240706-WA0006.jpg
        Regex("""(?i).*?(\d{4})(\d{2})(\d{2}).*"""),

        // Screenshot_2024-07-06-20-27-49-479_com.whatsapp.jpg
        Regex("""(?i).*?(\d{4})-(\d{2})-(\d{2}).*"""),

        // Instagram: IMG_20230706_192239_375.webp
        Regex("""(?i).*?(\d{4})_(\d{2})(\d{2}).*""")
    )

    for (regex in patterns) {
        val match = regex.matchEntire(fileName) ?: continue

        val year = match.groupValues[1].toIntOrNull() ?: continue
        val month = match.groupValues[2].toIntOrNull() ?: continue
        val day = match.groupValues[3].toIntOrNull() ?: continue

        try {
            return LocalDate.of(year, month, day)
        } catch (exception: DateTimeException) {
            continue
        }
    }

    return null
}

private fun Cursor.getLongOrNullSafe(columnIndex: Int): Long? {
    return if (columnIndex < 0 || isNull(columnIndex)) {
        null
    } else {
        getLong(columnIndex)
    }
}

private fun Cursor.getStringOrNullSafe(columnIndex: Int): String? {
    return if (columnIndex < 0 || isNull(columnIndex)) {
        null
    } else {
        getString(columnIndex)
    }
}

private fun Cursor.getIntOrZero(columnIndex: Int): Int {
    return if (columnIndex < 0 || isNull(columnIndex)) {
        0
    } else {
        getInt(columnIndex)
    }
}