package wang.zengye.dsm.ui.performance

import wang.zengye.dsm.ui.base.BaseIntent

sealed class PerformanceIntent : BaseIntent {
    data object LoadData : PerformanceIntent()
    data object Refresh : PerformanceIntent()
}
