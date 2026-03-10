package wang.zengye.dsm.ui.download

import wang.zengye.dsm.ui.base.BaseEvent

sealed class PeerListEvent : BaseEvent {
    data class Error(val message: String) : PeerListEvent()
}
