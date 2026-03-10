package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * 网络 Event
 */
sealed class NetworkEvent : BaseEvent {
    data class ShowError(val message: String) : NetworkEvent()
}
