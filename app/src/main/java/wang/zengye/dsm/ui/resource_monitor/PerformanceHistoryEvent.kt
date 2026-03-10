package wang.zengye.dsm.ui.resource_monitor

import wang.zengye.dsm.ui.base.BaseEvent

sealed class PerformanceHistoryEvent : BaseEvent {
    data class ShowError(val message: String) : PerformanceHistoryEvent()
}
