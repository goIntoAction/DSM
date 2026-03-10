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
import wang.zengye.dsm.data.model.DownloadTask
import wang.zengye.dsm.data.model.DownloadTaskAdditional
import wang.zengye.dsm.data.model.DownloadTransfer
import wang.zengye.dsm.data.repository.DownloadRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import javax.inject.Inject

data class DownloadStatistic(
    val totalTask: Int = 0,
    val downloading: Int = 0,
    val paused: Int = 0,
    val finished: Int = 0,
    val error: Int = 0
)

data class DownloadUiState(
    override val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val tasks: List<DownloadTask> = emptyList(),
    val filter: String = "all",
    override val error: String? = null,
    val statistic: DownloadStatistic = DownloadStatistic(),
    val downloadSpeed: Long = 0,
    val uploadSpeed: Long = 0
) : wang.zengye.dsm.ui.base.BaseState

// 迁移状态：[已完成]
// 说明：已使用 Moshi API，移除所有 Gson 操作
@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository
) : BaseViewModel<DownloadUiState, DownloadIntent, DownloadEvent>() {

    private val _uiState = MutableStateFlow(DownloadUiState())
    override val state: StateFlow<DownloadUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DownloadEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    init {
        sendIntent(DownloadIntent.LoadTasks)
    }

    override suspend fun processIntent(intent: DownloadIntent) {
        when (intent) {
            is DownloadIntent.LoadTasks -> loadTasks()
            is DownloadIntent.Refresh -> refresh()
            is DownloadIntent.SetFilter -> setFilter(intent.filter)
            is DownloadIntent.PauseTask -> pauseTask(intent.id)
            is DownloadIntent.ResumeTask -> resumeTask(intent.id)
            is DownloadIntent.DeleteTask -> deleteTask(intent.id, intent.forceComplete)
            is DownloadIntent.PauseAll -> pauseAll()
            is DownloadIntent.ResumeAll -> resumeAll()
        }
    }

    private suspend fun loadTasks() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        fetchTasks()
    }

    private suspend fun refresh() {
        _uiState.update { it.copy(isRefreshing = true, error = null) }
        fetchTasks()
    }

    private suspend fun fetchTasks() {
        downloadRepository.getTaskList().fold(
            onSuccess = { response ->
                val tasks = response.data?.task?.map { taskResp ->
                    DownloadTask(
                        id = taskResp.id ?: "",
                        title = taskResp.title ?: "",
                        size = taskResp.size ?: 0,
                        status = taskResp.status ?: 0,
                        additional = taskResp.additional?.let { additionalResp ->
                            DownloadTaskAdditional(
                                transfer = additionalResp.transfer?.let { transferResp ->
                                    DownloadTransfer(
                                        sizeDownloaded = transferResp.sizeDownloaded ?: 0,
                                        sizeUploaded = transferResp.sizeUploaded ?: 0,
                                        speedDownload = transferResp.speedDownload ?: 0,
                                        speedUpload = transferResp.speedUpload ?: 0,
                                        progress = transferResp.progress ?: 0.0
                                    )
                                }
                            )
                        }
                    )
                } ?: emptyList()

                // 使用 total 作为总任务数
                val statistic = DownloadStatistic(
                    totalTask = response.data?.total ?: tasks.size,
                    downloading = tasks.count { it.isDownloading },
                    paused = tasks.count { it.isPaused },
                    finished = tasks.count { it.isFinished },
                    error = tasks.count { it.isError }
                )

                var totalDownloadSpeed = 0L
                var totalUploadSpeed = 0L
                tasks.forEach { task ->
                    task.additional?.transfer?.let { transfer ->
                        totalDownloadSpeed += transfer.speedDownload
                        totalUploadSpeed += transfer.speedUpload
                    }
                }

                _uiState.update {
                    it.copy(
                        tasks = tasks,
                        statistic = statistic,
                        downloadSpeed = totalDownloadSpeed,
                        uploadSpeed = totalUploadSpeed,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            },
            onFailure = { error ->
                _uiState.update { it.copy(error = error.message, isLoading = false, isRefreshing = false) }
            }
        )
    }

    private fun setFilter(filter: String) {
        _uiState.update { it.copy(filter = filter) }
    }

    private suspend fun pauseTask(id: String) {
        downloadRepository.pauseTask(id).fold(
            onSuccess = { refresh() },
            onFailure = { }
        )
    }

    private suspend fun resumeTask(id: String) {
        downloadRepository.resumeTask(id).fold(
            onSuccess = { refresh() },
            onFailure = { }
        )
    }

    private suspend fun deleteTask(id: String, forceComplete: Boolean) {
        downloadRepository.deleteTask(id, forceComplete).fold(
            onSuccess = { refresh() },
            onFailure = { }
        )
    }

    private suspend fun pauseAll() {
        _uiState.value.tasks.filter { it.isDownloading }.forEach { task ->
            downloadRepository.pauseTask(task.id)
        }
        refresh()
    }

    private suspend fun resumeAll() {
        _uiState.value.tasks.filter { it.isPaused }.forEach { task ->
            downloadRepository.resumeTask(task.id)
        }
        refresh()
    }
}
