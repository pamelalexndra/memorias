package com.example.pamelapp.domain.model

enum class DeleteMode {
    TRASH,
    PERMANENT;

    val isTrash: Boolean
        get() = this == TRASH

    val isPermanent: Boolean
        get() = this == PERMANENT
}