package com.pamelapp.memorias.domain.model

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.LocalDate

data class Photo(
  val id: Long,
  val uri: Uri,
  val dateTaken: Instant,
  val year: Int,
  val month: Int,
  val day: Int,
  val displayName: String,
  val sizeBytes: Long,
  val mimeType: String,
  val width: Int = 0,
  val height: Int = 0
) {
  val yearsAgo: Int
    @RequiresApi(Build.VERSION_CODES.O)
    get() = LocalDate.now().year - year
  
  val stableFallbackHash: String
    @RequiresApi(Build.VERSION_CODES.O)
    get() = "${id}_${sizeBytes}_${dateTaken.toEpochMilli()}"
  
  val isValidForOnThisDay: Boolean
    @RequiresApi(Build.VERSION_CODES.O)
    get() = yearsAgo > 0
}