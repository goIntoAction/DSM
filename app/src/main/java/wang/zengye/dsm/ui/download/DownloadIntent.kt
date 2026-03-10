package wang.zengye.dsm.ui.download

import wang.zengye.dsm.ui.base.BaseIntent

sealed class DownloadIntent : BaseIntent {
    data object LoadTasks : DownloadIntent()
    data object Refresh : DownloadIntent()
    data class SetFilter(val filter: String) : DownloadIntent()
    data class PauseTask(val id: String) : DownloadIntent()
    data class ResumeTask(val id: String) : DownloadIntent()
    data class DeleteTask(val id: String, val forceComplete: Boolean = false) : DownloadIntent()
    data object PauseAll : DownloadIntent()
    data object ResumeAll : DownloadIntent()
}
