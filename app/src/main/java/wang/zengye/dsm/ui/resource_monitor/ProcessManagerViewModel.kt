package wang.zengye.dsm.ui.resource_monitor

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import wang.zengye.dsm.data.repository.SystemRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import javax.inject.Inject

/**
 * 进程数据
 */
data class ProcessItem(
    val pid: Int = 0,
    val name: String = "",
    val user: String = "",
    val cpu: Double = 0.0,
    val memory: Long = 0,
    val memoryPercent: Double = 0.0,
    val state: String = "",
    val command: String = ""
)

/**
 * 进程管理UI状态
 */
data class ProcessManagerUiState(
    override val isLoading: Boolean = false,
    val processes: List<ProcessItem> = emptyList(),
    override val error: String? = null,
    val sortBy: String = "cpu",
    val sortAsc: Boolean = false,
    val searchQuery: String = "",
    val isRefreshing: Boolean = false,
    val autoRefresh: Boolean = false
) : wang.zengye.dsm.ui.base.BaseState

/**
 * 进程管理ViewModel
 */
@HiltViewModel
class ProcessManagerViewModel @Inject constructor(
    private val systemRepository: SystemRepository
) : BaseViewModel<ProcessManagerUiState, ProcessManagerIntent, ProcessManagerEvent>() {

    companion object {
        private const val TAG = "ProcessManagerViewModel"
    }

    private val _uiState = MutableStateFlow(ProcessManagerUiState())
    override val state: StateFlow<ProcessManagerUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProcessManagerEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    private var refreshJob: Job? = null

    init {
        sendIntent(ProcessManagerIntent.LoadProcesses)
    }

    override suspend fun processIntent(intent: ProcessManagerIntent) {
        when (intent) {
            is ProcessManagerIntent.LoadProcesses -> loadProcesses()
            is ProcessManagerIntent.Refresh -> refresh()
            is ProcessManagerIntent.SetSortBy -> setSortBy(intent.sortBy)
            is ProcessManagerIntent.SetSearchQuery -> setSearchQuery(intent.query)
            is ProcessManagerIntent.ToggleAutoRefresh -> toggleAutoRefresh()
        }
    }

    /**
     * 加载进程列表
     */
    private suspend fun loadProcesses() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        fetchProcesses()
    }

    private suspend fun fetchProcesses() {
                    systemRepository.getProcessList().onSuccess { data ->            val processes = data.process?.map { proc ->
                ProcessItem(
                    pid = proc.pid ?: 0,
                    name = proc.name ?: "",
                    user = proc.user ?: "",
                    cpu = proc.cpu ?: 0.0,
                    memory = proc.memory ?: 0,
                    memoryPercent = proc.memoryPercent ?: 0.0,
                    state = proc.state ?: "",
                    command = proc.command ?: ""
                )
            } ?: emptyList()

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    isRefreshing = false,
                    processes = sortProcesses(processes, state.sortBy, state.sortAsc)
                )
            }
        }.onFailure { error ->
            Log.e(TAG, "ProcessList API error", error)
            _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = error.message) }
            _events.emit(ProcessManagerEvent.Error(error.message ?: "Failed to load processes"))
        }
    }

    private fun sortProcesses(processes: List<ProcessItem>, sortBy: String, asc: Boolean): List<ProcessItem> {
        val sorted = when (sortBy) {
            "cpu" -> processes.sortedByDescending { it.cpu }
            "memory" -> processes.sortedByDescending { it.memory }
            "name" -> processes.sortedBy { it.name.lowercase() }
            "pid" -> processes.sortedBy { it.pid }
            else -> processes
        }
        return if (asc) sorted.reversed() else sorted
    }

    /**
     * 设置排序方式
     */
    private fun setSortBy(sortBy: String) {
        _uiState.update { state ->
            val newAsc = if (state.sortBy == sortBy) !state.sortAsc else false
            state.copy(
                sortBy = sortBy,
                sortAsc = newAsc,
                processes = sortProcesses(state.processes, sortBy, newAsc)
            )
        }
    }

    /**
     * 设置搜索关键字
     */
    private fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * 切换自动刷新
     */
    private fun toggleAutoRefresh() {
        val newValue = !_uiState.value.autoRefresh
        _uiState.update { it.copy(autoRefresh = newValue) }

        if (newValue) {
            startAutoRefresh()
        } else {
            stopAutoRefresh()
        }
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (isActive) {
                delay(3000)
                _uiState.update { it.copy(isRefreshing = true) }
                fetchProcesses()
            }
        }
    }

    private fun stopAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = null
    }

    /**
     * 手动刷新
     */
    private suspend fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        fetchProcesses()
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }
}
