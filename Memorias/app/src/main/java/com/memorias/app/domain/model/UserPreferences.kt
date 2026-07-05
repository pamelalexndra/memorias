package com.memorias.app.domain.model

import com.memorias.app.domain.model.enums.AppTheme
import com.memorias.app.domain.model.enums.DeletionMode
import com.memorias.app.domain.model.enums.SessionSize

data class UserPreferences(
    val deletionMode: DeletionMode = DeletionMode.TRASH,
    val sessionSize: SessionSize = SessionSize.NORMAL,
    val notificationEnabled: Boolean = true,
    val notificationHour: Int = 8,
    val theme: AppTheme = AppTheme.SYSTEM,
    val showSwipeHints: Boolean = true,
    val onboardingSeen: Boolean = false
)