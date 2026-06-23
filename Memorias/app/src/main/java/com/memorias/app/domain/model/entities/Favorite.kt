package com.memorias.app.domain.model.entities

import java.time.Instant

data class Favorite(
    val localId: String,
    val serverId: String? = null,
    val mediaStoreId: Long,
    val photoHash: String,
    val dateTaken: Instant,
    val yearTaken: Int,
    val location: GeoLocation? = null,
    val note: String? = null,
    val addedAt: Instant = Instant.now(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD,
    ) {
    val isSynced: Boolean get() = syncStatus == SyncStatus.SYNCED
}