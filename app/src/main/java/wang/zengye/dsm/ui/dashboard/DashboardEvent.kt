package wang.zengye.dsm.ui.dashboard

import wang.zengye.dsm.ui.base.BaseEvent

sealed class DashboardEvent : BaseEvent {
    data class Error(val message: String) : DashboardEvent()
    data object ShutdownSuccess : DashboardEvent()
    data object RebootSuccess : DashboardEvent()
}
