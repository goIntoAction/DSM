package wang.zengye.dsm.ui.file

import android.content.Context
import android.net.Uri
import wang.zengye.dsm.ui.base.BaseIntent

sealed class FileUploadIntent : BaseIntent {
    data class SetTargetPath(val path: String) : FileUploadIntent()
    data class AddTasks(val uris: List<Uri>, val context: Context) : FileUploadIntent()
    data class RemoveTask(val taskId: String) : FileUploadIntent()
    data object ClearCompleted : FileUploadIntent()
    data class CancelTask(val taskId: String, val context: Context) : FileUploadIntent()
    data class RetryTask(val taskId: String, val context: Context) : FileUploadIntent()
    data class StartUpload(val context: Context) : FileUploadIntent()
}
