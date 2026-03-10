package wang.zengye.dsm.ui.file

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wang.zengye.dsm.R
import wang.zengye.dsm.data.model.FileInfoDto
import wang.zengye.dsm.data.repository.FileRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import wang.zengye.dsm.util.appString
import javax.inject.Inject

/**
 * 文件详情 UI 模型 - 独立于 DTO
 */
data class FileDetailItem(
    val name: String = "",
    val path: String = "",
    val isDir: Boolean = false,
    val size: Long = 0,
    val modifiedTime: Long = 0,
    val createdTime: Long = 0,
    val accessedTime: Long = 0,
    val owner: String = "",
    val realPath: String = "",
    val permission: FilePermissionInfo? = null
)

data class FilePermissionInfo(
    val read: Boolean = false,
    val write: Boolean = false,
    val execute: Boolean = false,
    val delete: Boolean = false,
    val posix: String = ""
)

/**
 * DTO 转 UI 模型
 */
private fun FileInfoDto.toFileDetailItem(): FileDetailItem {
    return FileDetailItem(
        name = name,
        path = path,
        isDir = isdir,
        size = additional?.size ?: 0,
        modifiedTime = additional?.time?.mTime ?: 0,
        createdTime = additional?.time?.cTime ?: 0,
        accessedTime = additional?.time?.aTime ?: 0,
        owner = additional?.owner?.name ?: "",
        realPath = additional?.realPath ?: "",
        permission = additional?.perm?.let { perm ->
            FilePermissionInfo(
                read = perm.acl?.read ?: false,
                write = perm.acl?.write ?: false,
                execute = perm.acl?.exec ?: false,
                delete = perm.acl?.del ?: false,
                posix = perm.posix
            )
        }
    )
}

data class FileDetailUiState(
    override val isLoading: Boolean = false,
    val fileInfo: FileDetailItem? = null,
    override val error: String? = null,
    val isDeleting: Boolean = false,
    val isRenaming: Boolean = false,
    val showRenameDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val operationMessage: String? = null,
    val isFavorite: Boolean = false,
    val isAddingFavorite: Boolean = false,
    val showShareDialog: Boolean = false,
    val shareUrl: String? = null,
    val isCreatingShare: Boolean = false
) : wang.zengye.dsm.ui.base.BaseState

