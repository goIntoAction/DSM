package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * 防火墙 Event
 */
sealed class FirewallEvent : BaseEvent {
    data class ShowError(val message: String) : FirewallEvent()
    data object DeleteSuccess : FirewallEvent()
}
