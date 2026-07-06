package com.example.pamelapp.domain.model

data class SwipeActionRecord(
    val type: SwipeActionType,
    val photo: Photo,
    val indexBeforeAction: Int,
    val wasFavoriteBeforeAction: Boolean
)