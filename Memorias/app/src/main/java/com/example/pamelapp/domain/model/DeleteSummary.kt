package com.example.pamelapp.domain.model

data class DeleteSummary(
    val totalPhotos: Int,
    val totalBytes: Long,
) {
    val totalMb: Float
        get() = totalBytes / 1_048_576f

    val isEmpty: Boolean
        get() = totalPhotos == 0
}