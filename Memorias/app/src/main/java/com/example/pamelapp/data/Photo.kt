package com.example.pamelapp.data

import android.net.Uri

data class Photo(
  val id: Long,
  val uri: Uri,
  val dateTaken: Long,
  val year: Int,
  val sizeBytes: Long,
  val displayName: String,
)