package wang.zengye.dsm.ui.file

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import wang.zengye.dsm.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import wang.zengye.dsm.data.model.FileInfoDto
import wang.zengye.dsm.data.repository.FileRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import wang.zengye.dsm.ui.base.BaseState
import wang.zengye.dsm.util.appString

data class FileItem(
    val name: String = "",
    val path: String = "",
    val isDir: Boolean = false,
    val size: Long = 0,
    val modified: Long = 0,
    val extension: String = "",
    val isHidden: Boolean = false,
    val owner: String = "",
    val permission: String = ""
) {
    val isFile: Boolean get() = !isDir
}

/**
 * 文件操作状态
 */
data class FileOperationState(
    val isOperating: Boolean = false,
    val operationType: String = "", // copy, move, delete, rename, compress, extract
    val progress: Float = 0f,
    val message: String = ""
)

data class FileUiState(
    val currentPath: String = "/",
    val files: List<FileItem> = emptyList(),
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val sortBy: String = "name",
    val sortAsc: Boolean = true,
    val selectedItems: Set<String> = emptySet(),
    val isSelectionMode: Boolean = false,
    val viewMode: String = "list", // list / grid
    val operationState: FileOperationState = FileOperationState(),
    val searchQuery: String = "",
    val isSearchMode: Boolean = false,
    val searchResults: List<FileItem> = emptyList()
) : BaseState

/**
 * FileViewModel
 * 迁移状态：[MVI 迁移完成]
 * 说明：已迁移到 MVI 架构，使用 Intent/State/Event 模式
 */
