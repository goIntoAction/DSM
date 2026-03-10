package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * 安全扫描 Event
 */
sealed class SecurityScanEvent : BaseEvent {
    data class ShowError(val message: String) : SecurityScanEvent()
}
