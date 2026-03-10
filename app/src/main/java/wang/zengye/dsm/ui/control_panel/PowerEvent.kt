package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * 电源管理 Event
 */
sealed class PowerEvent : BaseEvent {
    data class ShowError(val message: String) : PowerEvent()
    data object ShutdownSuccess : PowerEvent()
    data object RebootSuccess : PowerEvent()
}
