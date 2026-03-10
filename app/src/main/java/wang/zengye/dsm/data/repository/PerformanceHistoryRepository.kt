package wang.zengye.dsm.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import wang.zengye.dsm.R
import wang.zengye.dsm.util.appString
import java.util.concurrent.TimeUnit

/**
 * 性能历史数据点
 */
data class PerformanceHistoryPoint(
    val timestamp: Long = System.currentTimeMillis(),
    val cpuUsage: Float = 0f,
    val memoryUsage: Float = 0f,
    val networkRx: Long = 0L,  // bytes/s
    val networkTx: Long = 0L,
    val diskRead: Long = 0L,
    val diskWrite: Long = 0L
)

/**
 * 时间范围
 */
enum class HistoryTimeRange(val label: String, val durationMs: Long) {
    HOUR_1(appString(R.string.perf_history_1_hour), TimeUnit.HOURS.toMillis(1)),
    HOUR_6(appString(R.string.perf_history_6_hours), TimeUnit.HOURS.toMillis(6)),
    HOUR_24(appString(R.string.perf_history_24_hours), TimeUnit.HOURS.toMillis(24)),
    DAY_7(appString(R.string.perf_history_7_days), TimeUnit.DAYS.toMillis(7))
}

/**
 * 性能历史数据仓库（单例）
 * 用于在性能监控和历史图表之间共享数据
 */
object PerformanceHistoryRepository {
    
    private const val MAX_DATA_POINTS = 1440 // 最多保存1440个数据点（24小时，每分钟1个）
    
    private val _historyData = MutableStateFlow<List<PerformanceHistoryPoint>>(emptyList())
    val historyData: StateFlow<List<PerformanceHistoryPoint>> = _historyData.asStateFlow()
    
    /**
     * 添加数据点
     */
    fun addDataPoint(point: PerformanceHistoryPoint) {
        val currentList = _historyData.value.toMutableList()
        
        // 添加新数据点
        currentList.add(point)
        
        // 移除过期数据
        val cutoffTime = point.timestamp - TimeUnit.DAYS.toMillis(1)
        while (currentList.isNotEmpty() && currentList.first().timestamp < cutoffTime) {
            currentList.removeAt(0)
        }
        
        // 限制数据点数量
        while (currentList.size > MAX_DATA_POINTS) {
            currentList.removeAt(0)
        }
        
        _historyData.value = currentList
    }
    
    /**
     * 获取指定时间范围内的数据
     */
    fun getDataInRange(timeRange: HistoryTimeRange): List<PerformanceHistoryPoint> {
        val now = System.currentTimeMillis()
        val cutoff = now - timeRange.durationMs
        return _historyData.value.filter { it.timestamp >= cutoff }
    }
    
    /**
     * 获取最新数据点
     */
    fun getLatestPoint(): PerformanceHistoryPoint? {
        return _historyData.value.lastOrNull()
    }
    
    /**
     * 清除所有数据
     */
    fun clearAll() {
        _historyData.value = emptyList()
    }
}
