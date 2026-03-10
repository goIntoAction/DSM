package wang.zengye.dsm.ui.photos

import wang.zengye.dsm.ui.base.BaseEvent

sealed class AlbumDetailEvent : BaseEvent {
    data class ShowError(val message: String) : AlbumDetailEvent()
    data object DeleteSuccess : AlbumDetailEvent()
}
