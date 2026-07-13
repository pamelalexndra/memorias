package com.pamelapp.memorias.domain.repository.settingsRepository

import com.pamelapp.memorias.domain.model.DeleteMode

interface SettingsRepository {
  fun getDeleteMode(): DeleteMode
  fun setDeleteMode(mode: DeleteMode)
}