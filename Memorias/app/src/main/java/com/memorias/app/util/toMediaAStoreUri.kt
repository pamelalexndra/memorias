package com.memorias.app.util

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import com.memorias.app.domain.model.Photo

fun Photo.toMediaStoreUri(): Uri =
    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

fun mediaStoreUriFromId(id: Long): Uri =
    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
