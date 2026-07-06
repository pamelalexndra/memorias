package com.example.pamelapp.domain.model

import android.net.Uri
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
    get() = LocalDate.now().year - year

  val stableFallbackHash: String
    get() = "${id}_${sizeBytes}_${dateTaken.toEpochMilli()}"

  val isValidForOnThisDay: Boolean
    get() = yearsAgo > 0
}