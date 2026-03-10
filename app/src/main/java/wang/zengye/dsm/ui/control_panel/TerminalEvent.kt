package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * 终端设置 Event
 */
sealed class TerminalEvent : BaseEvent {
    data class ShowError(val message: String) : TerminalEvent()
    data object UpdateSuccess : TerminalEvent()
    data object DisconnectSuccess : TerminalEvent()
}
