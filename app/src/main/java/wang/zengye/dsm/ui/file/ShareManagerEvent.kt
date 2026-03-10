package wang.zengye.dsm.ui.file

import wang.zengye.dsm.ui.base.BaseEvent

sealed class ShareManagerEvent : BaseEvent {
    data class ShowError(val message: String) : ShareManagerEvent()
    data object DeleteSuccess : ShareManagerEvent()
}
