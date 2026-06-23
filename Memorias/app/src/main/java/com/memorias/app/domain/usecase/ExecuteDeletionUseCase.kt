package com.memorias.app.domain.usecase

import com.memorias.app.domain.model.entities.DeletionQueue
import com.memorias.app.domain.model.enums.DeletionMode
import com.memorias.app.domain.repository.PhotoRepository
import com.memorias.app.domain.repository.PreferencesRepository
import com.memorias.app.domain.result.DomainResult

class ExecuteDeletionUseCase @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val preferencesRepository: PreferencesRepository,
) {
    suspend operator fun invoke(queue: DeletionQueue): DomainResult<android.app.PendingIntent> {
        if (queue.isEmpty) {
            return DomainResult.Error.Unknown(
                IllegalArgumentException("La cola de eliminación está vacía"),
            )
        }

        val prefs = preferencesRepository.get()

        return when (prefs.deletionMode) {
            DeletionMode.PERMANENT -> photoRepository.createDeleteRequest(queue.photoIds)
            DeletionMode.TRASH     -> photoRepository.createTrashRequest(queue.photoIds)
        }
    }
}