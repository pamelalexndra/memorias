package com.example.pamelapp.data.database.entities

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pamelapp.domain.model.Photo
import java.time.Instant

@Entity(tableName = "favorite_photo")
data class FavoritePhotoEntity(
  @PrimaryKey(autoGenerate = false)
  val id: Long,
  val uri: Uri,
  val dateTaken: Long,
  val year: Int,
  val month: Int,
  val day: Int,
  val displayName: String,
  val sizeBytes: Long,
  val mimeType: String,
  val width: Int = 0,
  val height: Int = 0
)

@RequiresApi(Build.VERSION_CODES.O)
fun Photo.toFavoritePhotoEntity(): FavoritePhotoEntity {
  return FavoritePhotoEntity(
    id = id,
    uri = uri,
    dateTaken = dateTaken.toEpochMilli(),
    year = year,
    month = month,
    day = day,
    displayName = displayName,
    sizeBytes = sizeBytes,
    mimeType = mimeType,
    width = width,
    height = height
  )
}

@RequiresApi(Build.VERSION_CODES.O)
fun FavoritePhotoEntity.toPhoto(): Photo {
  return Photo(
    id = id,
    uri = uri,
    dateTaken = Instant.ofEpochMilli(dateTaken),
    year = year,
    month = month,
    day = day,
    displayName = displayName,
    sizeBytes = sizeBytes,
    mimeType = mimeType,
    width = width,
    height = height
  )
}