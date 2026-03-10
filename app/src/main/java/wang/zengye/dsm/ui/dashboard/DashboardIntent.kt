package wang.zengye.dsm.ui.dashboard

import wang.zengye.dsm.ui.base.BaseIntent

sealed class DashboardIntent : BaseIntent {
    data object LoadData : DashboardIntent()
    data object Refresh : DashboardIntent()
    data object Shutdown : DashboardIntent()
    data object Reboot : DashboardIntent()
}
