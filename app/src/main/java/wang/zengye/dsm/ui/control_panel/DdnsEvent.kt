package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * DDNS Event
 */
sealed class DdnsEvent : BaseEvent {
    data class ShowError(val message: String) : DdnsEvent()
    data object AddSuccess : DdnsEvent()
    data object DeleteSuccess : DdnsEvent()
    data object UpdateSuccess : DdnsEvent()
}
