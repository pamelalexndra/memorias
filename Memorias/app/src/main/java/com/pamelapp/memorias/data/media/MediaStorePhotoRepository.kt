package com.pamelapp.memorias.data.media

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.pamelapp.memorias.domain.model.Photo
import com.pamelapp.memorias.domain.repository.photoRepository.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class MediaStorePhotoRepository(
  private val contentResolver: ContentResolver
) : PhotoRepository {
  
  @RequiresApi(Build.VERSION_CODES.O)
  override suspend fun getPhotosOnThisDay(): List<Photo> =
    withContext(Dispatchers.IO) {
      val today = LocalDate.now()
      val zone = ZoneId.systemDefault()
      
      val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATE_TAKEN,
        MediaStore.Images.Media.DATE_ADDED,
        MediaStore.Images.Media.DATE_MODIFIED,
        MediaStore.Images.Media.SIZE,
        MediaStore.Images.Media.MIME_TYPE,
        MediaStore.Images.Media.WIDTH,
        MediaStore.Images.Media.HEIGHT
      )
      
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
        ?.let { Instant.ofEpochSecond(it) }
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
      Regex("""(?i).*?(\d{4})(\d{2})(\d{2}).*"""),
      Regex("""(?i).*?(\d{4})-(\d{2})-(\d{2}).*"""),
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
}