package com.memorias.app.presentation.screen.onthisday

import com.memorias.app.domain.usecase.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memorias.app.domain.model.DeletionQueue
import com.memorias.app.domain.model.OnThisDayGroup
import com.memorias.app.domain.model.Photo
import com.memorias.app.domain.model.PhotoFilter
import com.memorias.app.domain.model.SessionAction
import com.memorias.app.domain.model.enums.SessionSize
import com.memorias.app.domain.model.enums.SwipeAction
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
class OnThisDayViewModel (
    private val getOnThisDayPhotos: GetOnThisDayPhotosUseCase,
    private val loadNextPage: LoadNextPhotoPageUseCase,
    private val addFavorite: AddFavoriteUseCase,
    private val executeDeletion: ExecuteDeletionUseCase,
    private val recordSession: RecordSessionUseCase,
    private val observePreferences: ObservePreferencesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnThisDayUiState())
    val uiState: StateFlow<OnThisDayUiState> = _uiState.asStateFlow()

    private val _events = Channel<OnThisDayUiEvent>(Channel.BUFFERED)
    val events: Flow<OnThisDayUiEvent> = _events.receiveAsFlow()

    private val undoStack = ArrayDeque<SessionAction>()

    init {
        viewModelScope.launch {
            val prefs = observePreferences().first()
            _uiState.update { it.copy(sessionSizeLimit = prefs.sessionSize.count) }
        }
        loadPhotos()
    }

    fun loadPhotos(filter: PhotoFilter = PhotoFilter.None) {
        viewModelScope.launch {
            _uiState.update { it.copy(status = OnThisDayUiState.Status.Loading, activeFilter = filter) }

            getOnThisDayPhotos(filter)
                .onSuccess { page ->
                    _uiState.update {
                        it.copy(
                            status = if (page.groups.isEmpty()) OnThisDayUiState.Status.Empty
                            else OnThisDayUiState.Status.Reviewing,
                            groups = page.groups,
                            selectedYearIndex = 0,
                            currentPhotoIndex = 0,
                            nextCursor = page.nextCursor,
                            hasMorePhotos = page.hasMore,
                            sessionStartMs = System.currentTimeMillis(),
                            photosReviewedSinceLastPause = 0,
                        )
                    }
                    undoStack.clear()
                }
                .onError { error ->
                    _uiState.update { it.copy(status = OnThisDayUiState.Status.Error(error.toString())) }
                }
        }
    }

    private fun loadMoreIfNeeded() {
        val state = _uiState.value
        if (!state.hasMorePhotos || state.isLoadingMore || state.nextCursor == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            loadNextPage(state.nextCursor, state.activeFilter)
                .onSuccess { page ->
                    _uiState.update { s ->
                        s.copy(
                            groups = mergeGroups(s.groups, page.groups),
                            nextCursor = page.nextCursor,
                            hasMorePhotos = page.hasMore,
                            isLoadingMore = false,
                        )
                    }
                }
                .onError {
                    _uiState.update { it.copy(isLoadingMore = false) }
                }
        }
    }

    private fun mergeGroups(
        current: List<OnThisDayGroup>,
        incoming: List<OnThisDayGroup>,
    ): List<OnThisDayGroup> {
        val byYear = current.associateBy { it.year }.toMutableMap()
        incoming.forEach { group ->
            val existing = byYear[group.year]
            byYear[group.year] = if (existing != null) {
                existing.copy(photos = existing.photos + group.photos)
            } else group
        }
        return byYear.values.sortedByDescending { it.year }
    }

    fun onSwipe(action: SwipeAction) {
        val photo = _uiState.value.currentPhoto ?: return

        viewModelScope.launch {
            when (action) {
                SwipeAction.KEEP -> {
                    _uiState.update { it.copy(sessionKept = it.sessionKept + 1) }
                }
                SwipeAction.FAVORITE -> {
                    addFavorite(photo)
                        .onSuccess {
                            _uiState.update { it.copy(sessionFavorited = it.sessionFavorited + 1) }
                        }
                        .onError {
                            _events.send(OnThisDayUiEvent.ShowSnackbar("No se pudo guardar el favorito"))
                            return@launch
                        }
                }
                SwipeAction.DELETE -> {
                    _uiState.update { s -> s.copy(deletionQueue = s.deletionQueue.add(photo)) }
                }
            }

            undoStack.addLast(SessionAction(photo = photo, action = action))

            advance()
        }
    }

    fun undoLastAction() {
        val lastAction = undoStack.removeLastOrNull() ?: return

        viewModelScope.launch {
            when (lastAction.action) {
                SwipeAction.KEEP -> {
                    _uiState.update { it.copy(sessionKept = (it.sessionKept - 1).coerceAtLeast(0)) }
                }
                SwipeAction.FAVORITE -> {
                    _uiState.update { it.copy(sessionFavorited = (it.sessionFavorited - 1).coerceAtLeast(0)) }
                }
                SwipeAction.DELETE -> {
                    _uiState.update { it.copy(deletionQueue = it.deletionQueue.remove(lastAction.photo)) }
                }
            }

            _uiState.update { s ->
                val newIndex = (s.currentPhotoIndex - 1).coerceAtLeast(0)
                s.copy(currentPhotoIndex = newIndex, status = OnThisDayUiState.Status.Reviewing)
            }
        }
    }

    fun selectYear(index: Int) {
        _uiState.update { it.copy(selectedYearIndex = index, currentPhotoIndex = 0) }
        undoStack.clear()
    }

    fun continueAfterPause() {
        _uiState.update {
            it.copy(status = OnThisDayUiState.Status.Reviewing, photosReviewedSinceLastPause = 0)
        }
    }

    fun showDeleteConfirmation() {
        if (_uiState.value.deletionQueue.isEmpty) return
        _uiState.update { it.copy(status = OnThisDayUiState.Status.Confirming) }
    }

    fun removeFromQueue(photo: Photo) {
        _uiState.update { it.copy(deletionQueue = it.deletionQueue.remove(photo)) }
    }

    fun confirmDeletion() {
        viewModelScope.launch {
            val queue = _uiState.value.deletionQueue
            executeDeletion(queue)
                .onSuccess { pendingIntent ->
                    _events.send(OnThisDayUiEvent.LaunchDeleteIntent(pendingIntent))
                }
                .onError {
                    _events.send(OnThisDayUiEvent.ShowSnackbar("Error al preparar la eliminación"))
                    _uiState.update { it.copy(status = OnThisDayUiState.Status.Reviewing) }
                }
        }
    }

    fun onDeleteConfirmed() {
        viewModelScope.launch {
            val freedBytes = _uiState.value.deletionQueue.totalBytes
            _uiState.update { it.copy(deletionQueue = DeletionQueue()) }
            finishSession(freedBytes = freedBytes)
        }
    }

    fun onDeleteCancelled() {
        _uiState.update { it.copy(status = OnThisDayUiState.Status.Reviewing) }
    }

    fun dismissConfirmation() {
        _uiState.update { it.copy(status = OnThisDayUiState.Status.Reviewing) }
    }

    private fun advance() {
        val state = _uiState.value
        val reviewedSincePause = state.photosReviewedSinceLastPause + 1

        val hitLimit = !SessionSize.entries.first { it.count == state.sessionSizeLimit }.isUnlimited &&
                reviewedSincePause >= state.sessionSizeLimit

        if (hitLimit && !state.isLastPhotoInGroup) {
            _uiState.update {
                it.copy(
                    status = OnThisDayUiState.Status.PausedAtLimit,
                    photosReviewedSinceLastPause = 0,
                )
            }
            return
        }

        if (state.isLastPhotoInGroup) {
            loadMoreIfNeeded()
            if (state.deletionQueue.isNotEmpty) {
                _uiState.update {
                    it.copy(status = OnThisDayUiState.Status.Confirming, photosReviewedSinceLastPause = reviewedSincePause)
                }
            } else {
                viewModelScope.launch { finishSession(freedBytes = 0L) }
            }
        } else {
            _uiState.update {
                it.copy(
                    currentPhotoIndex = it.currentPhotoIndex + 1,
                    photosReviewedSinceLastPause = reviewedSincePause,
                )
            }
        }
    }

    private suspend fun finishSession(freedBytes: Long) {
        val state = _uiState.value
        val duration = System.currentTimeMillis() - state.sessionStartMs

        recordSession(
            photosReviewed = state.sessionReviewed,
            photosDeleted = state.deletionQueue.size,
            photosFavorited = state.sessionFavorited,
            photosKept = state.sessionKept,
            spaceFreedBytes = freedBytes,
            durationMs = duration,
        )

        _uiState.update { it.copy(status = OnThisDayUiState.Status.Done) }
        undoStack.clear()
    }

    fun startNewSession() {
        loadPhotos()
    }
}