package wang.zengye.dsm.ui.taskmanager

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import wang.zengye.dsm.data.repository.SystemRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import wang.zengye.dsm.util.SettingsManager
import javax.inject.Inject

data class ProcessInfo(
    val pid: Int = 0,
    val name: String = "",
    val user: String = "",
    val cpu: Double = 0.0,
    val memory: Double = 0.0,
    val status: String = ""
)

data class ServiceInfo(
    val name: String = "",
    val displayName: String = "",
    val status: String = "",
    val enabled: Boolean = false
)

data class TaskManagerUiState(
    override val isLoading: Boolean = true,
    override val error: String? = null,
    val processes: List<ProcessInfo> = emptyList(),
    val services: List<ServiceInfo> = emptyList(),
    val refreshDuration: Int = 5
) : wang.zengye.dsm.ui.base.BaseState

@HiltViewModel
class TaskManagerViewModel @Inject constructor(
    private val systemRepository: SystemRepository
) : BaseViewModel<TaskManagerUiState, TaskManagerIntent, TaskManagerEvent>(), DefaultLifecycleObserver {

    companion object {
        private const val TAG = "TaskManagerViewModel"
    }

    private val _uiState = MutableStateFlow(TaskManagerUiState())
    override val state: StateFlow<TaskManagerUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TaskManagerEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    private var refreshJob: Job? = null

    init {
        viewModelScope.launch {
            val duration = SettingsManager.refreshDuration.first()
            _uiState.update { it.copy(refreshDuration = duration) }
        }
        sendIntent(TaskManagerIntent.LoadData)
    }

    override suspend fun processIntent(intent: TaskManagerIntent) {
        when (intent) {
            is TaskManagerIntent.LoadData -> loadData()
            is TaskManagerIntent.Refresh -> loadDataOnce()
        }
    }

    private suspend fun loadData() {
        _uiState.update { it.copy(isLoading = false, error = null) }

        // 获取进程列表
        systemRepository.getProcessList().onSuccess { response ->
            val processList = response.process?.map { item ->
                ProcessInfo(
                    pid = item.pid ?: 0,
                    name = item.name ?: "",
                    user = item.user ?: "",
                    cpu = item.cpu ?: 0.0,
                    memory = item.memoryPercent ?: 0.0,
                    status = item.state ?: ""
                )
            } ?: emptyList()

            _uiState.update { it.copy(processes = processList) }
        }.onFailure { error ->
            Log.e(TAG, "Load process error", error)
        }

        // 获取服务列表
        systemRepository.getProcessGroup().onSuccess { response ->
            val serviceList = response.data?.services?.map { item ->
                ServiceInfo(
                    name = item.name ?: "",
                    displayName = item.displayName ?: "",
                    status = item.status ?: "",
                    enabled = item.enabled ?: false
                )
            } ?: emptyList()

            _uiState.update { it.copy(services = serviceList) }
        }.onFailure { error ->
            Log.e(TAG, "Load service error", error)
        }

        // 启动自动刷新
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (isActive) {
                delay(_uiState.value.refreshDuration * 1000L)
                loadDataOnce()
            }
        }
    }

    private suspend fun loadDataOnce() {
        _uiState.update { it.copy(isLoading = false, error = null) }
        systemRepository.getProcessList().onSuccess { response ->
            val processList = response.process?.map { item ->
                ProcessInfo(
                    pid = item.pid ?: 0,
                    name = item.name ?: "",
                    user = item.user ?: "",
                    cpu = item.cpu ?: 0.0,
                    memory = item.memoryPercent ?: 0.0,
                    status = item.state ?: ""
                )
            } ?: emptyList()
            _uiState.update { it.copy(processes = processList) }
        }.onFailure { error ->
            Log.e(TAG, "Load process error", error)
        }
        systemRepository.getProcessGroup().onSuccess { response ->
            val serviceList = response.data?.services?.map { item ->
                ServiceInfo(
                    name = item.name ?: "",
                    displayName = item.displayName ?: "",
                    status = item.status ?: "",
                    enabled = item.enabled ?: false
                )
            } ?: emptyList()
            _uiState.update { it.copy(services = serviceList) }
        }.onFailure { error ->
            Log.e(TAG, "Load service error", error)
        }
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
