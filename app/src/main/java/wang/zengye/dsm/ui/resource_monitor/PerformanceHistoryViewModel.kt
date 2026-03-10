package wang.zengye.dsm.ui.resource_monitor

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wang.zengye.dsm.R
import wang.zengye.dsm.data.repository.HistoryTimeRange
import wang.zengye.dsm.data.repository.PerformanceHistoryRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import wang.zengye.dsm.util.appString
import javax.inject.Inject
import kotlin.math.max
import kotlin.random.Random

/**
 * 性能数据点
 */
data class PerformanceDataPoint(
    val timestamp: Long,
    val value: Float
)

/**
 * 性能数据
 */
data class PerformanceMetric(
    val label: String,
    val unit: String,
    val currentValue: Float,
    val maxValue: Float,
    val dataPoints: List<PerformanceDataPoint>,
    val color: Color
)

/**
 * 历史性能UI状态
 */
data class PerformanceHistoryUiState(
    override val isLoading: Boolean = false,
    val timeRange: TimeRange = TimeRange.HOUR_1,
    val cpuMetric: PerformanceMetric = PerformanceMetric(
        label = appString(R.string.resource_monitor_cpu_usage_label),
        unit = "%",
        currentValue = 0f,
        maxValue = 100f,
        dataPoints = emptyList(),
        color = Color(0xFF2196F3)
    ),
    val memoryMetric: PerformanceMetric = PerformanceMetric(
        label = appString(R.string.resource_monitor_memory_usage_label),
        unit = "%",
        currentValue = 0f,
        maxValue = 100f,
        dataPoints = emptyList(),
        color = Color(0xFF4CAF50)
    ),
    val networkInMetric: PerformanceMetric = PerformanceMetric(
        label = appString(R.string.resource_monitor_network_receive_label),
        unit = "MB/s",
        currentValue = 0f,
        maxValue = 100f,
        dataPoints = emptyList(),
        color = Color(0xFFFF9800)
    ),
    val networkOutMetric: PerformanceMetric = PerformanceMetric(
        label = appString(R.string.resource_monitor_network_send_label),
        unit = "MB/s",
        currentValue = 0f,
        maxValue = 100f,
        dataPoints = emptyList(),
        color = Color(0xFF9C27B0)
    ),
    val diskReadMetric: PerformanceMetric = PerformanceMetric(
        label = appString(R.string.resource_monitor_disk_read_label),
        unit = "MB/s",
        currentValue = 0f,
        maxValue = 100f,
        dataPoints = emptyList(),
        color = Color(0xFF00BCD4)
    ),
    val diskWriteMetric: PerformanceMetric = PerformanceMetric(
        label = appString(R.string.resource_monitor_disk_write_label),
        unit = "MB/s",
        currentValue = 0f,
        maxValue = 100f,
        dataPoints = emptyList(),
        color = Color(0xFFE91E63)
    ),
    override val error: String? = null
) : wang.zengye.dsm.ui.base.BaseState

enum class TimeRange(val label: String, val minutes: Int) {
    HOUR_1(appString(R.string.resource_monitor_time_1h), 60),
    HOUR_6(appString(R.string.resource_monitor_time_6h), 360),
    HOUR_24(appString(R.string.resource_monitor_time_24h), 1440),
    DAY_7(appString(R.string.resource_monitor_time_7d), 10080)
}

// 将 HistoryTimeRange 转换为 TimeRange 的辅助函数
internal fun HistoryTimeRange.toTimeRange(): TimeRange = when (this) {
    HistoryTimeRange.HOUR_1 -> TimeRange.HOUR_1
    HistoryTimeRange.HOUR_6 -> TimeRange.HOUR_6
    HistoryTimeRange.HOUR_24 -> TimeRange.HOUR_24
    HistoryTimeRange.DAY_7 -> TimeRange.DAY_7
}

internal fun TimeRange.toHistoryTimeRange(): HistoryTimeRange = when (this) {
    TimeRange.HOUR_1 -> HistoryTimeRange.HOUR_1
    TimeRange.HOUR_6 -> HistoryTimeRange.HOUR_6
    TimeRange.HOUR_24 -> HistoryTimeRange.HOUR_24
    TimeRange.DAY_7 -> HistoryTimeRange.DAY_7
}

/**
 * 历史性能ViewModel
 */
