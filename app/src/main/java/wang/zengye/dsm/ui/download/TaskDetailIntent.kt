package wang.zengye.dsm.ui.download

import wang.zengye.dsm.ui.base.BaseIntent

sealed class TaskDetailIntent : BaseIntent {
    data class LoadTaskDetail(val taskId: String) : TaskDetailIntent()
    data class LoadTrackers(val taskId: String) : TaskDetailIntent()
    data class LoadPeers(val taskId: String) : TaskDetailIntent()
    data class LoadFiles(val taskId: String) : TaskDetailIntent()
    data object ShowDeleteDialog : TaskDetailIntent()
    data object HideDeleteDialog : TaskDetailIntent()
    data class DeleteTask(val taskId: String, val onSuccess: () -> Unit = {}) : TaskDetailIntent()
}
