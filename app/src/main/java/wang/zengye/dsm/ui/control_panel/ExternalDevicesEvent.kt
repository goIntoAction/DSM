package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * 外部设备 Event
 */
sealed class ExternalDevicesEvent : BaseEvent {
    data class ShowError(val message: String) : ExternalDevicesEvent()
    data object EjectSuccess : ExternalDevicesEvent()
}
