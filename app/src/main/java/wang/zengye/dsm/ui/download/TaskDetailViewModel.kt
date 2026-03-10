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
import wang.zengye.dsm.R
import wang.zengye.dsm.data.repository.DownloadRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import wang.zengye.dsm.util.appString
import javax.inject.Inject

/**
 * 下载任务详情数据
 */
data class DownloadTaskDetail(
    val id: String = "",
    val title: String = "",
    val type: String = "",
    val status: Int = 0,
    val size: Long = 0,
    val username: String = "",
    val destination: String = "",
    val uri: String = "",
    val createdTime: Long = 0,
    val completedTime: Long = 0,
    val sizeDownloaded: Long = 0,
    val speedDownload: Long = 0,
    val speedUpload: Long = 0,
    val progress: Float = 0f,
    val waitingSeconds: Int = 0
)

/**
 * Peer 数据
 */
data class PeerItem(
    val ip: String = "",
    val port: Int = 0,
    val client: String = "",
    val progress: Float = 0f,
    val speedDownload: Long = 0,
    val speedUpload: Long = 0
)

/**
 * 文件数据
 */
data class TaskFileItem(
    val index: Int = 0,
    val fileName: String = "",
    val size: Long = 0,
    val sizeDownloaded: Long = 0,
    val priority: String = "normal"
)

/**
 * UI 状态
 */
data class TaskDetailUiState(
    override val isLoading: Boolean = false,
    val task: DownloadTaskDetail? = null,
    override val error: String? = null,
    val trackers: List<TrackerItem> = emptyList(),
    val peers: List<PeerItem> = emptyList(),
    val files: List<TaskFileItem> = emptyList(),
    val isLoadingTrackers: Boolean = false,
    val isLoadingPeers: Boolean = false,
    val isLoadingFiles: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val isDeleting: Boolean = false
) : wang.zengye.dsm.ui.base.BaseState

// 迁移状态：[已完成]
// 说明：已使用 Moshi API，移除所有 Gson 操作
@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository
) : BaseViewModel<TaskDetailUiState, TaskDetailIntent, TaskDetailEvent>() {

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    override val state: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TaskDetailEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    override suspend fun processIntent(intent: TaskDetailIntent) {
        when (intent) {
            is TaskDetailIntent.LoadTaskDetail -> loadTaskDetail(intent.taskId)
            is TaskDetailIntent.LoadTrackers -> loadTrackers(intent.taskId)
            is TaskDetailIntent.LoadPeers -> loadPeers(intent.taskId)
            is TaskDetailIntent.LoadFiles -> loadFiles(intent.taskId)
            is TaskDetailIntent.ShowDeleteDialog -> showDeleteDialog()
            is TaskDetailIntent.HideDeleteDialog -> hideDeleteDialog()
            is TaskDetailIntent.DeleteTask -> deleteTask(intent.taskId, intent.onSuccess)
        }
    }

    private suspend fun loadTaskDetail(taskId: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        downloadRepository.getTaskInfo(taskId).fold(
            onSuccess = { response ->
                val detailResp = response
                val detail = detailResp.additional?.transfer

                val task = DownloadTaskDetail(
                    id = detailResp.id ?: "",
                    title = detailResp.title ?: "",
                    type = "", // 需要从其他API获取
                    status = detailResp.status ?: 0,
                    size = detailResp.size ?: 0,
                    username = "",
                    destination = "",
                    uri = "",
                    createdTime = (detailResp.createTime ?: 0).toLong(),
                    completedTime = (detailResp.completedTime ?: 0).toLong(),
                    sizeDownloaded = detail?.sizeDownloaded ?: 0,
                    speedDownload = detail?.speedDownload ?: 0,
                    speedUpload = detail?.speedUpload ?: 0,
                    progress = if (detailResp.size ?: 0 > 0) {
                        (detail?.sizeDownloaded ?: 0).toFloat() / (detailResp.size ?: 1)
                    } else 0f,
                    waitingSeconds = 0
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        task = task
                    )
                }

                // 加载 BT 额外信息
                loadTrackers(taskId)
                loadPeers(taskId)
                loadFiles(taskId)
            },
            onFailure = { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        )
    }

    private suspend fun loadTrackers(taskId: String) {
        _uiState.update { it.copy(isLoadingTrackers = true) }

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

                _uiState.update { it.copy(isLoadingTrackers = false, trackers = trackers) }
            },
            onFailure = {
                _uiState.update { it.copy(isLoadingTrackers = false) }
            }
        )
    }

    private suspend fun loadPeers(taskId: String) {
        _uiState.update { it.copy(isLoadingPeers = true) }

        downloadRepository.getBtPeerList(taskId).fold(
            onSuccess = { response ->
                val peers = response.peers?.map { peerResp ->
                    PeerItem(
                        ip = peerResp.address?.substringBefore(":") ?: "",
                        port = peerResp.address?.substringAfter(":")?.toIntOrNull() ?: 0,
                        client = peerResp.client ?: "",
                        progress = (peerResp.progress ?: 0.0).toFloat(),
                        speedDownload = peerResp.speedDownload ?: 0,
                        speedUpload = peerResp.speedUpload ?: 0
                    )
                } ?: emptyList()

                _uiState.update { it.copy(isLoadingPeers = false, peers = peers) }
            },
            onFailure = {
                _uiState.update { it.copy(isLoadingPeers = false) }
            }
        )
    }

    private suspend fun loadFiles(taskId: String) {
        _uiState.update { it.copy(isLoadingFiles = true) }

        downloadRepository.getBtFileList(taskId).fold(
            onSuccess = { response ->
                val files = response.files?.mapIndexed { index, fileResp ->
                    TaskFileItem(
                        index = index,
                        fileName = fileResp.name ?: "",
                        size = fileResp.size ?: 0,
                        sizeDownloaded = 0,
                        priority = when (fileResp.priority) {
                            1 -> "high"
                            0 -> "normal"
                            -1 -> "low"
                            else -> "normal"
                        }
                    )
                } ?: emptyList()

                _uiState.update { it.copy(isLoadingFiles = false, files = files) }
            },
            onFailure = {
                _uiState.update { it.copy(isLoadingFiles = false) }
            }
        )
    }

    private fun showDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    private fun hideDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    private suspend fun deleteTask(taskId: String, onSuccess: () -> Unit) {
        _uiState.update { it.copy(isDeleting = true) }

        downloadRepository.deleteTask(taskId).fold(
            onSuccess = {
                _uiState.update { it.copy(isDeleting = false, showDeleteDialog = false) }
                onSuccess()
            },
            onFailure = { error ->
                _uiState.update { it.copy(isDeleting = false, error = error.message) }
            }
        )
    }
}
