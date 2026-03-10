package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseIntent

sealed class NotificationsIntent : BaseIntent {
    data object LoadNotifications : NotificationsIntent()
    data class MarkAsRead(val id: Long) : NotificationsIntent()
    data object ClearAll : NotificationsIntent()
}
