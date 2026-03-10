package wang.zengye.dsm.ui.photos

import wang.zengye.dsm.ui.base.BaseEvent

sealed class PhotoPreviewEvent : BaseEvent {
    data class ShowError(val message: String) : PhotoPreviewEvent()
}
