package wang.zengye.dsm.ui.resource_monitor

import wang.zengye.dsm.ui.base.BaseEvent

sealed class PerformanceEvent : BaseEvent {
    data class Error(val message: String) : PerformanceEvent()
}
