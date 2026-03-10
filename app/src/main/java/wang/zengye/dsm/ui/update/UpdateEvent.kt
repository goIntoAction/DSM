package wang.zengye.dsm.ui.update

import wang.zengye.dsm.ui.base.BaseEvent

sealed class UpdateEvent : BaseEvent {
    data class Error(val message: String) : UpdateEvent()
}
