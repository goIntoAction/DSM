package wang.zengye.dsm.ui.file

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wang.zengye.dsm.data.repository.FileRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import javax.inject.Inject

/**
 * 文件搜索UI状态
 */
data class FileSearchUiState(
    val searchQuery: String = "",
    val searchPath: String = "/",
    val isRecursive: Boolean = true,
    val searchContent: Boolean = false,
    override val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val searchResults: List<FileItem> = emptyList(),
    override val error: String? = null,
    val searchProgress: Float = 0f
) : wang.zengye.dsm.ui.base.BaseState

/**
 * 文件搜索ViewModel
 */
@HiltViewModel
class FileSearchViewModel @Inject constructor(
    private val fileRepository: FileRepository
) : BaseViewModel<FileSearchUiState, FileSearchIntent, FileSearchEvent>() {

    companion object {
        private const val TAG = "FileSearchViewModel"
    }

    private val _state = MutableStateFlow(FileSearchUiState())
    override val state: StateFlow<FileSearchUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<FileSearchEvent>(extraBufferCapacity = 1)
    override val events = _events.asSharedFlow()

    private var searchTaskId: String? = null
    private var pollJob: Job? = null

    override suspend fun processIntent(intent: FileSearchIntent) {
        when (intent) {
            is FileSearchIntent.SetSearchQuery -> setSearchQuery(intent.query)
            is FileSearchIntent.SetSearchPath -> setSearchPath(intent.path)
            is FileSearchIntent.SetRecursive -> setRecursive(intent.enabled)
            is FileSearchIntent.SetSearchContent -> setSearchContent(intent.enabled)
            is FileSearchIntent.StartSearch -> startSearch()
            is FileSearchIntent.CancelSearch -> cancelSearch()
            is FileSearchIntent.ClearResults -> clearResults()
        }
    }

    private fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    private fun setSearchPath(path: String) {
        _state.update { it.copy(searchPath = path) }
    }

    private fun setRecursive(enabled: Boolean) {
        _state.update { it.copy(isRecursive = enabled) }
    }

    private fun setSearchContent(enabled: Boolean) {
        _state.update { it.copy(searchContent = enabled) }
    }

    /**
     * 开始搜索
     */
    private suspend fun startSearch() {
        val query = _state.value.searchQuery.trim()
        if (query.isEmpty()) return

        _state.update { it.copy(isSearching = true, error = null, searchResults = emptyList(), searchProgress = 0f) }

        // 开始搜索任务 - 使用 Moshi API
        fileRepository.searchStart(
            folderPath = _state.value.searchPath,
            pattern = query,
            recursive = _state.value.isRecursive
        )
            .onSuccess { response ->
                searchTaskId = response.data?.taskid
                if (searchTaskId != null) {
                    pollSearchResults()
                }
            }
            .onFailure { exception ->
                val errorMsg = exception.message ?: "搜索失败"
                _state.update { it.copy(isSearching = false, error = errorMsg) }
                _events.emit(FileSearchEvent.ShowError(errorMsg))
            }
    }

    /**
     * 轮询搜索结果
     */
    private fun pollSearchResults() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            var finished = false
            var offset = 0
            val limit = 500
            val allResults = mutableListOf<FileItem>()

            while (!finished) {
                delay(500) // 每500ms轮询一次
                ensureActive()

                val taskId = searchTaskId ?: return@launch
                fileRepository.searchList(taskId, offset, limit)
                    .onSuccess { response ->
                        val data = response.data
                        val status = data?.status ?: ""
                        val total = data?.total ?: 0
                        
                        // 解析文件项 - 使用 Moshi 模型
                        data?.items?.forEach { searchItem ->
                            val file = searchItem.file
                            if (file != null) {
                                allResults.add(FileItem(
                                    name = file.name ?: file.filename ?: "",
                                    path = file.path ?: "",
                                    isDir = file.isdir,
                                    size = file.additional?.size ?: 0,
                                    modified = file.additional?.time?.mTime ?: 0
                                ))
                            }
                        }
                        offset += (data?.items?.size ?: 0)

                        // 更新进度
                        if (total > 0) {
                            _state.update { it.copy(searchProgress = allResults.size.toFloat() / total) }
                        }

                        // 检查是否完成
                        if (status == "finished" || status == "broken") {
                            finished = true
                        }
                    }
                    .onFailure { exception ->
                        _state.update { it.copy(error = exception.message) }
                        finished = true
                    }
            }

            _state.update {
                it.copy(
                    isSearching = false,
                    searchResults = allResults,
                    searchProgress = 1f
                )
            }
            _events.emit(FileSearchEvent.SearchCompleted)
        }
    }

    /**
     * 取消搜索
     */
    private fun cancelSearch() {
        pollJob?.cancel()
        searchTaskId = null
        _state.update { it.copy(isSearching = false) }
    }

    /**
     * 清空结果
     */
    private fun clearResults() {
        _state.update { it.copy(searchResults = emptyList(), error = null) }
    }
}
