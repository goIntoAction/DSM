package wang.zengye.dsm.ui.file

import wang.zengye.dsm.ui.base.BaseEvent

sealed class FavoriteEvent : BaseEvent {
    data class ShowError(val message: String) : FavoriteEvent()
    data object RenameSuccess : FavoriteEvent()
    data object DeleteSuccess : FavoriteEvent()
}