@HiltViewModel
class FileDetailViewModel @Inject constructor(
    private val fileRepository: FileRepository
) : BaseViewModel<FileDetailUiState, FileDetailIntent, FileDetailEvent>() {

    companion object {
        private const val TAG = "FileDetailViewModel"
    }

    private val _state = MutableStateFlow(FileDetailUiState())
    override val state: StateFlow<FileDetailUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<FileDetailEvent>(extraBufferCapacity = 1)
    override val events = _events.asSharedFlow()

    private var currentPath: String? = null

    override suspend fun processIntent(intent: FileDetailIntent) {
        when (intent) {
            is FileDetailIntent.LoadFileDetail -> loadFileDetail(intent.path)
            is FileDetailIntent.RenameFile -> renameFile(intent.path, intent.newName)
            is FileDetailIntent.DeleteFile -> deleteFile(intent.path)
            is FileDetailIntent.ToggleFavorite -> toggleFavorite(intent.path, intent.name)
            is FileDetailIntent.CreateShareLink -> createShareLink(intent.path)
            is FileDetailIntent.ShowRenameDialog -> _state.update { it.copy(showRenameDialog = true) }
            is FileDetailIntent.HideRenameDialog -> _state.update { it.copy(showRenameDialog = false) }
            is FileDetailIntent.ShowDeleteDialog -> _state.update { it.copy(showDeleteDialog = true) }
            is FileDetailIntent.HideDeleteDialog -> _state.update { it.copy(showDeleteDialog = false) }
            is FileDetailIntent.ShowShareDialog -> _state.update { it.copy(showShareDialog = true) }
            is FileDetailIntent.HideShareDialog -> _state.update { it.copy(showShareDialog = false, shareUrl = null) }
            is FileDetailIntent.DownloadFile -> downloadFile()
            is FileDetailIntent.ClearMessage -> _state.update { it.copy(operationMessage = null) }
        }
    }

    private suspend fun loadFileDetail(path: String) {
        currentPath = path
        _state.update { it.copy(isLoading = true, error = null) }

        fileRepository.getFileInfo(path)
            .onSuccess { response ->
                val fileInfo = response.data?.files?.firstOrNull()?.toFileDetailItem()
                _state.update { it.copy(fileInfo = fileInfo, isLoading = false) }
            }
            .onFailure { exception ->
                val errorMsg = exception.message ?: appString(R.string.dashboard_load_failed)
                _state.update { it.copy(error = errorMsg, isLoading = false) }
                _events.emit(FileDetailEvent.ShowError(errorMsg))
            }
    }

    private suspend fun renameFile(path: String, newName: String) {
        _state.update { it.copy(isRenaming = true) }

        fileRepository.rename(path, newName)
            .onSuccess {
                _state.update {
                    it.copy(
                        isRenaming = false,
                        showRenameDialog = false,
                        operationMessage = appString(R.string.file_rename_success)
                    )
                }
                // 重新加载文件信息
                val parentPath = path.substringBeforeLast("/")
                val newPath = "$parentPath/$newName"
                loadFileDetail(newPath)
                _events.emit(FileDetailEvent.FileRenamed)
            }
            .onFailure { exception ->
                _state.update {
                    it.copy(
                        isRenaming = false,
                        operationMessage = exception.message ?: appString(R.string.file_rename_failed)
                    )
                }
            }
    }

    private suspend fun deleteFile(path: String) {
        _state.update { it.copy(isDeleting = true) }

        fileRepository.deleteStart(listOf(path))
            .onSuccess {
                _state.update {
                    it.copy(
                        isDeleting = false,
                        showDeleteDialog = false,
                        operationMessage = appString(R.string.file_delete_success)
                    )
                }
                _events.emit(FileDetailEvent.FileDeleted)
            }
            .onFailure { exception ->
                _state.update {
                    it.copy(
                        isDeleting = false,
                        operationMessage = appString(R.string.file_delete_failed_reason, exception.message ?: "")
                    )
                }
            }
    }

    private suspend fun toggleFavorite(path: String, name: String) {
        _state.update { it.copy(isAddingFavorite = true) }

        if (_state.value.isFavorite) {
            // 取消收藏
            fileRepository.favoriteDelete(path)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isFavorite = false,
                            isAddingFavorite = false,
                            operationMessage = appString(R.string.file_favorite_removed)
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isAddingFavorite = false,
                            operationMessage = exception.message ?: appString(R.string.file_favorite_remove_failed)
                        )
                    }
                }
        } else {
            // 添加收藏
            fileRepository.favoriteAdd(name, path)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isFavorite = true,
                            isAddingFavorite = false,
                            operationMessage = appString(R.string.file_favorite_added)
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isAddingFavorite = false,
                            operationMessage = exception.message ?: appString(R.string.file_favorite_add_failed)
                        )
                    }
                }
        }
    }

    private suspend fun downloadFile() {
        val fileInfo = _state.value.fileInfo ?: return
        _events.emit(FileDetailEvent.StartDownload(fileInfo.path, fileInfo.name))
    }

    private suspend fun createShareLink(path: String) {
        _state.update { it.copy(isCreatingShare = true) }

        fileRepository.shareCreate(listOf(path))
            .onSuccess { response ->
                val url = response.data?.links?.firstOrNull()?.url
                _state.update {
                    it.copy(
                        isCreatingShare = false,
                        shareUrl = url
                    )
                }
            }
            .onFailure { exception ->
                _state.update {
                    it.copy(
                        isCreatingShare = false,
                        operationMessage = appString(R.string.file_create_share_failed, exception.message ?: "")
                    )
                }
            }
    }
}