package com.example.pamelapp.domain.repository

import com.example.pamelapp.domain.model.Photo
import java.time.LocalDate

interface PhotoRepository {
    suspend fun getPhotosOnThisDay(date: LocalDate): List<Photo>
    suspend fun getPhotosByIds(ids: Set<Long>): List<Photo>
}