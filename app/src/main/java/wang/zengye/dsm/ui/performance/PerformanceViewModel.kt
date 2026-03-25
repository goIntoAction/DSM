package wang.zengye.dsm.ui.performance

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import wang.zengye.dsm.data.repository.PerformanceHistoryPoint
import wang.zengye.dsm.data.repository.PerformanceHistoryRepository
import wang.zengye.dsm.data.repository.SystemRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import wang.zengye.dsm.util.SettingsManager
import javax.inject.Inject

/**
 * 性能监控数据
 */
data class PerformanceData(
    val cpuUsage: Int = 0,
    val memoryUsage: Int = 0,
    val memoryTotal: Long = 0,
    val memoryUsed: Long = 0,
    val networkRx: Long = 0,
    val networkTx: Long = 0,
    val diskRead: Long = 0,
    val diskWrite: Long = 0,
    val volumeRead: Long = 0,
    val volumeWrite: Long = 0
)

data class PerformanceUiState(
    override val isLoading: Boolean = true,
    override val error: String? = null,
    val cpuHistory: List<Int> = emptyList(),
    val memoryHistory: List<Int> = emptyList(),
    val networkRxHistory: List<Long> = emptyList(),
    val networkTxHistory: List<Long> = emptyList(),
    val diskReadHistory: List<Long> = emptyList(),
    val diskWriteHistory: List<Long> = emptyList(),
    val volumeReadHistory: List<Long> = emptyList(),
    val volumeWriteHistory: List<Long> = emptyList(),
    val currentData: PerformanceData = PerformanceData(),
    val refreshDuration: Int = 10
) : wang.zengye.dsm.ui.base.BaseState {
    companion object {
        const val MAX_HISTORY_SIZE = 60 // 保留60个数据点（约10分钟）
    }

    fun addDataPoint(data: PerformanceData): PerformanceUiState {
        return copy(
            cpuHistory = (cpuHistory + data.cpuUsage).takeLast(MAX_HISTORY_SIZE),
            memoryHistory = (memoryHistory + data.memoryUsage).takeLast(MAX_HISTORY_SIZE),
            networkRxHistory = (networkRxHistory + data.networkRx).takeLast(MAX_HISTORY_SIZE),
            networkTxHistory = (networkTxHistory + data.networkTx).takeLast(MAX_HISTORY_SIZE),
            diskReadHistory = (diskReadHistory + data.diskRead).takeLast(MAX_HISTORY_SIZE),
            diskWriteHistory = (diskWriteHistory + data.diskWrite).takeLast(MAX_HISTORY_SIZE),
            volumeReadHistory = (volumeReadHistory + data.volumeRead).takeLast(MAX_HISTORY_SIZE),
            volumeWriteHistory = (volumeWriteHistory + data.volumeWrite).takeLast(MAX_HISTORY_SIZE),
            currentData = data
        )
    }
}

@HiltViewModel
class PerformanceViewModel @Inject constructor(
    private val systemRepository: SystemRepository,
    private val performanceHistoryRepository: PerformanceHistoryRepository
) : BaseViewModel<PerformanceUiState, PerformanceIntent, PerformanceEvent>(), DefaultLifecycleObserver {

    companion object {
        private const val TAG = "PerformanceViewModel"
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
        viewModelScope.launch {
            val duration = SettingsManager.refreshDuration.first()
            _uiState.update { it.copy(refreshDuration = duration) }
        }
        sendIntent(PerformanceIntent.LoadData)
    }

    override suspend fun processIntent(intent: PerformanceIntent) {
        when (intent) {
            is PerformanceIntent.LoadData -> loadData()
            is PerformanceIntent.Refresh -> loadData()
        }
    }

    private suspend fun loadData() {
        _uiState.update { it.copy(isLoading = false, error = null) }

        systemRepository.getUtilization().onSuccess { response ->
            try {
                val data = response.data
                // CPU - 包含user_load + system_load + other_load (IO等待)
                val userLoad = data?.cpu?.userLoad ?: 0
                val systemLoad = data?.cpu?.systemLoad ?: 0
                val otherLoad = data?.cpu?.otherLoad ?: 0
                val cpuUsage = userLoad + systemLoad + otherLoad

                // 内存
                val memoryUsage = data?.memory?.realUsage ?: 0
                val memorySizeKb = data?.memory?.memorySize ?: 0L
                val memoryTotal = memorySizeKb * 1024
                val memoryUsed = memoryUsage * memorySizeKb * 1024 / 100

                // 网络 - 遍历所有网卡累加数据
                var networkRx = 0L
                var networkTx = 0L
                data?.network?.forEach { iface ->
                    networkRx += iface.rx ?: 0
                    networkTx += iface.tx ?: 0
                }

                // 磁盘IO - 使用total对象获取汇总数据
                val diskRead = data?.disk?.total?.readByte ?: 0L
                val diskWrite = data?.disk?.total?.writeByte ?: 0L

                // 存储卷IO - 使用total对象获取汇总数据
                val volumeRead = data?.space?.total?.readByte ?: 0L
                val volumeWrite = data?.space?.total?.writeByte ?: 0L

                val newData = PerformanceData(
                    cpuUsage = cpuUsage,
                    memoryUsage = memoryUsage,
                    memoryTotal = memoryTotal,
                    memoryUsed = memoryUsed,
                    networkRx = networkRx,
                    networkTx = networkTx,
                    diskRead = diskRead,
                    diskWrite = diskWrite,
                    volumeRead = volumeRead,
                    volumeWrite = volumeWrite
                )

                _uiState.update { it.addDataPoint(newData) }

                // 同步保存到历史数据仓库
                performanceHistoryRepository.addDataPoint(
                    PerformanceHistoryPoint(
                        timestamp = System.currentTimeMillis(),
                        cpuUsage = cpuUsage.toFloat(),
                        memoryUsage = memoryUsage.toFloat(),
                        networkRx = networkRx,
                        networkTx = networkTx,
                        diskRead = diskRead,
                        diskWrite = diskWrite
                    )
                )

                // 启动定时刷新
                startAutoRefresh()

            } catch (e: Exception) {
                Log.e(TAG, "Parse error", e)
                _uiState.update { it.copy(error = e.message) }
                _events.emit(PerformanceEvent.Error(e.message ?: "Parse error"))
            }
        }.onFailure { error ->
            Log.e(TAG, "API error", error)
            _uiState.update { it.copy(error = error.message) }
            _events.emit(PerformanceEvent.Error(error.message ?: "API error"))
        }
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        val duration = _uiState.value.refreshDuration
        if (duration > 0) {
            refreshJob = viewModelScope.launch {
                delay(duration * 1000L)
                loadData()
            }
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
        if (_uiState.value.refreshDuration > 0) {
            startAutoRefresh()
            Log.d(TAG, "onStart: 自动刷新已恢复")
        }
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }
}
