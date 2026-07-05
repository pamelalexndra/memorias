package com.memorias.app.domain.model

data class DeletionQueue(
    val photos: List<Photo> = emptyList(),
    ) {
    val isEmpty: Boolean get() = photos.isEmpty()
    val isNotEmpty: Boolean get() = photos.isNotEmpty()
    val size: Int get() = photos.size
    val totalBytes: Long get() = photos.sumOf { it.sizeBytes }
    val photoIds: List<Long> get() = photos.map { it.id }

    fun add(photo: Photo): DeletionQueue = copy(photos = photos + photo)
    fun remove(photo: Photo): DeletionQueue = copy(photos = photos - photo)
    fun clear(): DeletionQueue = copy(photos = emptyList())
}