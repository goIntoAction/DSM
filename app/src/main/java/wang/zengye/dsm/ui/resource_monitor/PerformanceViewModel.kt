package wang.zengye.dsm.ui.resource_monitor

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
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

data class PerformanceData(
    val cpuUsage: Int = 0,
    val memoryUsage: Int = 0,
    val memoryUsed: Long = 0,
    val memoryTotal: Long = 0,
    val networkRx: Long = 0,
    val networkTx: Long = 0,
    val diskRead: Long = 0,
    val diskWrite: Long = 0
)

data class PerformanceUiState(
    override val isLoading: Boolean = false,
    val current: PerformanceData = PerformanceData(),
    val cpuHistory: List<Int> = emptyList(),
    val memoryHistory: List<Int> = emptyList(),
    val networkRxHistory: List<Long> = emptyList(),
    val networkTxHistory: List<Long> = emptyList(),
    val autoRefresh: Boolean = true,
    val refreshInterval: Int = 3,
    override val error: String? = null
) : wang.zengye.dsm.ui.base.BaseState

@HiltViewModel
class PerformanceViewModel @Inject constructor(
    private val systemRepository: SystemRepository
) : BaseViewModel<PerformanceUiState, PerformanceIntent, PerformanceEvent>(), DefaultLifecycleObserver {

    companion object {
        private const val TAG = "PerformanceViewModel"
        private const val MAX_HISTORY = 60
    }

    private val _uiState = MutableStateFlow(PerformanceUiState())
    override val state: StateFlow<PerformanceUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PerformanceEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    private var refreshJob: Job? = null

    init {
        sendIntent(PerformanceIntent.LoadData)
    }

    override suspend fun processIntent(intent: PerformanceIntent) {
        when (intent) {
            is PerformanceIntent.StartAutoRefresh -> startAutoRefresh()
            is PerformanceIntent.StopAutoRefresh -> stopAutoRefresh()
            is PerformanceIntent.ToggleAutoRefresh -> toggleAutoRefresh()
            is PerformanceIntent.SetRefreshInterval -> setRefreshInterval(intent.seconds)
            is PerformanceIntent.RefreshOnce -> refreshOnce()
            is PerformanceIntent.LoadData -> loadData()
        }
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (isActive) {
                fetchUtilization()
                delay(_uiState.value.refreshInterval * 1000L)
            }
        }
        _uiState.update { it.copy(autoRefresh = true) }
    }

    private fun stopAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = null
        _uiState.update { it.copy(autoRefresh = false) }
    }

    private fun toggleAutoRefresh() {
        if (_uiState.value.autoRefresh) {
            stopAutoRefresh()
        } else {
            startAutoRefresh()
        }
    }

    private fun setRefreshInterval(seconds: Int) {
        _uiState.update { it.copy(refreshInterval = seconds) }
        if (_uiState.value.autoRefresh) {
            startAutoRefresh()
        }
    }

    private suspend fun refreshOnce() {
        fetchUtilization()
    }

    private suspend fun loadData() {
        _uiState.update { it.copy(isLoading = true) }
        fetchUtilization()
    }

    private suspend fun fetchUtilization() {
                    systemRepository.getUtilization().onSuccess { response ->
            val data = response.data
            val cpuUsage = (data?.cpu?.userLoad ?: 0) + (data?.cpu?.systemLoad ?: 0)
            val memoryUsage = data?.memory?.realUsage ?: 0
            val memorySizeKb = data?.memory?.memorySize ?: 0L
            val memoryTotal = memorySizeKb * 1024
            val memoryUsed = memoryUsage * memorySizeKb * 1024 / 100

            var networkRx = 0L
            var networkTx = 0L
            data?.network?.forEach { net ->
                networkRx += net.rx ?: 0
                networkTx += net.tx ?: 0
            }

            var diskRead = 0L
            var diskWrite = 0L
            data?.disk?.total?.let { diskTotal ->
                diskRead = diskTotal.readByte ?: 0L
                diskWrite = diskTotal.writeByte ?: 0L
            }

            val current = PerformanceData(
                cpuUsage = cpuUsage,
                memoryUsage = memoryUsage,
                memoryUsed = memoryUsed,
                memoryTotal = memoryTotal,
                networkRx = networkRx,
                networkTx = networkTx,
                diskRead = diskRead,
                diskWrite = diskWrite
            )

            _uiState.update { state ->
                state.copy(
                    current = current,
                    cpuHistory = (state.cpuHistory + current.cpuUsage).takeLast(MAX_HISTORY),
                    memoryHistory = (state.memoryHistory + current.memoryUsage).takeLast(MAX_HISTORY),
                    networkRxHistory = (state.networkRxHistory + current.networkRx).takeLast(MAX_HISTORY),
                    networkTxHistory = (state.networkTxHistory + current.networkTx).takeLast(MAX_HISTORY),
                    isLoading = false,
                    error = null
                )
            }
        }.onFailure { error ->
            Log.e(TAG, "Utilization API failed", error)
            _uiState.update { it.copy(error = error.message, isLoading = false) }
            _events.emit(PerformanceEvent.Error(error.message ?: "Failed to load data"))
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
        if (_uiState.value.refreshInterval > 0) {
            startAutoRefresh()
            Log.d(TAG, "onStart: 自动刷新已恢复")
        }
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }
}
