package com.memorias.app.domain.model.entities

data class PhotoPage(
    val groups: List<OnThisDayGroup>,
    val totalCount: Int,
    val hasMore: Boolean,
    val nextCursor: Long? = null
)