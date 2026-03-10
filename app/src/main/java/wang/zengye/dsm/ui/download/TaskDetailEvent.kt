package wang.zengye.dsm.ui.download

import wang.zengye.dsm.ui.base.BaseEvent

sealed class TaskDetailEvent : BaseEvent {
    data class Error(val message: String) : TaskDetailEvent()
    data object DeleteSuccess : TaskDetailEvent()
}
