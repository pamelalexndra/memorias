package com.memorias.app.presentation.screen.onthisday

import com.memorias.app.domain.model.DeletionQueue
import com.memorias.app.domain.model.OnThisDayGroup
import com.memorias.app.domain.model.PhotoFilter
import com.memorias.app.domain.model.enums.SessionSize

data class OnThisDayUiState(
    val status: Status = Status.Loading,
    val groups: List<OnThisDayGroup> = emptyList(),
    val selectedYearIndex: Int = 0,
    val currentPhotoIndex: Int = 0,
    val deletionQueue: DeletionQueue = DeletionQueue(),
    val sessionFavorited: Int = 0,
    val sessionKept: Int = 0,
    val sessionStartMs: Long = System.currentTimeMillis(),
    val activeFilter: PhotoFilter = PhotoFilter.None,
    val nextCursor: Long? = null,
    val hasMorePhotos: Boolean = false,
    val isLoadingMore: Boolean = false,
    val sessionSizeLimit: Int = SessionSize.NORMAL.count,
    val photosReviewedSinceLastPause: Int = 0,
) {
    sealed class Status {
        object Loading : Status()
        object Empty : Status()
        object Reviewing : Status()
        object Confirming : Status()
        object PausedAtLimit : Status()
        object Done : Status()
        data class Error(val message: String) : Status()
    }

    val selectedGroup: OnThisDayGroup? get() = groups.getOrNull(selectedYearIndex)
    val currentPhoto: Photo? get() = selectedGroup?.photos?.getOrNull(currentPhotoIndex)
    val totalPhotosInGroup: Int get() = selectedGroup?.photoCount ?: 0
    val progressInGroup: Float get() =
        if (totalPhotosInGroup == 0) 0f else (currentPhotoIndex + 1).toFloat() / totalPhotosInGroup
    val sessionReviewed: Int get() = sessionFavorited + sessionKept + deletionQueue.size
    val isLastPhotoInGroup: Boolean get() = currentPhotoIndex >= totalPhotosInGroup - 1
    val showDeleteBadge: Boolean get() = deletionQueue.isNotEmpty
    val canUndo: Boolean get() = currentPhotoIndex > 0 || deletionQueue.isNotEmpty

    fun currentPhotoUri(): Uri? = currentPhoto?.let {
        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, it.id)
    }
}