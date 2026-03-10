package wang.zengye.dsm.ui.taskmanager

import wang.zengye.dsm.ui.base.BaseIntent

sealed class TaskManagerIntent : BaseIntent {
    data object LoadData : TaskManagerIntent()
    data object Refresh : TaskManagerIntent()
}
