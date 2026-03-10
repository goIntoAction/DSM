package wang.zengye.dsm.ui.resource_monitor

import wang.zengye.dsm.ui.base.BaseIntent

sealed class PerformanceIntent : BaseIntent {
    data object StartAutoRefresh : PerformanceIntent()
    data object StopAutoRefresh : PerformanceIntent()
    data object ToggleAutoRefresh : PerformanceIntent()
    data class SetRefreshInterval(val seconds: Int) : PerformanceIntent()
    data object RefreshOnce : PerformanceIntent()
    data object LoadData : PerformanceIntent()
}
