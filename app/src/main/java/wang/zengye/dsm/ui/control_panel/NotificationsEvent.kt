package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseEvent

sealed class NotificationsEvent : BaseEvent {
    data class Error(val message: String) : NotificationsEvent()
}
