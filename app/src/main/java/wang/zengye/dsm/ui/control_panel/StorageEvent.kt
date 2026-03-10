package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseEvent

sealed class StorageEvent : BaseEvent {
    data class Error(val message: String) : StorageEvent()
}
