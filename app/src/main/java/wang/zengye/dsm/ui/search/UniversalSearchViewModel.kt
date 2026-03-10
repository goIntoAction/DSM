package wang.zengye.dsm.ui.search

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wang.zengye.dsm.R
import wang.zengye.dsm.data.repository.FileRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import wang.zengye.dsm.util.appString
import javax.inject.Inject

/**
 * 搜索结果项
 */
data class SearchResultItem(
    val id: String,
    val name: String,
    val path: String,
    val type: SearchResultType,
    val size: Long = 0,
    val modified: Long = 0,
    val isDir: Boolean = false
)

enum class SearchResultType {
    FILE, PHOTO, ALBUM, CONTAINER
}

/**
 * 全局搜索UI状态
 */
data class UniversalSearchUiState(
    val query: String = "",
    override val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val results: List<SearchResultItem> = emptyList(),
    override val error: String? = null,
    val searchScope: SearchScope = SearchScope.ALL
) : wang.zengye.dsm.ui.base.BaseState

enum class SearchScope {
    ALL, FILES, PHOTOS
}

/**
 * 全局搜索ViewModel
 */
@HiltViewModel
class UniversalSearchViewModel @Inject constructor(
    private val fileRepository: FileRepository
) : BaseViewModel<UniversalSearchUiState, UniversalSearchIntent, UniversalSearchEvent>() {

    companion object {
        private const val TAG = "UniversalSearchViewModel"
    }

    private val _state = MutableStateFlow(UniversalSearchUiState())
    override val state: StateFlow<UniversalSearchUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<UniversalSearchEvent>(extraBufferCapacity = 1)
    override val events = _events.asSharedFlow()

    private var searchJob: Job? = null
    private var searchTaskId: String? = null

    override suspend fun processIntent(intent: UniversalSearchIntent) {
        when (intent) {
            is UniversalSearchIntent.SetQuery -> setQuery(intent.query)
            is UniversalSearchIntent.SetScope -> setScope(intent.scope)
            is UniversalSearchIntent.Clear -> clear()
        }
    }

    private suspend fun setQuery(query: String) {
        _state.update { it.copy(query = query) }

        // 防抖
        searchJob?.cancel()
        if (query.trim().isNotEmpty()) {
            searchJob = viewModelScope.launch {
                delay(500)
                performSearch(query.trim())
            }
        } else {
            _state.update { it.copy(results = emptyList(), error = null) }
        }
    }

    private suspend fun setScope(scope: SearchScope) {
        _state.update { it.copy(searchScope = scope) }
        if (_state.value.query.isNotEmpty()) {
            performSearch(_state.value.query)
        }
    }

    private suspend fun performSearch(query: String) {
        when (_state.value.searchScope) {
            SearchScope.ALL -> searchAll(query)
            SearchScope.FILES -> searchFiles(query)
            SearchScope.PHOTOS -> {
                _state.update { it.copy(isSearching = false, results = emptyList()) }
                _events.emit(UniversalSearchEvent.ShowError(appString(R.string.common_coming_soon)))
            }
        }
    }

    private suspend fun searchAll(query: String) {
        // 目前只搜索文件
        searchFiles(query)
    }

    private suspend fun searchFiles(query: String) {
        _state.update { it.copy(isSearching = true, error = null, results = emptyList()) }

        // 使用 Moshi API 开始搜索任务
        fileRepository.searchStart("/", query, true)
            .onSuccess { response ->
                searchTaskId = response.data?.taskid
                if (searchTaskId != null) {
                    pollSearchResults()
                } else {
                    _state.update { it.copy(isSearching = false, error = appString(R.string.search_error_task_id_failed)) }
                }
            }
            .onFailure { exception ->
                val errorMsg = exception.message ?: appString(R.string.search_failed)
                _state.update { it.copy(isSearching = false, error = appString(R.string.search_failed)) }
                _events.emit(UniversalSearchEvent.ShowError(errorMsg))
            }
    }

    private fun pollSearchResults() {
        searchJob = viewModelScope.launch {
            var finished = false
            var offset = 0
            val limit = 100
            val allResults = mutableListOf<SearchResultItem>()

            while (!finished) {
                delay(300)
                ensureActive()

                val taskId = searchTaskId ?: return@launch

                fileRepository.searchList(taskId, offset, limit)
                    .onSuccess { response ->
                        val data = response.data
                        val status = data?.status ?: ""

                        // 解析结果 - 使用 Moshi 模型
                        data?.items?.forEach { item ->
                            val file = item.file
                            if (file != null) {
                                allResults.add(SearchResultItem(
                                    id = file.path,
                                    name = file.name,
                                    path = file.path,
                                    type = SearchResultType.FILE,
                                    size = file.additional?.size ?: 0,
                                    modified = file.additional?.time?.mTime ?: 0,
                                    isDir = file.isdir
                                ))
                            }
                        }
                        offset += (data?.items?.size ?: 0)

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
                    results = allResults
                )
            }
            _events.emit(UniversalSearchEvent.SearchCompleted)
        }
    }

    private fun clear() {
        searchJob?.cancel()
        _state.update { UniversalSearchUiState() }
    }
}