package wang.zengye.dsm.ui.photos

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wang.zengye.dsm.data.model.AlbumItem
import wang.zengye.dsm.data.model.PhotoItem
import wang.zengye.dsm.data.repository.PhotoRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import javax.inject.Inject

data class AlbumDetailUiState(
    override val isLoading: Boolean = false,
    val album: AlbumItem? = null,
    val photos: List<PhotoItem> = emptyList(),
    override val error: String? = null,
    val showDeleteDialog: Boolean = false,
    val isDeleting: Boolean = false
) : wang.zengye.dsm.ui.base.BaseState

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    private val photoRepository: PhotoRepository
) : BaseViewModel<AlbumDetailUiState, AlbumDetailIntent, AlbumDetailEvent>() {

    private val _state = MutableStateFlow(AlbumDetailUiState())
    override val state: StateFlow<AlbumDetailUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AlbumDetailEvent>(extraBufferCapacity = 10)
    override val events = _events.asSharedFlow()

    override suspend fun processIntent(intent: AlbumDetailIntent) {
        when (intent) {
            is AlbumDetailIntent.LoadAlbumPhotos -> loadAlbumPhotos(intent.albumId)
            is AlbumDetailIntent.SetAlbum -> setAlbum(intent.album)
            is AlbumDetailIntent.ShowDeleteDialog -> showDeleteDialog()
            is AlbumDetailIntent.HideDeleteDialog -> hideDeleteDialog()
            is AlbumDetailIntent.DeleteAlbum -> deleteAlbum()
        }
    }

    private suspend fun loadAlbumPhotos(albumId: Long) {
        _state.update { it.copy(isLoading = true, error = null) }

        photoRepository.getAlbumPhotos(albumId)
            .onSuccess { photos ->
                val sortedPhotos = photos.sortedByDescending { it.time }
                _state.update {
                    it.copy(
                        photos = sortedPhotos,
                        isLoading = false
                    )
                }
            }
            .onFailure { error ->
                _state.update { it.copy(error = error.message, isLoading = false) }
                _events.emit(AlbumDetailEvent.ShowError(error.message ?: "加载失败"))
            }
    }

    private fun setAlbum(album: AlbumItem) {
        _state.update { it.copy(album = album) }
    }

    private fun showDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = true) }
    }

    private fun hideDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = false) }
    }

    private suspend fun deleteAlbum() {
        val albumId = _state.value.album?.id ?: return

        _state.update { it.copy(isDeleting = true) }

        photoRepository.deleteAlbum(albumId)
            .onSuccess {
                _state.update { it.copy(isDeleting = false, showDeleteDialog = false) }
                _events.emit(AlbumDetailEvent.DeleteSuccess)
            }
            .onFailure { error ->
                _state.update { it.copy(isDeleting = false, error = error.message) }
                _events.emit(AlbumDetailEvent.ShowError(error.message ?: "删除失败"))
            }
    }
}
