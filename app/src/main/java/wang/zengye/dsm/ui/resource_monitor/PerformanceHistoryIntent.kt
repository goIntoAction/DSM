package wang.zengye.dsm.ui.resource_monitor

import wang.zengye.dsm.ui.base.BaseIntent

sealed class PerformanceHistoryIntent : BaseIntent {
    data object LoadHistoricalData : PerformanceHistoryIntent()
    data class SetTimeRange(val timeRange: TimeRange) : PerformanceHistoryIntent()
}
