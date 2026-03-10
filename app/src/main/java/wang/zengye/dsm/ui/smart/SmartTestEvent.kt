package wang.zengye.dsm.ui.smart

import wang.zengye.dsm.ui.base.BaseEvent

sealed class SmartTestEvent : BaseEvent {
    data class Error(val message: String) : SmartTestEvent()
    data object TestStarted : SmartTestEvent()
}
