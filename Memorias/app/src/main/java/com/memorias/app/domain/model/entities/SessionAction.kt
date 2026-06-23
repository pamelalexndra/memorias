package com.memorias.app.domain.model.entities

data class SessionAction(
    val photo: Photo,
    val action: SwipeAction,
    val timestampMs: Long = System.currentTimeMillis()
)