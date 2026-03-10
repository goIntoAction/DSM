package wang.zengye.dsm.ui.file

import android.net.Uri
import wang.zengye.dsm.data.model.AppDownloadTask
import wang.zengye.dsm.ui.base.BaseIntent

sealed class DownloadManagerIntent : BaseIntent {
    data class SetDownloadDirectory(val uri: Uri) : DownloadManagerIntent()
    data object ClearDownloadDirectory : DownloadManagerIntent()
    data class StartDownload(val remotePath: String, val fileName: String) : DownloadManagerIntent()
    data class CancelDownload(val taskId: String) : DownloadManagerIntent()
    data class RetryDownload(val task: AppDownloadTask) : DownloadManagerIntent()
    data class DeleteTask(val task: AppDownloadTask) : DownloadManagerIntent()
    data object ClearCompletedTasks : DownloadManagerIntent()
}