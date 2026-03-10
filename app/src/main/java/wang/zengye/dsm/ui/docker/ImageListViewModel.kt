package wang.zengye.dsm.ui.docker

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wang.zengye.dsm.data.repository.DockerRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import javax.inject.Inject

/**
 * Docker镜像数据
 */
data class DockerImageItem(
    val id: String = "",
    val repoTag: String = "",
    val repository: String = "",
    val tag: String = "",
    val size: Long = 0,
    val created: Long = 0,
    val isDsmImage: Boolean = false
)

/**
 * Docker镜像管理UI状态
 */
data class ImageListUiState(
    override val isLoading: Boolean = false,
    val images: List<DockerImageItem> = emptyList(),
    override val error: String? = null,
    val showDeleteDialog: Boolean = false,
    val selectedImage: DockerImageItem? = null,
    val isDeleting: Boolean = false
) : wang.zengye.dsm.ui.base.BaseState

// 迁移状态：[已完成]
// 说明：已使用 Moshi API，移除所有 Gson 操作
@HiltViewModel
class ImageListViewModel @Inject constructor(
    private val dockerRepository: DockerRepository
) : BaseViewModel<ImageListUiState, ImageListIntent, ImageListEvent>() {

    private val _uiState = MutableStateFlow(ImageListUiState())
    override val state: StateFlow<ImageListUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ImageListEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    init {
        sendIntent(ImageListIntent.LoadImages)
    }

    override suspend fun processIntent(intent: ImageListIntent) {
        when (intent) {
            is ImageListIntent.LoadImages -> loadImages()
            is ImageListIntent.ShowDeleteDialog -> showDeleteDialog(intent.image)
            is ImageListIntent.HideDialog -> hideDialog()
            is ImageListIntent.DeleteImage -> deleteImage()
        }
    }

    /**
     * 加载镜像列表
     */
    private suspend fun loadImages() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        dockerRepository.getImageList().fold(
            onSuccess = { response ->
                val images = response.images?.map { item ->
                    val repoTag = item.repoTags?.firstOrNull() ?: ""
                    val parts = repoTag.split(":")

                    DockerImageItem(
                        id = item.id ?: "",
                        repoTag = repoTag,
                        repository = parts.getOrElse(0) { "" },
                        tag = parts.getOrElse(1) { "latest" },
                        size = item.size ?: 0,
                        created = item.created ?: 0,
                        isDsmImage = item.isDsm ?: false
                    )
                } ?: emptyList()

                _uiState.update { it.copy(isLoading = false, images = images.sortedByDescending { it.created }) }
            },
            onFailure = { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        )
    }

    /**
     * 显示删除确认对话框
     */
    private fun showDeleteDialog(image: DockerImageItem) {
        _uiState.update { it.copy(showDeleteDialog = true, selectedImage = image) }
    }

    /**
     * 隐藏对话框
     */
    private fun hideDialog() {
        _uiState.update { it.copy(showDeleteDialog = false, selectedImage = null) }
    }

    /**
     * 删除镜像
     */
    private suspend fun deleteImage() {
        val image = _uiState.value.selectedImage ?: return

        _uiState.update { it.copy(isDeleting = true, error = null) }

        dockerRepository.deleteImage(image.id).fold(
            onSuccess = {
                _uiState.update { it.copy(isDeleting = false, showDeleteDialog = false) }
                sendIntent(ImageListIntent.LoadImages)
            },
            onFailure = { error ->
                _uiState.update { it.copy(isDeleting = false, error = error.message) }
            }
        )
    }
}