@HiltViewModel
class FileViewModel @Inject constructor(
    private val fileRepository: FileRepository
) : BaseViewModel<FileUiState, FileIntent, FileEvent>() {

    companion object {
        private const val TAG = "FileViewModel"
        private const val POLL_INTERVAL_MS = 500L
        private const val MAX_POLL_RETRIES = 1200 // 1200 × 500ms = 10 分钟超时
    }

    private val _state = MutableStateFlow(FileUiState())
    override val state: StateFlow<FileUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<FileEvent>(extraBufferCapacity = 1)
    override val events = _events.asSharedFlow()

    private var searchTaskId: String? = null
    private var operationJob: Job? = null
    private var searchJob: Job? = null

    init {
        sendIntent(FileIntent.LoadFiles("/"))
    }

    override suspend fun processIntent(intent: FileIntent) {
        when (intent) {
            is FileIntent.LoadFiles -> loadFiles(intent.path)
            is FileIntent.NavigateTo -> loadFiles(intent.path)
            is FileIntent.NavigateUp -> navigateUp()
            is FileIntent.ToggleSelection -> toggleSelection(intent.path)
            is FileIntent.ClearSelection -> clearSelection()
            is FileIntent.ToggleSelectAll -> toggleSelectAll()
            is FileIntent.EnterSelectionMode -> enterSelectionMode()
            is FileIntent.ExitSelectionMode -> exitSelectionMode()
            is FileIntent.ToggleViewMode -> toggleViewMode()
            is FileIntent.SortFiles -> sortFiles(intent.sortBy)
            is FileIntent.CreateFolder -> createFolder(intent.name)
            is FileIntent.Rename -> rename(intent.path, intent.newName)
            is FileIntent.Delete -> delete(intent.paths)
            is FileIntent.Copy -> copy(intent.paths, intent.destPath)
            is FileIntent.Move -> move(intent.paths, intent.destPath)
            is FileIntent.Compress -> compress(intent.paths, intent.destPath, intent.password)
            is FileIntent.Extract -> extract(intent.filePath, intent.destPath, intent.password)
            is FileIntent.Search -> search(intent.query)
            is FileIntent.ClearSearch -> clearSearch()
            is FileIntent.AddToFavorite -> addToFavorite(intent.path, intent.name)
            is FileIntent.CreateShareLink -> createShareLink(intent.path)
            is FileIntent.HandleFileClick -> handleFileClick(intent.path, intent.name, intent.isDir)
        }
    }

    private suspend fun loadFiles(path: String) {
        _state.update { it.copy(isLoading = true, error = null, currentPath = path, isSearchMode = false) }

        if (path == "/") {
            // 加载共享文件夹列表
            fileRepository.getShareList()
                .onSuccess { response ->
                    val files = response.data?.shares?.mapNotNull { it.toFileItem() } ?: emptyList()
                    Log.d(TAG, "Parsed ${files.size} shares")
                    _state.update { it.copy(files = files, isLoading = false) }
                }
                .onFailure { exception ->
                    val errorMsg = exception.message ?: appString(R.string.dashboard_load_failed)
                    _state.update { it.copy(error = errorMsg, isLoading = false) }
                    _events.emit(FileEvent.ShowError(errorMsg))
                }
        } else {
            // 加载文件列表
            fileRepository.getFileList(path)
                .onSuccess { response ->
                    val files = response.data?.files?.mapNotNull { it.toFileItem() } ?: emptyList()
                    Log.d(TAG, "Parsed ${files.size} files")
                    _state.update { it.copy(files = files, isLoading = false) }
                }
                .onFailure { exception ->
                    val errorMsg = exception.message ?: appString(R.string.dashboard_load_failed)
                    _state.update { it.copy(error = errorMsg, isLoading = false) }
                    _events.emit(FileEvent.ShowError(errorMsg))
                }
        }
    }

    /**
     * 将 Moshi 模型转换为 UI 模型
     */
    private fun FileInfoDto.toFileItem(): FileItem? {
        return try {
            FileItem(
                name = this.filename ?: this.name ?: "",
                path = this.path ?: "",
                isDir = this.isdir ?: false,
                size = this.additional?.size ?: 0,
                modified = this.additional?.time?.mTime ?: 0,
                extension = (this.filename ?: this.name ?: "").substringAfterLast(".", ""),
                isHidden = this.hidden ?: false,
                owner = this.additional?.owner?.name ?: "",
                permission = this.additional?.perm?.posix ?: ""
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing file item", e)
            null
        }
    }

    private suspend fun navigateUp() {
        val currentPath = _state.value.currentPath
        if (currentPath != "/") {
            val parentPath = currentPath.substringBeforeLast("/")
            loadFiles(if (parentPath.isEmpty()) "/" else parentPath)
        }
    }

    private fun toggleSelection(path: String) {
        _state.update { state ->
            val newSelection = state.selectedItems.toMutableSet()
            if (newSelection.contains(path)) {
                newSelection.remove(path)
                // 如果取消选择后没有选中项，退出选择模式
                if (newSelection.isEmpty()) {
                    state.copy(selectedItems = newSelection, isSelectionMode = false)
                } else {
                    state.copy(selectedItems = newSelection)
                }
            } else {
                newSelection.add(path)
                state.copy(selectedItems = newSelection, isSelectionMode = true)
            }
        }
    }

    private fun clearSelection() {
        _state.update { it.copy(selectedItems = emptySet(), isSelectionMode = false) }
    }

    private fun toggleSelectAll() {
        _state.update { state ->
            val allPaths = state.files.map { it.path }.toSet()
            if (state.selectedItems.size == allPaths.size && allPaths.isNotEmpty()) {
                // 已全选，取消全选
                state.copy(selectedItems = emptySet(), isSelectionMode = false)
            } else {
                // 全选
                state.copy(selectedItems = allPaths, isSelectionMode = true)
            }
        }
    }

    private fun enterSelectionMode() {
        _state.update { it.copy(isSelectionMode = true) }
    }

    private fun exitSelectionMode() {
        _state.update { it.copy(selectedItems = emptySet(), isSelectionMode = false) }
    }

    private fun toggleViewMode() {
        _state.update { state ->
            state.copy(viewMode = if (state.viewMode == "list") "grid" else "list")
        }
    }

    private fun sortFiles(sortBy: String) {
        _state.update { state ->
            val newSortAsc = if (state.sortBy == sortBy) !state.sortAsc else true
            val sortedFiles = when (sortBy) {
                "name" -> state.files.sortedWith(compareBy { it.name.lowercase() })
                "size" -> state.files.sortedBy { it.size }
                "modified" -> state.files.sortedBy { it.modified }
                "type" -> state.files.sortedWith(compareBy<FileItem> { it.isDir }.thenBy { it.extension })
                else -> state.files
            }.let { if (!newSortAsc) it.reversed() else it }

            state.copy(files = sortedFiles, sortBy = sortBy, sortAsc = newSortAsc)
        }
    }

    // ==================== 文件操作 ====================

    /**
     * 创建文件夹
     */
    private suspend fun createFolder(name: String) {
        _state.update { it.copy(operationState = FileOperationState(isOperating = true, operationType = "create", message = appString(R.string.file_creating_folder))) }

        val currentPath = _state.value.currentPath
        val result = fileRepository.createFolder(currentPath, name)

        result
            .onSuccess {
                _state.update { it.copy(operationState = FileOperationState()) }
                loadFiles(currentPath)
                _events.emit(FileEvent.OperationSuccess("create"))
            }
            .onFailure { exception ->
                _state.update { it.copy(operationState = FileOperationState()) }
                _events.emit(FileEvent.ShowError(exception.message ?: appString(R.string.file_create_failed)))
            }
    }

    /**
     * 重命名文件/文件夹
     */
    private suspend fun rename(path: String, newName: String) {
        _state.update { it.copy(operationState = FileOperationState(isOperating = true, operationType = "rename", message = appString(R.string.file_renaming))) }

        val result = fileRepository.rename(path, newName)

        result
            .onSuccess {
                _state.update { it.copy(operationState = FileOperationState()) }
                loadFiles(_state.value.currentPath)
                _events.emit(FileEvent.OperationSuccess("rename"))
            }
            .onFailure { exception ->
                _state.update { it.copy(operationState = FileOperationState()) }
                _events.emit(FileEvent.ShowError(exception.message ?: appString(R.string.file_rename_failed)))
            }
    }

    /**
     * 删除文件/文件夹
     */
    private suspend fun delete(paths: List<String>) {
        _state.update { it.copy(operationState = FileOperationState(isOperating = true, operationType = "delete", message = appString(R.string.file_deleting))) }

        val result = fileRepository.deleteStart(paths)

        result
            .onSuccess { response ->
                val taskId = response.data?.taskid
                if (taskId != null && taskId.isNotEmpty()) {
                    // 轮询任务状态
                    pollDeleteStatus(taskId)
                } else {
                    _state.update { it.copy(operationState = FileOperationState()) }
                    loadFiles(_state.value.currentPath)
                    _events.emit(FileEvent.OperationSuccess("delete"))
                }
            }
            .onFailure { exception ->
                _state.update { it.copy(operationState = FileOperationState()) }
                _events.emit(FileEvent.ShowError(exception.message ?: appString(R.string.file_delete_failed)))
            }
    }

    private suspend fun pollDeleteStatus(taskId: String) {
        var isFinished = false
        var hasError = false
        var retryCount = 0

        while (!isFinished && !hasError && retryCount < MAX_POLL_RETRIES) {
            delay(POLL_INTERVAL_MS)
            retryCount++

            fileRepository.deleteStatus(taskId)
                .onSuccess { response ->
                    val data = response.data
                    val finished = data?.finished ?: false
                    val progress = data?.progress?.toFloat() ?: 0f

                    _state.update { it.copy(operationState = FileOperationState(isOperating = true, operationType = "delete", progress = progress * 100, message = appString(R.string.file_deleting_progress, (progress * 100).toInt()))) }

                    if (finished) {
                        isFinished = true
                        _state.update { it.copy(operationState = FileOperationState()) }
                        loadFiles(_state.value.currentPath)
                        _events.emit(FileEvent.OperationSuccess("delete"))
                    }
                }
                .onFailure { exception ->
                    hasError = true
                    _state.update { it.copy(operationState = FileOperationState()) }
                    _events.emit(FileEvent.ShowError(exception.message ?: appString(R.string.file_delete_failed)))
                }
        }

        if (!isFinished && !hasError) {
            _state.update { it.copy(operationState = FileOperationState()) }
            _events.emit(FileEvent.ShowError(appString(R.string.file_operation_timeout)))
        }
    }

    /**
     * 复制文件/文件夹
     */
    private suspend fun copy(paths: List<String>, destPath: String) {
        _state.update { it.copy(operationState = FileOperationState(isOperating = true, operationType = "copy", message = appString(R.string.file_copying))) }

        val result = fileRepository.copyMoveStart(paths, destPath, overwrite = true, removeSrc = false)

        result
            .onSuccess { response ->
                val taskId = response.data?.taskid
                if (taskId != null && taskId.isNotEmpty()) {
                    pollCopyMoveStatus(taskId, "copy")
                } else {
                    _state.update { it.copy(operationState = FileOperationState()) }
                    _events.emit(FileEvent.OperationSuccess("copy"))
                }
            }
            .onFailure { exception ->
                _state.update { it.copy(operationState = FileOperationState()) }
                _events.emit(FileEvent.ShowError(exception.message ?: appString(R.string.file_copy_failed)))
            }
    }

    /**
     * 移动文件/文件夹
     */
    private suspend fun move(paths: List<String>, destPath: String) {
        _state.update { it.copy(operationState = FileOperationState(isOperating = true, operationType = "move", message = appString(R.string.file_moving))) }

        val result = fileRepository.copyMoveStart(paths, destPath, overwrite = true, removeSrc = true)

        result
            .onSuccess { response ->
                val taskId = response.data?.taskid
                if (taskId != null && taskId.isNotEmpty()) {
                    pollCopyMoveStatus(taskId, "move")
                } else {
                    _state.update { it.copy(operationState = FileOperationState()) }
                    loadFiles(_state.value.currentPath)
                    _events.emit(FileEvent.OperationSuccess("move"))
                }
            }
            .onFailure { exception ->
                _state.update { it.copy(operationState = FileOperationState()) }
                _events.emit(FileEvent.ShowError(exception.message ?: appString(R.string.file_move_failed)))
            }
    }

    private suspend fun pollCopyMoveStatus(taskId: String, operationType: String) {
        var isFinished = false
        var hasError = false
        var retryCount = 0

        while (!isFinished && !hasError && retryCount < MAX_POLL_RETRIES) {
            delay(POLL_INTERVAL_MS)
            retryCount++

            fileRepository.copyMoveStatus(taskId)
                .onSuccess { response ->
                    val data = response.data
                    val finished = data?.finished ?: false
                    val progress = data?.progress?.toFloat() ?: 0f

                    _state.update { it.copy(operationState = FileOperationState(isOperating = true, operationType = operationType, progress = progress * 100, message = appString(R.string.file_processing_progress, (progress * 100).toInt()))) }

                    if (finished) {
                        isFinished = true
                        _state.update { it.copy(operationState = FileOperationState()) }
                        loadFiles(_state.value.currentPath)
                        _events.emit(FileEvent.OperationSuccess(operationType))
                    }
                }
                .onFailure { exception ->
                    hasError = true
                    _state.update { it.copy(operationState = FileOperationState()) }
                    _events.emit(FileEvent.ShowError(exception.message ?: appString(R.string.file_operation_failed)))
                }
        }

        if (!isFinished && !hasError) {
            _state.update { it.copy(operationState = FileOperationState()) }
            _events.emit(FileEvent.ShowError(appString(R.string.file_operation_timeout)))
        }
    }

    /**
     * 压缩文件/文件夹
     */
    private suspend fun compress(paths: List<String>, destPath: String, password: String? = null) {
        _state.update { it.copy(operationState = FileOperationState(isOperating = true, operationType = "compress", message = appString(R.string.file_compressing))) }

        val result = fileRepository.compressStart(paths, destPath, level = "normal", password = password)

        result
            .onSuccess { response ->
                val taskId = response.data?.taskid
                if (taskId != null && taskId.isNotEmpty()) {
                    pollCompressStatus(taskId)
                } else {
                    _state.update { it.copy(operationState = FileOperationState()) }
                    _events.emit(FileEvent.OperationSuccess("compress"))
                }
            }
            .onFailure { exception ->
                _state.update { it.copy(operationState = FileOperationState()) }
                _events.emit(FileEvent.ShowError(exception.message ?: appString(R.string.file_compress_failed)))
            }
    }

    private suspend fun pollCompressStatus(taskId: String) {
        var isFinished = false
        var hasError = false

        while (!isFinished && !hasError) {
            delay(500)

            fileRepository.compressStatus(taskId)
                .onSuccess { response ->
                    val data = response.data
                    val finished = data?.finished ?: false
                    val progress = data?.progress?.toFloat() ?: 0f

                    _state.update { it.copy(operationState = FileOperationState(isOperating = true, operationType = "compress", progress = progress * 100, message = appString(R.string.file_compressing_progress, (progress * 100).toInt()))) }

                    if (finished) {
                        isFinished = true
                        _state.update { it.copy(operationState = FileOperationState()) }
                        loadFiles(_state.value.currentPath)
                        _events.emit(FileEvent.OperationSuccess("compress"))
                    }
                }
                .onFailure { exception ->
                    hasError = true
                    _state.update { it.copy(operationState = FileOperationState()) }
                    _events.emit(FileEvent.ShowError(exception.message ?: appString(R.string.file_compress_failed)))
                }
        }
    }

    /**
     * 解压文件
     */
    private suspend fun extract(filePath: String, destPath: String, password: String? = null) {
        _state.update { it.copy(operationState = FileOperationState(isOperating = true, operationType = "extract", message = appString(R.string.file_extracting))) }

        val result = fileRepository.extractStart(filePath, destPath, password = password)

        result
            .onSuccess { response ->
                val taskId = response.data?.taskid
                if (taskId != null && taskId.isNotEmpty()) {
                    pollExtractStatus(taskId)
                } else {
                    _state.update { it.copy(operationState = FileOperationState()) }
                    _events.emit(FileEvent.OperationSuccess("extract"))
                }
            }
            .onFailure { exception ->
                _state.update { it.copy(operationState = FileOperationState()) }
                _events.emit(FileEvent.ShowError(exception.message ?: appString(R.string.file_extract_failed)))
            }
    }

    private suspend fun pollExtractStatus(taskId: String) {
        var isFinished = false
        var hasError = false

        while (!isFinished && !hasError) {
            delay(500)

            fileRepository.extractStatus(taskId)
                .onSuccess { response ->
                    val data = response.data
                    val finished = data?.finished ?: false
                    val progress = data?.progress?.toFloat() ?: 0f

                    _state.update { it.copy(operationState = FileOperationState(isOperating = true, operationType = "extract", progress = progress * 100, message = appString(R.string.file_extracting_progress, (progress * 100).toInt()))) }

                    if (finished) {
                        isFinished = true
                        _state.update { it.copy(operationState = FileOperationState()) }
                        loadFiles(_state.value.currentPath)
                        _events.emit(FileEvent.OperationSuccess("extract"))
                    }
                }
                .onFailure { exception ->
                    hasError = true
                    _state.update { it.copy(operationState = FileOperationState()) }
                    _events.emit(FileEvent.ShowError(exception.message ?: appString(R.string.file_extract_failed)))
                }
        }
    }

    // ==================== 搜索 ====================

    /**
     * 搜索文件
     */
    private suspend fun search(query: String) {
        if (query.isBlank()) {
            _state.update { it.copy(isSearchMode = false, searchResults = emptyList(), searchQuery = "") }
            return
        }

        _state.update { it.copy(isLoading = true, isSearchMode = true, searchQuery = query) }

        val currentPath = _state.value.currentPath
        val result = fileRepository.searchStart(currentPath, query)

        result
            .onSuccess { response ->
                searchTaskId = response.data?.taskid
                if (searchTaskId != null && searchTaskId!!.isNotEmpty()) {
                    fetchSearchResults()
                } else {
                    _state.update { it.copy(isLoading = false, error = appString(R.string.file_search_failed_vm)) }
                    _events.emit(FileEvent.ShowError(appString(R.string.file_search_failed_vm)))
                }
            }
            .onFailure { exception ->
                val errorMsg = exception.message ?: appString(R.string.file_search_failed_vm)
                _state.update { it.copy(isLoading = false, error = errorMsg) }
                _events.emit(FileEvent.ShowError(errorMsg))
            }
    }

    private suspend fun fetchSearchResults() {
        val taskId = searchTaskId ?: return

        // 等待搜索完成
        delay(1000)

        fileRepository.searchList(taskId)
            .onSuccess { response ->
                val files = response.data?.items?.mapNotNull { item ->
                    item.file?.toFileItem()
                } ?: emptyList()
                _state.update { it.copy(isLoading = false, searchResults = files) }
            }
            .onFailure { exception ->
                _state.update { it.copy(isLoading = false, error = exception.message) }
            }
    }

    private fun clearSearch() {
        searchJob?.cancel()
        searchJob = null
        searchTaskId = null
        _state.update { it.copy(isSearchMode = false, searchResults = emptyList(), searchQuery = "") }
    }

    // ==================== 下载链接 ====================

    /**
     * 获取文件下载URL
     */
    fun getDownloadUrl(path: String): String {
        return fileRepository.getDownloadUrl(path)
    }

    /**
     * 获取文件缩略图URL
     */
    fun getThumbnailUrl(path: String): String {
        return fileRepository.getThumbnailUrl(path)
    }

    // ==================== 收藏夹 ====================

    /**
     * 添加到收藏夹
     */
    private suspend fun addToFavorite(path: String, name: String) {
        fileRepository.favoriteAdd(name, path)
            .onSuccess {
                _events.emit(FileEvent.OperationSuccess("favorite"))
            }
            .onFailure { exception ->
                _events.emit(FileEvent.ShowError(exception.message ?: appString(R.string.file_add_failed)))
            }
    }

    // ==================== 共享链接 ====================

    /**
     * 创建共享链接
     */
    private suspend fun createShareLink(path: String) {
        fileRepository.shareCreate(listOf(path))
            .onSuccess { response ->
                val url = response.data?.links?.firstOrNull()?.url
                if (url != null) {
                    _events.emit(FileEvent.ShareLinkCreated(url))
                } else {
                    _events.emit(FileEvent.ShowError(appString(R.string.file_create_link_failed)))
                }
            }
            .onFailure { exception ->
                _events.emit(FileEvent.ShowError(exception.message ?: appString(R.string.file_create_failed)))
            }
    }

    // ==================== 文件点击处理 ====================

    /**
     * 处理文件点击
     * 将点击逻辑从Screen 移至 ViewModel，简化 remember lambda 依赖
     */
    private suspend fun handleFileClick(path: String, name: String, isDir: Boolean) {
        val state = _state.value

        if (state.isSelectionMode) {
            // 选择模式下，点击切换选中状态
            sendIntent(FileIntent.ToggleSelection(path))
        } else if (isDir) {
            // 目录，导航进入
            sendIntent(FileIntent.NavigateTo(path))
        } else {
            // 文件，根据类型导航
            val ext = name.substringAfterLast(".").lowercase()
            val url = fileRepository.getDownloadUrl(path)

            when {
                FileTypes.isImage(ext) -> {
                    _events.emit(FileEvent.NavigateToImageViewer(path))
                }
                FileTypes.isVideo(ext) -> {
                    _events.emit(FileEvent.NavigateToVideoPlayer(url, name))
                }
                FileTypes.isAudio(ext) -> {
                    _events.emit(FileEvent.NavigateToAudioPlayer(url, name))
                }
                FileTypes.isPdf(ext) -> {
                    _events.emit(FileEvent.NavigateToPdfViewer(url, name))
                }
                FileTypes.isText(ext) -> {
                    _events.emit(FileEvent.NavigateToTextEditor(url, name))
                }
                else -> {
                    // 不支持的文件类型，显示下载对话框
                    _events.emit(FileEvent.ShowDownloadDialog(path, name))
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        operationJob?.cancel()
        searchJob?.cancel()
    }
}