@HiltViewModel
class PerformanceHistoryViewModel @Inject constructor(
    private val performanceHistoryRepository: PerformanceHistoryRepository
) : BaseViewModel<PerformanceHistoryUiState, PerformanceHistoryIntent, PerformanceHistoryEvent>() {

    private val _state = MutableStateFlow(PerformanceHistoryUiState())
    override val state: StateFlow<PerformanceHistoryUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PerformanceHistoryEvent>(extraBufferCapacity = 10)
    override val events = _events.asSharedFlow()

    private var dataCollectionJob: kotlinx.coroutines.Job? = null

    init {
        sendIntent(PerformanceHistoryIntent.LoadHistoricalData)
    }

    override suspend fun processIntent(intent: PerformanceHistoryIntent) {
        when (intent) {
            is PerformanceHistoryIntent.LoadHistoricalData -> loadHistoricalData()
            is PerformanceHistoryIntent.SetTimeRange -> setTimeRange(intent.timeRange)
        }
    }

    /**
     * 加载历史数据
     */
    private suspend fun loadHistoricalData() {
        _state.update { it.copy(isLoading = true) }

        // 从仓库获取真实数据
        val historyTimeRange = _state.value.timeRange.toHistoryTimeRange()
        val realData = performanceHistoryRepository.getDataInRange(historyTimeRange)

        if (realData.isNotEmpty()) {
            // 使用真实数据
            val cpuPoints = realData.map { PerformanceDataPoint(it.timestamp, it.cpuUsage) }
            val memoryPoints = realData.map { PerformanceDataPoint(it.timestamp, it.memoryUsage) }
            val netInPoints = realData.map { PerformanceDataPoint(it.timestamp, it.networkRx / 1024f / 1024f) } // bytes to MB/s
            val netOutPoints = realData.map { PerformanceDataPoint(it.timestamp, it.networkTx / 1024f / 1024f) }
            val diskReadPoints = realData.map { PerformanceDataPoint(it.timestamp, it.diskRead / 1024f / 1024f) }
            val diskWritePoints = realData.map { PerformanceDataPoint(it.timestamp, it.diskWrite / 1024f / 1024f) }

            // 计算最大值用于图表缩放
            val maxNetIn = (netInPoints.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(1f)
            val maxNetOut = (netOutPoints.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(1f)
            val maxDiskRead = (diskReadPoints.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(1f)
            val maxDiskWrite = (diskWritePoints.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(1f)

            _state.update { state ->
                state.copy(
                    isLoading = false,
                    cpuMetric = state.cpuMetric.copy(
                        currentValue = cpuPoints.lastOrNull()?.value ?: 0f,
                        dataPoints = cpuPoints
                    ),
                    memoryMetric = state.memoryMetric.copy(
                        currentValue = memoryPoints.lastOrNull()?.value ?: 0f,
                        dataPoints = memoryPoints
                    ),
                    networkInMetric = state.networkInMetric.copy(
                        currentValue = netInPoints.lastOrNull()?.value ?: 0f,
                        maxValue = maxNetIn * 1.2f,
                        dataPoints = netInPoints
                    ),
                    networkOutMetric = state.networkOutMetric.copy(
                        currentValue = netOutPoints.lastOrNull()?.value ?: 0f,
                        maxValue = maxNetOut * 1.2f,
                        dataPoints = netOutPoints
                    ),
                    diskReadMetric = state.diskReadMetric.copy(
                        currentValue = diskReadPoints.lastOrNull()?.value ?: 0f,
                        maxValue = maxDiskRead * 1.2f,
                        dataPoints = diskReadPoints
                    ),
                    diskWriteMetric = state.diskWriteMetric.copy(
                        currentValue = diskWritePoints.lastOrNull()?.value ?: 0f,
                        maxValue = maxDiskWrite * 1.2f,
                        dataPoints = diskWritePoints
                    )
                )
            }
        } else {
            // 如果没有真实数据，使用模拟数据
            delay(500)

            val now = System.currentTimeMillis()
            val dataPoints = generateMockDataPoints(now, _state.value.timeRange.minutes)

            _state.update { state ->
                state.copy(
                    isLoading = false,
                    cpuMetric = state.cpuMetric.copy(
                        currentValue = dataPoints.cpu.lastOrNull()?.value ?: 0f,
                        dataPoints = dataPoints.cpu
                    ),
                    memoryMetric = state.memoryMetric.copy(
                        currentValue = dataPoints.memory.lastOrNull()?.value ?: 0f,
                        dataPoints = dataPoints.memory
                    ),
                    networkInMetric = state.networkInMetric.copy(
                        currentValue = dataPoints.networkIn.lastOrNull()?.value ?: 0f,
                        dataPoints = dataPoints.networkIn
                    ),
                    networkOutMetric = state.networkOutMetric.copy(
                        currentValue = dataPoints.networkOut.lastOrNull()?.value ?: 0f,
                        dataPoints = dataPoints.networkOut
                    ),
                    diskReadMetric = state.diskReadMetric.copy(
                        currentValue = dataPoints.diskRead.lastOrNull()?.value ?: 0f,
                        dataPoints = dataPoints.diskRead
                    ),
                    diskWriteMetric = state.diskWriteMetric.copy(
                        currentValue = dataPoints.diskWrite.lastOrNull()?.value ?: 0f,
                        dataPoints = dataPoints.diskWrite
                    )
                )
            }
        }
    }

    /**
     * 设置时间范围
     */
    private suspend fun setTimeRange(timeRange: TimeRange) {
        _state.update { it.copy(timeRange = timeRange) }
        loadHistoricalData()
    }

    /**
     * 生成模拟数据点
     */
    private fun generateMockDataPoints(now: Long, minutes: Int): MockData {
        val interval = when (minutes) {
            60 -> 60 * 1000L // 1分钟间隔
            360 -> 5 * 60 * 1000L // 5分钟间隔
            1440 -> 15 * 60 * 1000L // 15分钟间隔
            else -> 60 * 60 * 1000L // 1小时间隔
        }

        val count = (minutes * 60 * 1000L / interval).toInt()
        var lastCpu = 30f
        var lastMemory = 50f
        var lastNetIn = 10f
        var lastNetOut = 5f
        var lastDiskRead = 20f
        var lastDiskWrite = 10f

        val cpuPoints = mutableListOf<PerformanceDataPoint>()
        val memoryPoints = mutableListOf<PerformanceDataPoint>()
        val netInPoints = mutableListOf<PerformanceDataPoint>()
        val netOutPoints = mutableListOf<PerformanceDataPoint>()
        val diskReadPoints = mutableListOf<PerformanceDataPoint>()
        val diskWritePoints = mutableListOf<PerformanceDataPoint>()

        for (i in count downTo 1) {
            val timestamp = now - i * interval

            // 生成平滑的随机数据
            lastCpu = (lastCpu + Random.nextFloat() * 20 - 10).coerceIn(5f, 95f)
            lastMemory = (lastMemory + Random.nextFloat() * 10 - 5).coerceIn(30f, 90f)
            lastNetIn = max(0f, lastNetIn + Random.nextFloat() * 30 - 15)
            lastNetOut = max(0f, lastNetOut + Random.nextFloat() * 20 - 10)
            lastDiskRead = max(0f, lastDiskRead + Random.nextFloat() * 40 - 20)
            lastDiskWrite = max(0f, lastDiskWrite + Random.nextFloat() * 30 - 15)

            cpuPoints.add(PerformanceDataPoint(timestamp, lastCpu))
            memoryPoints.add(PerformanceDataPoint(timestamp, lastMemory))
            netInPoints.add(PerformanceDataPoint(timestamp, lastNetIn))
            netOutPoints.add(PerformanceDataPoint(timestamp, lastNetOut))
            diskReadPoints.add(PerformanceDataPoint(timestamp, lastDiskRead))
            diskWritePoints.add(PerformanceDataPoint(timestamp, lastDiskWrite))
        }

        return MockData(
            cpu = cpuPoints,
            memory = memoryPoints,
            networkIn = netInPoints,
            networkOut = netOutPoints,
            diskRead = diskReadPoints,
            diskWrite = diskWritePoints
        )
    }

    private data class MockData(
        val cpu: List<PerformanceDataPoint>,
        val memory: List<PerformanceDataPoint>,
        val networkIn: List<PerformanceDataPoint>,
        val networkOut: List<PerformanceDataPoint>,
        val diskRead: List<PerformanceDataPoint>,
        val diskWrite: List<PerformanceDataPoint>
    )
}
