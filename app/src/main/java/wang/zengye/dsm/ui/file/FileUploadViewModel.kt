package wang.zengye.dsm.ui.file

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import wang.zengye.dsm.R
import wang.zengye.dsm.util.appString
import wang.zengye.dsm.ui.base.BaseViewModel
import wang.zengye.dsm.service.UploadService
import wang.zengye.dsm.service.UploadTask
import wang.zengye.dsm.service.UploadStatus

data class UploadUiState(
    val targetPath: String = "/",
    val tasks: List<UploadTask> = emptyList(),
    override val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    override val error: String? = null,
    val currentUploadingId: String? = null
) : wang.zengye.dsm.ui.base.BaseState

@HiltViewModel
class UploadViewModel @Inject constructor(
    // 不再需要 FileRepository，使用 UploadService
) : BaseViewModel<UploadUiState, FileUploadIntent, FileUploadEvent>() {

    private val _state = MutableStateFlow(UploadUiState())
    override val state: StateFlow<UploadUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<FileUploadEvent>(extraBufferCapacity = 10)
    override val events = _events.asSharedFlow()

    // 本地任务列表（用于显示和管理）
    private val _localTasks = mutableMapOf<String, UploadTask>()

    // Service 任务 ID 到本地任务 ID 的映射
    private val _taskIdMap = mutableMapOf<String, String>()

    override suspend fun processIntent(intent: FileUploadIntent) {
        when (intent) {
            is FileUploadIntent.SetTargetPath -> setTargetPath(intent.path)
            is FileUploadIntent.AddTasks -> addTasks(intent.uris, intent.context)
            is FileUploadIntent.RemoveTask -> removeTask(intent.taskId)
            is FileUploadIntent.ClearCompleted -> clearCompleted()
            is FileUploadIntent.CancelTask -> cancelTask(intent.taskId, intent.context)
            is FileUploadIntent.RetryTask -> retryTask(intent.taskId, intent.context)
            is FileUploadIntent.StartUpload -> startUpload(intent.context)
        }
    }

    private fun setTargetPath(path: String) {
        _state.update { it.copy(targetPath = path) }
    }

    private fun addTasks(uris: List<Uri>, context: Context) {
        val newTasks = uris.map { uri ->
            val fileName = getFileName(context, uri)
            val fileSize = getFileSize(context, uri)
            val task = UploadTask(
                id = System.currentTimeMillis().toString() + uri.hashCode(),
                fileName = fileName,
                uri = uri,
                uploadType = wang.zengye.dsm.service.UploadType.FILE,
                bytesTotal = fileSize
            )
            _localTasks[task.id] = task
            task
        }

        _state.update { it.copy(tasks = it.tasks + newTasks) }
    }

    private fun removeTask(taskId: String) {
        _localTasks.remove(taskId)
        _state.update { it.copy(tasks = it.tasks.filter { t -> t.id != taskId }) }
    }

    private fun clearCompleted() {
        _state.value.tasks.filter { it.status == UploadStatus.COMPLETED }.forEach {
            _localTasks.remove(it.id)
        }
        _state.update { it.copy(tasks = it.tasks.filter { t -> t.status != UploadStatus.COMPLETED }) }
    }

    private fun cancelTask(taskId: String, context: Context) {
        val serviceTaskId = _taskIdMap.entries.find { it.value == taskId }?.key
        if (serviceTaskId != null) {
            UploadService.cancelUpload(context, serviceTaskId)
        }
        updateTask(taskId) { it.copy(status = UploadStatus.CANCELLED) }
    }

    private suspend fun retryTask(taskId: String, context: Context) {
        val task = _localTasks[taskId] ?: return
        updateTask(taskId) { it.copy(status = UploadStatus.PENDING, bytesUploaded = 0, error = null) }

        // 直接重新上传
        startUpload(context)
    }

    private suspend fun startUpload(context: Context) {
        _state.update { it.copy(isUploading = true, error = null) }

        val pendingTasks = _state.value.tasks.filter { it.status == UploadStatus.PENDING }
        val destFolderPath = _state.value.targetPath

        // 监听 UploadService 进度
        val monitorJob = viewModelScope.launch {
            UploadService.tasks.collect { serviceTasks ->
                serviceTasks.forEach { (serviceTaskId, serviceTask) ->
                    val localTaskId = _taskIdMap[serviceTaskId] ?: return@forEach

                    // 更新进度
                    updateTask(localTaskId) {
                        it.copy(
                            bytesUploaded = serviceTask.bytesUploaded,
                            bytesTotal = serviceTask.bytesTotal,
                            status = serviceTask.status,
                            error = serviceTask.error
                        )
                    }

                    // 更新当前上传 ID
                    if (serviceTask.isUploading) {
                        _state.update { it.copy(currentUploadingId = localTaskId) }
                    }
                }
            }
        }

        // 使用 UploadService 批量上传，获取返回的 serviceTaskIds
        val uris = pendingTasks.map { it.uri }
        val fileNames = pendingTasks.map { it.fileName }

        val serviceTaskIds = UploadService.startFileUploadMultiple(context, uris, fileNames, destFolderPath)

        // 建立 taskId 映射（Service 返回的 ID -> 本地任务 ID）
        serviceTaskIds.forEachIndexed { index, serviceTaskId ->
            val localTaskId = pendingTasks.getOrNull(index)?.id
            if (localTaskId != null) {
                _taskIdMap[serviceTaskId] = localTaskId
            }
        }

        // 等待所有任务完成（带超时保护）
        val timeoutMs = 10 * 60 * 1000L  // 10 分钟超时
        val startTime = System.currentTimeMillis()

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            kotlinx.coroutines.delay(500)
            val allDone = _state.value.tasks.all {
                it.status == UploadStatus.COMPLETED ||
                it.status == UploadStatus.FAILED ||
                it.status == UploadStatus.CANCELLED
            }
            if (allDone) break
        }

        monitorJob.cancel()
        _state.update { it.copy(isUploading = false, currentUploadingId = null) }

        // 发送完成事件
        _events.emit(FileUploadEvent.UploadCompleted)
    }

    private fun updateTask(taskId: String, update: (UploadTask) -> UploadTask) {
        _state.update { state ->
            state.copy(
                tasks = state.tasks.map { task ->
                    if (task.id == taskId) update(task) else task
                }
            )
        }
        // 同步更新本地缓存
        _localTasks[taskId]?.let { _localTasks[taskId] = update(it) }
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var name = "unknown"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    private fun getFileSize(context: Context, uri: Uri): Long {
        var size = 0L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
            if (cursor.moveToFirst() && sizeIndex >= 0) {
                size = cursor.getLong(sizeIndex)
            }
        }
        return size
    }
}