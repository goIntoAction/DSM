package wang.zengye.dsm.ui.logcenter

import wang.zengye.dsm.ui.base.BaseIntent

sealed class LogCenterIntent : BaseIntent {
    data object LoadRecentLogs : LogCenterIntent()
    data class LoadLogs(val logTypeIndex: Int = 0) : LogCenterIntent()
    data object Refresh : LogCenterIntent()
    data class SetLevelFilter(val level: String?) : LogCenterIntent()
    data class SetSearchQuery(val query: String) : LogCenterIntent()
    data object LoadHistories : LogCenterIntent()
}
