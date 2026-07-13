package com.pamelapp.memorias.domain.repository.photoRepository

import com.pamelapp.memorias.domain.model.Photo

interface PhotoRepository {
  suspend fun getPhotosOnThisDay(): List<Photo>
}