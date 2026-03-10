package wang.zengye.dsm.ui.taskmanager

import wang.zengye.dsm.ui.base.BaseEvent

sealed class TaskManagerEvent : BaseEvent {
    data class Error(val message: String) : TaskManagerEvent()
}
