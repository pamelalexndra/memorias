package com.example.pamelapp.domain.repository

import com.example.pamelapp.domain.model.Photo

interface PhotoRepository {
    suspend fun getPhotosOnThisDay(): List<Photo>
}