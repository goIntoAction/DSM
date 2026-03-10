package wang.zengye.dsm.ui.download

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
import wang.zengye.dsm.data.repository.DownloadRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import javax.inject.Inject

/**
 * Tracker数据
 */
data class TrackerItem(
    val url: String = "",
    val status: String = "", // working, error, updating, waiting
    val seeds: Int = 0,
    val peers: Int = 0
)

/**
 * Tracker管理UI状态
 */
data class TrackerManagerUiState(
    override val isLoading: Boolean = false,
    val trackers: List<TrackerItem> = emptyList(),
    override val error: String? = null,
    val showAddDialog: Boolean = false,
    val newTrackerUrl: String = "",
    val isAdding: Boolean = false
) : wang.zengye.dsm.ui.base.BaseState

// 迁移状态：[已完成]
// 说明：已使用 Moshi API，移除所有 Gson 操作
@HiltViewModel
class TrackerManagerViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository
) : BaseViewModel<TrackerManagerUiState, TrackerManagerIntent, TrackerManagerEvent>() {

    private val _uiState = MutableStateFlow(TrackerManagerUiState())
    override val state: StateFlow<TrackerManagerUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TrackerManagerEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    override suspend fun processIntent(intent: TrackerManagerIntent) {
        when (intent) {
            is TrackerManagerIntent.LoadTrackers -> loadTrackers(intent.taskId)
            is TrackerManagerIntent.ShowAddDialog -> showAddDialog()
            is TrackerManagerIntent.HideDialog -> hideDialog()
            is TrackerManagerIntent.UpdateNewTrackerUrl -> updateNewTrackerUrl(intent.url)
            is TrackerManagerIntent.AddTracker -> addTracker(intent.taskId)
        }
    }

    /**
     * 加载Tracker列表
     */
    private suspend fun loadTrackers(taskId: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        downloadRepository.getBtTrackerList(taskId).fold(
            onSuccess = { response ->
                val trackers = response.trackers?.map { trackerResp ->
                    TrackerItem(
                        url = trackerResp.url ?: "",
                        status = trackerResp.status ?: "",
                        seeds = trackerResp.seeds ?: 0,
                        peers = trackerResp.peers ?: 0
                    )
                } ?: emptyList()

                _uiState.update { it.copy(isLoading = false, trackers = trackers) }
            },
            onFailure = { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        )
    }

    /**
     * 显示添加对话框
     */
    private fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true, newTrackerUrl = "") }
    }

    /**
     * 隐藏对话框
     */
    private fun hideDialog() {
        _uiState.update { it.copy(showAddDialog = false, newTrackerUrl = "") }
    }

    /**
     * 更新新Tracker URL
     */
    private fun updateNewTrackerUrl(url: String) {
        _uiState.update { it.copy(newTrackerUrl = url) }
    }

    /**
     * 添加Tracker
     */
    private suspend fun addTracker(taskId: String) {
        val url = _uiState.value.newTrackerUrl.trim()
        if (url.isEmpty()) return

        // 支持多个tracker，用换行分隔
        val trackers = url.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        _uiState.update { it.copy(isAdding = true, error = null) }

        downloadRepository.addBtTracker(taskId, trackers).fold(
            onSuccess = {
                _uiState.update { it.copy(isAdding = false, showAddDialog = false) }
                loadTrackers(taskId)
            },
            onFailure = { e ->
                _uiState.update { it.copy(isAdding = false, error = e.message) }
            }
        )
    }
}
