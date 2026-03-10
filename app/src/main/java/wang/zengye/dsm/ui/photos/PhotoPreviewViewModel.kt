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
import wang.zengye.dsm.data.model.PhotoItem
import wang.zengye.dsm.data.repository.PhotoRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import javax.inject.Inject

/**
 * 照片预览UI状态
 */
data class PhotoPreviewUiState(
    override val isLoading: Boolean = false,
    val photo: PhotoItem? = null,
    override val error: String? = null,
    val showInfo: Boolean = false,
    val showMenu: Boolean = false
) : wang.zengye.dsm.ui.base.BaseState

/**
 * 照片预览ViewModel
 */
@HiltViewModel
class PhotoPreviewViewModel @Inject constructor(
    private val photoRepository: PhotoRepository
) : BaseViewModel<PhotoPreviewUiState, PhotoPreviewIntent, PhotoPreviewEvent>() {

    private val _state = MutableStateFlow(PhotoPreviewUiState())
    override val state: StateFlow<PhotoPreviewUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PhotoPreviewEvent>(extraBufferCapacity = 10)
    override val events = _events.asSharedFlow()

    override suspend fun processIntent(intent: PhotoPreviewIntent) {
        when (intent) {
            is PhotoPreviewIntent.LoadPhoto -> loadPhoto(intent.photoId)
            is PhotoPreviewIntent.SetPhoto -> setPhoto(intent.photo)
            is PhotoPreviewIntent.ToggleInfo -> toggleInfo()
            is PhotoPreviewIntent.ToggleMenu -> toggleMenu()
        }
    }

    private suspend fun loadPhoto(photoId: Long) {
        _state.update { it.copy(isLoading = true, error = null) }

        // 通过 API 获取照片详情
        photoRepository.getPhotos(limit = 500)
            .onSuccess { photos ->
                val photo = photos.find { it.id == photoId }
                if (photo != null) {
                    _state.update { it.copy(photo = photo, isLoading = false) }
                } else {
                    _state.update { it.copy(error = "Photo not found", isLoading = false) }
                    _events.emit(PhotoPreviewEvent.ShowError("Photo not found"))
                }
            }
            .onFailure { error ->
                _state.update { it.copy(error = error.message, isLoading = false) }
                _events.emit(PhotoPreviewEvent.ShowError(error.message ?: "加载失败"))
            }
    }

    private fun setPhoto(photo: PhotoItem) {
        _state.update { it.copy(photo = photo) }
    }

    private fun toggleInfo() {
        _state.update { it.copy(showInfo = !it.showInfo) }
    }

    private fun toggleMenu() {
        _state.update { it.copy(showMenu = !it.showMenu) }
    }
}
