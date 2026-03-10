package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * 系统信息 Event
 */
sealed class SystemInfoEvent : BaseEvent {
    data class ShowError(val message: String) : SystemInfoEvent()
}
