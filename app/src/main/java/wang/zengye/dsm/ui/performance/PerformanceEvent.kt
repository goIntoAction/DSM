package wang.zengye.dsm.ui.performance

import wang.zengye.dsm.ui.base.BaseEvent

sealed class PerformanceEvent : BaseEvent {
    data class Error(val message: String) : PerformanceEvent()
}
