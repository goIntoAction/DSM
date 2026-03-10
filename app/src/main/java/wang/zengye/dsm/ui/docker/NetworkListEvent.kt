package wang.zengye.dsm.ui.docker

import wang.zengye.dsm.ui.base.BaseEvent

sealed class NetworkListEvent : BaseEvent {
    data class ShowError(val message: String) : NetworkListEvent()
    data object CreateSuccess : NetworkListEvent()
    data object DeleteSuccess : NetworkListEvent()
}
