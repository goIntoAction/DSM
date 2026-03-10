package wang.zengye.dsm.ui.control_panel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import wang.zengye.dsm.data.repository.ControlPanelRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import javax.inject.Inject

/**
 * 媒体索引文件夹
 */
data class MediaIndexFolder(
    val path: String = "",
    val name: String = "",
    val enabled: Boolean = false,
    val fileCount: Int = 0
)

/**
 * 媒体索引状态
 */
data class MediaIndexStatus(
    val isIndexing: Boolean = false,
    val progress: Int = 0,
    val totalFiles: Int = 0,
    val indexedFiles: Int = 0,
    val lastIndexTime: Long = 0
)

/**
 * 媒体索引设置UI状态
 */
data class MediaIndexUiState(
    override val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    override val error: String? = null,
    val status: MediaIndexStatus = MediaIndexStatus(),
    val folders: List<MediaIndexFolder> = emptyList(),
    val autoIndex: Boolean = true,
    val indexVideo: Boolean = true,
    val indexPhoto: Boolean = true,
    val indexMusic: Boolean = true,
    val showReindexDialog: Boolean = false
) : wang.zengye.dsm.ui.base.BaseState

// 迁移状态：[已完成]
// 说明：已完全迁移到 Moshi API
@HiltViewModel
class MediaIndexViewModel @Inject constructor(
    private val controlPanelRepository: ControlPanelRepository
) : BaseViewModel<MediaIndexUiState, MediaIndexIntent, MediaIndexEvent>() {

    private val _state = MutableStateFlow(MediaIndexUiState())
    override val state: StateFlow<MediaIndexUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<MediaIndexEvent>(extraBufferCapacity = 1)
    override val events = _events.asSharedFlow()

    init {
        sendIntent(MediaIndexIntent.LoadMediaIndexStatus)
    }

    override suspend fun processIntent(intent: MediaIndexIntent) {
        when (intent) {
            is MediaIndexIntent.LoadMediaIndexStatus -> loadMediaIndexStatus()
            is MediaIndexIntent.ToggleAutoIndex -> toggleAutoIndex()
            is MediaIndexIntent.ToggleIndexVideo -> toggleIndexVideo()
            is MediaIndexIntent.ToggleIndexPhoto -> toggleIndexPhoto()
            is MediaIndexIntent.ToggleIndexMusic -> toggleIndexMusic()
            is MediaIndexIntent.ToggleFolder -> toggleFolder(intent.path)
            is MediaIndexIntent.ShowReindexDialog -> showReindexDialog()
            is MediaIndexIntent.HideReindexDialog -> hideReindexDialog()
            is MediaIndexIntent.StartReindex -> startReindex()
            is MediaIndexIntent.SaveSettings -> saveSettings()
        }
    }

    private suspend fun loadMediaIndexStatus() {
        _state.update { it.copy(isLoading = true, error = null) }

        controlPanelRepository.getMediaIndexStatus()
            .onSuccess { response ->
                val data = response.data
                val status = MediaIndexStatus(
                    isIndexing = data?.indexing ?: false,
                    progress = data?.progress ?: 0,
                    totalFiles = data?.total ?: 0,
                    indexedFiles = data?.indexed ?: 0,
                    lastIndexTime = data?.lastIndex ?: 0
                )

                val folders = data?.folders?.map { folder ->
                    MediaIndexFolder(
                        path = folder.path ?: "",
                        name = folder.name ?: "",
                        enabled = folder.enabled ?: false,
                        fileCount = folder.fileCount ?: 0
                    )
                } ?: emptyList()

                val settings = data?.settings
                val autoIndex = settings?.autoIndex ?: true
                val indexVideo = settings?.indexVideo ?: true
                val indexPhoto = settings?.indexPhoto ?: true
                val indexMusic = settings?.indexMusic ?: true

                _state.update {
                    it.copy(
                        isLoading = false,
                        status = status,
                        folders = folders,
                        autoIndex = autoIndex,
                        indexVideo = indexVideo,
                        indexPhoto = indexPhoto,
                        indexMusic = indexMusic
                    )
                }
            }
            .onFailure { exception ->
                _state.update {
                    it.copy(isLoading = false, error = exception.message)
                }
            }
    }

    private fun toggleAutoIndex() {
        _state.update { it.copy(autoIndex = !it.autoIndex) }
    }

    private fun toggleIndexVideo() {
        _state.update { it.copy(indexVideo = !it.indexVideo) }
    }

    private fun toggleIndexPhoto() {
        _state.update { it.copy(indexPhoto = !it.indexPhoto) }
    }

    private fun toggleIndexMusic() {
        _state.update { it.copy(indexMusic = !it.indexMusic) }
    }

    private fun toggleFolder(path: String) {
        _state.update { state ->
            state.copy(
                folders = state.folders.map { folder ->
                    if (folder.path == path) {
                        folder.copy(enabled = !folder.enabled)
                    } else {
                        folder
                    }
                }
            )
        }
    }

    private fun showReindexDialog() {
        _state.update { it.copy(showReindexDialog = true) }
    }

    private fun hideReindexDialog() {
        _state.update { it.copy(showReindexDialog = false) }
    }

    private suspend fun startReindex() {
        _state.update { it.copy(showReindexDialog = false, isSaving = true) }

        controlPanelRepository.reindexMedia()
            .onSuccess {
                _state.update { it.copy(isSaving = false) }
                _events.emit(MediaIndexEvent.ReindexSuccess)
                loadMediaIndexStatus()
            }
            .onFailure { exception ->
                _state.update { it.copy(isSaving = false) }
                _events.emit(MediaIndexEvent.ShowError(exception.message ?: "重建索引失败"))
            }
    }

    private suspend fun saveSettings() {
        _state.update { it.copy(isSaving = true) }

        controlPanelRepository.setMediaIndex(
            thumbQuality = "medium",
            mobileEnabled = _state.value.autoIndex
        )
            .onSuccess {
                _state.update { it.copy(isSaving = false) }
                _events.emit(MediaIndexEvent.SaveSuccess)
            }
            .onFailure { exception ->
                _state.update { it.copy(isSaving = false) }
                _events.emit(MediaIndexEvent.ShowError(exception.message ?: "保存失败"))
            }
    }
}
