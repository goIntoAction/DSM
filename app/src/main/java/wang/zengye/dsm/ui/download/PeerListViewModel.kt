package wang.zengye.dsm.ui.download

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
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
 * Peer信息
 */
data class PeerInfo(
    val ip: String = "",
    val port: Int = 0,
    val client: String = "",
    val progress: Double = 0.0,
    val downloadSpeed: Long = 0,
    val uploadSpeed: Long = 0,
    val downloaded: Long = 0,
    val uploaded: Long = 0,
    val status: String = "" // connected, disconnected, etc.
)

/**
 * Peer列表UI状态
 */
data class PeerListUiState(
    override val isLoading: Boolean = false,
    val peers: List<PeerInfo> = emptyList(),
    override val error: String? = null,
    val taskId: String = "",
    val taskName: String = "",
    val autoRefresh: Boolean = false,
    val sortType: String = "progress" // progress, download, upload, ip
) : wang.zengye.dsm.ui.base.BaseState

// 迁移状态：[已完成]
// 说明：已使用 Moshi API，移除所有 Gson 操作
@HiltViewModel
class PeerListViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository
) : BaseViewModel<PeerListUiState, PeerListIntent, PeerListEvent>(), DefaultLifecycleObserver {

    companion object {
        private const val TAG = "PeerListViewModel"
    }

    private val _uiState = MutableStateFlow(PeerListUiState())
    override val state: StateFlow<PeerListUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PeerListEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    private var refreshJob: Job? = null

    init {
        // 初始不加载，等待SetTask调用
    }

    override suspend fun processIntent(intent: PeerListIntent) {
        when (intent) {
            is PeerListIntent.SetTask -> setTask(intent.taskId, intent.taskName)
            is PeerListIntent.LoadPeers -> loadPeers()
            is PeerListIntent.SetSortType -> setSortType(intent.sortType)
            is PeerListIntent.ToggleAutoRefresh -> toggleAutoRefresh()
        }
    }

    /**
     * 设置任务ID并加载Peer列表
     */
    private fun setTask(taskId: String, taskName: String) {
        _uiState.update { it.copy(taskId = taskId, taskName = taskName) }
        sendIntent(PeerListIntent.LoadPeers)
    }

    /**
     * 加载Peer列表
     */
    private suspend fun loadPeers() {
        val taskId = _uiState.value.taskId
        if (taskId.isEmpty()) return

        _uiState.update { it.copy(isLoading = true, error = null) }

        downloadRepository.getBtPeerList(taskId).fold(
            onSuccess = { response ->
                val peers = response.peers?.map { peerResp ->
                    PeerInfo(
                        ip = peerResp.address?.substringBefore(":") ?: "",
                        port = peerResp.address?.substringAfter(":")?.toIntOrNull() ?: 0,
                        client = peerResp.client ?: "",
                        progress = peerResp.progress ?: 0.0,
                        downloadSpeed = peerResp.speedDownload ?: 0,
                        uploadSpeed = peerResp.speedUpload ?: 0,
                        downloaded = 0,
                        uploaded = 0,
                        status = peerResp.status ?: "connected"
                    )
                } ?: emptyList()

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        peers = sortPeers(peers, state.sortType)
                    )
                }
            },
            onFailure = { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        )
    }

    private fun sortPeers(peers: List<PeerInfo>, sortType: String): List<PeerInfo> {
        return when (sortType) {
            "progress" -> peers.sortedByDescending { it.progress }
            "download" -> peers.sortedByDescending { it.downloadSpeed }
            "upload" -> peers.sortedByDescending { it.uploadSpeed }
            "ip" -> peers.sortedBy { it.ip }
            else -> peers
        }
    }

    /**
     * 设置排序方式
     */
    private fun setSortType(sortType: String) {
        _uiState.update { state ->
            state.copy(
                sortType = sortType,
                peers = sortPeers(state.peers, sortType)
            )
        }
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
            while (true) {
                delay(2000)
                loadPeers()
            }
        }
    }

    private fun stopAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = null
    }

    // ===== LifecycleObserver: 退后台/锁屏时停止自动刷新 =====
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        refreshJob?.cancel()
        refreshJob = null
        Log.d(TAG, "onStop: 自动刷新已停止")
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        startAutoRefresh()
        Log.d(TAG, "onStart: 自动刷新已恢复")
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }
}
