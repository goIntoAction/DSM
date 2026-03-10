package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * 任务计划 Event
 */
sealed class TaskSchedulerEvent : BaseEvent {
    data class ShowError(val message: String) : TaskSchedulerEvent()
    data object RunSuccess : TaskSchedulerEvent()
    data object DeleteSuccess : TaskSchedulerEvent()
}
