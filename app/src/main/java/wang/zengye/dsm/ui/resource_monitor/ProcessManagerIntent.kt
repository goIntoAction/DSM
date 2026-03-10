package wang.zengye.dsm.ui.resource_monitor

import wang.zengye.dsm.ui.base.BaseIntent

sealed class ProcessManagerIntent : BaseIntent {
    data object LoadProcesses : ProcessManagerIntent()
    data object Refresh : ProcessManagerIntent()
    data class SetSortBy(val sortBy: String) : ProcessManagerIntent()
    data class SetSearchQuery(val query: String) : ProcessManagerIntent()
    data object ToggleAutoRefresh : ProcessManagerIntent()
}
