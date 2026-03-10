package wang.zengye.dsm.ui.logcenter

import wang.zengye.dsm.ui.base.BaseEvent

sealed class LogCenterEvent : BaseEvent {
    data class Error(val message: String) : LogCenterEvent()
}
