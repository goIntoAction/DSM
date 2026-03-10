package wang.zengye.dsm.ui.download

import wang.zengye.dsm.ui.base.BaseEvent

sealed class TrackerManagerEvent : BaseEvent {
    data class Error(val message: String) : TrackerManagerEvent()
}
