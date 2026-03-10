package wang.zengye.dsm.ui.logcenter

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import wang.zengye.dsm.R
import wang.zengye.dsm.data.repository.SystemRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import javax.inject.Inject

/**
 * 日志条目
 */
data class LogEntry(
    val id: String = "",
    val time: String = "",
    val level: String = "",
    val category: String = "",
    val message: String = "",
    val user: String = ""
)

/**
 * 日志中心UI状态
 */
data class LogCenterUiState(
    override val isLoading: Boolean = true,
    override val error: String? = null,
    val recentLogs: List<LogEntry> = emptyList(),
    val logs: List<LogEntry> = emptyList(),
    val filteredLogs: List<LogEntry> = emptyList(),
    val selectedLogType: Int = 0,
    val logTypes: List<String> = listOf("system", "connection", "file_transfer", "disk"),
    val logTypeNames: List<Int> = listOf(R.string.log_type_system, R.string.log_type_connection, R.string.log_type_file_transfer, R.string.log_type_disk),
    val errorCount: Int = 0,
    val warnCount: Int = 0,
    val infoCount: Int = 0,
    val selectedLevel: String? = null,
    val searchQuery: String = "",
    val levelOptions: List<Int> = listOf(R.string.log_level_all, R.string.log_level_error, R.string.log_level_warning, R.string.log_level_info),
    val histories: List<LogEntry> = emptyList(),
    val isLoadingHistory: Boolean = true
) : wang.zengye.dsm.ui.base.BaseState

@HiltViewModel
class LogCenterViewModel @Inject constructor(
    private val systemRepository: SystemRepository
) : BaseViewModel<LogCenterUiState, LogCenterIntent, LogCenterEvent>() {

    companion object {
        private const val TAG = "LogCenterViewModel"
    }

    private val _uiState = MutableStateFlow(LogCenterUiState())
    override val state: StateFlow<LogCenterUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LogCenterEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    init {
        sendIntent(LogCenterIntent.LoadRecentLogs)
    }

    override suspend fun processIntent(intent: LogCenterIntent) {
        when (intent) {
            is LogCenterIntent.LoadRecentLogs -> loadRecentLogs()
            is LogCenterIntent.LoadLogs -> loadLogs(intent.logTypeIndex)
            is LogCenterIntent.Refresh -> refresh()
            is LogCenterIntent.SetLevelFilter -> setLevelFilter(intent.level)
            is LogCenterIntent.SetSearchQuery -> setSearchQuery(intent.query)
            is LogCenterIntent.LoadHistories -> loadHistories()
        }
    }

    private suspend fun loadRecentLogs() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        systemRepository.getLatestLogs(start = 0, limit = 50).onSuccess { response ->
            val logs = response.data?.items?.map { item ->
                LogEntry(
                    id = item.id ?: "",
                    time = item.time ?: "",
                    level = item.level ?: "",
                    category = item.logType ?: "",
                    message = item.description ?: "",
                    user = item.who ?: ""
                )
            } ?: emptyList()

            val errorCount = logs.count { it.level == "error" }
            val warnCount = logs.count { it.level == "warn" }
            val infoCount = logs.count { it.level == "info" }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    recentLogs = logs,
                    errorCount = errorCount,
                    warnCount = warnCount,
                    infoCount = infoCount
                )
            }
        }.onFailure { error ->
            Log.e(TAG, "Load recent logs error", error)
            _uiState.update { it.copy(isLoading = false, error = error.message) }
            _events.emit(LogCenterEvent.Error(error.message ?: "Failed to load logs"))
        }
    }

    private suspend fun loadLogs(logTypeIndex: Int) {
        _uiState.update { it.copy(isLoading = true, selectedLogType = logTypeIndex) }

        val logType = _uiState.value.logTypes.getOrElse(logTypeIndex) { "system" }

        systemRepository.getLogs(start = 0, limit = 50, logType = logType).onSuccess { response ->
            val logs = response.data?.items?.map { item ->
                LogEntry(
                    id = item.id ?: "",
                    time = item.time ?: "",
                    level = when (item.level) {
                        "warn" -> "warning"
                        "err", "error" -> "error"
                        else -> item.level ?: ""
                    },
                    category = item.logType ?: "",
                    message = item.description ?: "",
                    user = item.who ?: ""
                )
            } ?: emptyList()

            _uiState.update { it.copy(isLoading = false, logs = logs) }
            applyFilters()
        }.onFailure { error ->
            Log.e(TAG, "Load logs error", error)
            _uiState.update { it.copy(isLoading = false, error = error.message) }
        }
    }

    private suspend fun refresh() {
        loadRecentLogs()
    }

    private fun setLevelFilter(level: String?) {
        _uiState.update { it.copy(selectedLevel = level) }
        applyFilters()
    }

    private fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    private fun applyFilters() {
        val logs = _uiState.value.logs
        val selectedLevel = _uiState.value.selectedLevel
        val searchQuery = _uiState.value.searchQuery

        val filtered = logs.filter { log ->
            val levelMatch = selectedLevel == null || when (selectedLevel) {
                "log_level_error" -> log.level == "error" || log.level == "err"
                "log_level_warning" -> log.level == "warning" || log.level == "warn"
                "log_level_info" -> log.level == "info"
                else -> true
            }
            val searchMatch = searchQuery.isEmpty() ||
                log.message.contains(searchQuery, ignoreCase = true) ||
                log.category.contains(searchQuery, ignoreCase = true) ||
                log.user.contains(searchQuery, ignoreCase = true)
            levelMatch && searchMatch
        }

        _uiState.update { it.copy(filteredLogs = filtered) }
    }

    private suspend fun loadHistories() {
        _uiState.update { it.copy(isLoadingHistory = true) }

        systemRepository.getLogHistory().onSuccess { response ->
            val histories = response.data?.items?.map { item ->
                LogEntry(
                    id = item.id ?: "",
                    time = item.time ?: "",
                    level = when (item.level) {
                        "0" -> "info"
                        "1" -> "warning"
                        "2" -> "error"
                        else -> item.level ?: ""
                    },
                    category = item.type ?: "",
                    message = item.event ?: "",
                    user = item.user ?: ""
                )
            } ?: emptyList()

            _uiState.update { it.copy(isLoadingHistory = false, histories = histories) }
        }.onFailure { error ->
            Log.e(TAG, "Load history error", error)
            _uiState.update { it.copy(isLoadingHistory = false) }
        }
    }
}
