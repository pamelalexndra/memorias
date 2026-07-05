package com.memorias.app.domain.model

import com.memorias.app.domain.model.enums.SwipeAction

data class SessionAction(
    val photo: Photo,
    val action: SwipeAction,
    val timestampMs: Long = System.currentTimeMillis()
)