package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseIntent

/**
 * 任务计划 Intent
 */
sealed class TaskSchedulerIntent : BaseIntent {
    data object LoadTasks : TaskSchedulerIntent()
    data class RunTask(val taskId: Int) : TaskSchedulerIntent()
    data class ToggleTask(val taskId: Int, val enabled: Boolean) : TaskSchedulerIntent()
    data class DeleteTask(val taskId: Int) : TaskSchedulerIntent()
}
