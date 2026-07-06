package com.example.pamelapp.domain.repository

import com.example.pamelapp.domain.model.DeleteMode

interface SettingsRepository {
    fun getDeleteMode(): DeleteMode
    fun setDeleteMode(mode: DeleteMode)
}