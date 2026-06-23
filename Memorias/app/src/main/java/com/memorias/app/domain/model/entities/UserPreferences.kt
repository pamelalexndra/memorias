package com.memorias.app.domain.model.entities

data class UserPreferences(
    val deletionMode: DeletionMode = DeletionMode.TRASH,
    val sessionSize: SessionSize = SessionSize.NORMAL,
    val notificationEnabled: Boolean = true,
    val notificationHour: Int = 8,
    val theme: AppTheme = AppTheme.SYSTEM,
    val showSwipeHints: Boolean = true,
    val onboardingSeen: Boolean = false
)