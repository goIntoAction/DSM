package wang.zengye.dsm.ui.file

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wang.zengye.dsm.data.model.AppDownloadStatus
import wang.zengye.dsm.data.model.AppDownloadTask
import wang.zengye.dsm.data.model.AppDownloadUiState
import wang.zengye.dsm.data.repository.DownloadTaskRepository
import wang.zengye.dsm.data.repository.FileDownloadRepository
import wang.zengye.dsm.service.DownloadService
import wang.zengye.dsm.service.DownloadStatus
import wang.zengye.dsm.ui.base.BaseViewModel
import wang.zengye.dsm.util.DownloadDirectoryManager
import wang.zengye.dsm.util.PermissionRequester
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

/**
 * 下载管理 ViewModel
 * 使用 Room 数据库进行持久化，通过 DownloadService 实现后台保活下载
 */
@HiltViewModel
class DownloadManagerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileDownloadRepository: FileDownloadRepository,
    private val downloadTaskRepository: DownloadTaskRepository
) : BaseViewModel<AppDownloadUiState, DownloadManagerIntent, DownloadManagerEvent>() {

    private val _state = MutableStateFlow(AppDownloadUiState())
    override val state: StateFlow<AppDownloadUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<DownloadManagerEvent>(extraBufferCapacity = 10)
    override val events = _events.asSharedFlow()

    // 任务ID映射：Service TaskId -> Room TaskId
    private val taskIdMap = mutableMapOf<String, String>()

    // 待请求通知权限后继续下载的任务（使用 AtomicReference 避免并发问题）
    private val pendingDownloadAfterPermission = AtomicReference<Pair<String, String>?>()

    // 监听 Service 进度的 Job
    private var monitorJob: Job? = null
    
    // 监听权限结果的 Job
    private var permissionJob: Job? = null

    init {
        // 异步加载下载目录信息和任务列表
        loadDownloadDirectory()
        observeTasks()
        monitorServiceProgress()
        observePermissionResults()
        // 修复App被杀死后遗留的"假运行"任务
        fixOrphanedTasks()
    }

    /**
     * 监听权限请求结果
     */
    private fun observePermissionResults() {
        permissionJob = viewModelScope.launch {
            PermissionRequester.permissionResultFlow.collect { result ->
                if (result.permission == Manifest.permission.POST_NOTIFICATIONS) {
                    handlePermissionResult(result.isGranted, result.shouldShowRationale)
                }
            }
        }
    }

    /**
     * 处理权限请求结果
     */
    private suspend fun handlePermissionResult(isGranted: Boolean, shouldShowRationale: Boolean) {
        if (isGranted) {
            // 用户授权，继续下载
            continueDownloadAfterPermissionGranted()
        } else {
            // 用户拒绝，可能显示警告
            if (shouldShowRationale) {
                _events.emit(DownloadManagerEvent.ShowNotificationPermissionDeniedWarning)
            }
            // 无论是否显示警告，都继续下载
            continueDownloadAfterPermissionGranted()
        }
    }

    /**
     * 修复App被杀死后遗留的"假运行"任务
     * 这些任务在数据库中显示RUNNING/PENDING，但实际上Service已不存在
     */
    private fun fixOrphanedTasks() {
        viewModelScope.launch {
            // 获取所有RUNNING和PENDING状态的任务
            val runningTasks = downloadTaskRepository.getTasksByStatus(
                listOf(AppDownloadStatus.RUNNING, AppDownloadStatus.PENDING)
            )

            if (runningTasks.isEmpty()) return@launch

            android.util.Log.d("DM_VM", "fixOrphanedTasks: Found ${runningTasks.size} running/pending tasks")

            // 检查DownloadService中是否有对应的任务
            val serviceTasks = DownloadService.tasks.value
            val activeServicePaths = serviceTasks.values.map { it.remotePath }.toSet()

            runningTasks.forEach { task ->
                // 如果Service中没有对应的任务，说明是"假运行"
                if (task.remotePath !in activeServicePaths) {
                    android.util.Log.d("DM_VM", "fixOrphanedTasks: Marking orphaned task as FAILED: ${task.id}")
                    downloadTaskRepository.updateTask(
                        task.copy(
                            status = AppDownloadStatus.FAILED,
                            errorMessage = "下载被中断（App被关闭）",
                            finishTime = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    override suspend fun processIntent(intent: DownloadManagerIntent) {
        when (intent) {
            is DownloadManagerIntent.SetDownloadDirectory -> setDownloadDirectory(intent.uri)
            is DownloadManagerIntent.ClearDownloadDirectory -> clearDownloadDirectory()
            is DownloadManagerIntent.StartDownload -> startDownload(intent.remotePath, intent.fileName)
            is DownloadManagerIntent.CancelDownload -> cancelDownload(intent.taskId)
            is DownloadManagerIntent.RetryDownload -> retryDownload(intent.task)
            is DownloadManagerIntent.DeleteTask -> deleteTask(intent.task)
            is DownloadManagerIntent.ClearCompletedTasks -> clearCompletedTasks()
        }
    }

    /**
     * 加载下载目录信息
     */
    private fun loadDownloadDirectory() {
        viewModelScope.launch {
            val directoryUri = DownloadDirectoryManager.getDownloadDirectoryUri(context)
            android.util.Log.d("DM_VM", "loadDownloadDirectory: uri=$directoryUri")
            _state.update {
                val newState = it.copy(
                    currentDirectoryUri = directoryUri?.toString(),
                    hasDirectoryPermission = directoryUri != null
                )
                android.util.Log.d("DM_VM", "loadDownloadDirectory: hasPermission=${newState.hasDirectoryPermission}, uri=${newState.currentDirectoryUri}")
                newState
            }
        }
    }

    /**
     * 观察 Room 数据库的任务列表
     */
    private fun observeTasks() {
        viewModelScope.launch {
            downloadTaskRepository.getAllTasksFlow().collect { tasks ->
                android.util.Log.d("DM_VM", "observeTasks: received ${tasks.size} tasks from database")
                _state.update { it.copy(tasks = tasks) }
            }
        }
    }
    
    /**
     * 监听 DownloadService 的进度更新
     */
    private fun monitorServiceProgress() {
        monitorJob = viewModelScope.launch {
            android.util.Log.d("DM_VM", "monitorServiceProgress: Starting to monitor DownloadService.tasks")
            DownloadService.tasks.collect { serviceTasks ->
                android.util.Log.d("DM_VM", "monitorServiceProgress: Received ${serviceTasks.size} service tasks")
                serviceTasks.forEach { (serviceTaskId, serviceTask) ->
                    val roomTaskId = taskIdMap[serviceTaskId]
                    android.util.Log.d("DM_VM", "monitorServiceProgress: serviceTaskId=$serviceTaskId, roomTaskId=$roomTaskId, status=${serviceTask.status}, progress=${serviceTask.bytesDownloaded}/${serviceTask.bytesTotal}")
                    
                    if (roomTaskId == null) {
                        android.util.Log.w("DM_VM", "monitorServiceProgress: No roomTaskId mapping for serviceTaskId=$serviceTaskId")
                        return@forEach
                    }
                    
                    // 更新 Room 数据库中的任务状态
                    val roomTask = downloadTaskRepository.getTaskById(roomTaskId)
                    if (roomTask == null) {
                        android.util.Log.w("DM_VM", "monitorServiceProgress: Room task not found for id=$roomTaskId")
                        return@forEach
                    }
                    
                    val newStatus = when (serviceTask.status) {
                        DownloadStatus.PENDING -> AppDownloadStatus.PENDING
                        DownloadStatus.DOWNLOADING -> AppDownloadStatus.RUNNING
                        DownloadStatus.PAUSED -> AppDownloadStatus.PAUSED
                        DownloadStatus.COMPLETED -> AppDownloadStatus.COMPLETED
                        DownloadStatus.FAILED -> AppDownloadStatus.FAILED
                        DownloadStatus.CANCELLED -> AppDownloadStatus.CANCELLED
                    }
                    
                    if (roomTask.status != newStatus || 
                        roomTask.downloadedBytes != serviceTask.bytesDownloaded ||
                        roomTask.totalBytes != serviceTask.bytesTotal) {
                        android.util.Log.d("DM_VM", "monitorServiceProgress: Updating room task $roomTaskId: status=$newStatus, bytes=${serviceTask.bytesDownloaded}/${serviceTask.bytesTotal}")
                        downloadTaskRepository.updateTask(
                            roomTask.copy(
                                status = newStatus,
                                downloadedBytes = serviceTask.bytesDownloaded,
                                totalBytes = serviceTask.bytesTotal,
                                finishTime = if (serviceTask.isCompleted || serviceTask.isFailed) System.currentTimeMillis() else null,
                                errorMessage = serviceTask.error
                            )
                        )
                    }
                    
                    // 清理已完成的任务映射
                    if (serviceTask.isCompleted || serviceTask.isFailed) {
                        taskIdMap.remove(serviceTaskId)
                        android.util.Log.d("DM_VM", "monitorServiceProgress: Cleaned up mapping for $serviceTaskId")
                    }
                }
            }
        }
    }

    /**
     * 设置下载目录
     * 设置完成后自动开始待下载的任务
     */
    private suspend fun setDownloadDirectory(uri: Uri) {
        android.util.Log.d("DM_VM", "setDownloadDirectory: uri=$uri")
        DownloadDirectoryManager.saveDownloadDirectoryUri(context, uri)
        _state.update {
            val newState = it.copy(
                currentDirectoryUri = uri.toString(),
                hasDirectoryPermission = true
            )
            android.util.Log.d("DM_VM", "setDownloadDirectory updated: hasPermission=${newState.hasDirectoryPermission}")
            newState
        }

        // 如果有待处理的下载任务，继续下载
        val pending = pendingDownloadAfterPermission.getAndSet(null)
        if (pending != null) {
            val (remotePath, fileName) = pending
            android.util.Log.d("DM_VM", "Continuing pending download after directory set: $fileName")
            doStartDownload(remotePath, fileName, uri)
            // 通知可以跳转到下载管理页面
            _events.emit(DownloadManagerEvent.NavigateToDownloadManager)
        }

        _events.emit(DownloadManagerEvent.ShowMessage("下载目录已设置"))
    }

    /**
     * 清除下载目录
     */
    private suspend fun clearDownloadDirectory() {
        val currentUri = _state.value.currentDirectoryUri?.let { Uri.parse(it) }
        DownloadDirectoryManager.clearDownloadDirectoryUri(context, currentUri)
        _state.update {
            it.copy(
                currentDirectoryUri = null,
                hasDirectoryPermission = false
            )
        }
    }

    /**
     * 开始下载
     * 统一的下载入口，会检查通知权限和下载目录
     */
    internal suspend fun startDownload(remotePath: String, fileName: String) {
        android.util.Log.d("DM_VM", "startDownload: $fileName, currentDirectoryUri=${_state.value.currentDirectoryUri}, hasPermission=${_state.value.hasDirectoryPermission}")

        // 1. 检查通知权限（Android 13+）
        if (!PermissionRequester.hasNotificationPermission()) {
            android.util.Log.d("DM_VM", "No notification permission, requesting via PermissionRequester")
            pendingDownloadAfterPermission.set(Pair(remotePath, fileName))
            val requested = PermissionRequester.requestNotificationPermission()
            if (!requested) {
                // 无法请求权限（可能是 Activity 不可用），直接继续下载
                android.util.Log.w("DM_VM", "Cannot request permission, continuing download anyway")
                continueDownloadAfterPermissionGranted()
            }
            // 权限请求已发出，等待结果通过 observePermissionResults 处理
            return
        }

        // 2. 检查下载目录
        val directoryUri = _state.value.currentDirectoryUri?.let { Uri.parse(it) }
        if (directoryUri == null) {
            android.util.Log.d("DM_VM", "No directory, sending NeedSetDownloadDirectory event")
            // 保存待下载任务，等待用户设置目录
            pendingDownloadAfterPermission.set(Pair(remotePath, fileName))
            _events.emit(DownloadManagerEvent.NeedSetDownloadDirectory)
            return
        }

        // 3. 创建任务并启动下载
        doStartDownload(remotePath, fileName, directoryUri)
        
        // 4. 通知可以跳转到下载管理页面
        _events.emit(DownloadManagerEvent.NavigateToDownloadManager)
    }

    /**
     * 通知权限处理后继续下载
     */
    private suspend fun continueDownloadAfterPermissionGranted() {
        val pending = pendingDownloadAfterPermission.getAndSet(null)

        if (pending != null) {
            val (remotePath, fileName) = pending
            val directoryUri = _state.value.currentDirectoryUri?.let { Uri.parse(it) }
            if (directoryUri != null) {
                doStartDownload(remotePath, fileName, directoryUri)
            } else {
                // 没有设置目录，提示用户设置
                pendingDownloadAfterPermission.set(pending) // 恢复待处理任务
                _events.emit(DownloadManagerEvent.NeedSetDownloadDirectory)
                return
            }
        }
        // 通知跳转
        _events.emit(DownloadManagerEvent.NavigateToDownloadManager)
    }

    /**
     * 实际执行下载（已通过权限检查）
     */
    private suspend fun doStartDownload(remotePath: String, fileName: String, directoryUri: Uri) {
        // 创建 Room 任务记录
        val roomTask = fileDownloadRepository.createDownloadTask(remotePath, directoryUri).copy(
            status = AppDownloadStatus.PENDING
        )
        android.util.Log.d("DM_VM", "Task created: ${roomTask.id}, saving to database")

        downloadTaskRepository.insertTask(roomTask)
        android.util.Log.d("DM_VM", "Task saved to database: ${roomTask.id}")

        // 建立 taskId 映射（Service 和 Room 使用同一个 ID）
        taskIdMap[roomTask.id] = roomTask.id

        // 使用 DownloadService 进行后台下载，传递 taskId
        android.util.Log.d("DM_VM", "Starting DownloadService with taskId=${roomTask.id}")
        DownloadService.startDownload(
            context = context,
            taskId = roomTask.id,
            remotePath = remotePath,
            fileName = fileName,
            directoryUri = directoryUri
        )
    }

    /**
     * 取消下载
     */
    private suspend fun cancelDownload(taskId: String) {
        // 更新 Room 数据库状态
        val task = downloadTaskRepository.getTaskById(taskId)
        if (task != null) {
            downloadTaskRepository.updateTask(
                task.copy(status = AppDownloadStatus.CANCELLED)
            )
        }
        
        // 取消 Service 中的任务
        DownloadService.cancelDownload(context, taskId)
        taskIdMap.remove(taskId)
    }

    /**
     * 重试下载
     */
    private suspend fun retryDownload(task: AppDownloadTask) {
        // 删除旧任务（包括临时文件）
        deleteTask(task)
        // 重新开始下载
        startDownload(task.remotePath, task.fileName)
    }

    /**
     * 删除任务
     */
    private suspend fun deleteTask(task: AppDownloadTask) {
        // 如果任务已完成且有本地文件，尝试删除文件
        if (task.status == AppDownloadStatus.COMPLETED) {
            val directoryUri = Uri.parse(task.directoryUri)
            fileDownloadRepository.deleteFile(directoryUri, task.fileName)
        }
        
        // 删除该任务的临时文件
        fileDownloadRepository.deleteTempFile(task.id)

        // 从数据库删除
        downloadTaskRepository.deleteTaskById(task.id)
        
        // 清理映射
        taskIdMap.remove(task.id)
    }

    /**
     * 清除已完成的任务
     */
    private suspend fun clearCompletedTasks() {
        downloadTaskRepository.deleteTasksByStatus(
            listOf(
                AppDownloadStatus.COMPLETED,
                AppDownloadStatus.FAILED,
                AppDownloadStatus.CANCELLED
            )
        )
        // 清理映射
        taskIdMap.entries.removeAll { entry ->
            val task = _state.value.tasks.find { it.id == entry.value }
            task?.status in listOf(AppDownloadStatus.COMPLETED, AppDownloadStatus.FAILED, AppDownloadStatus.CANCELLED)
        }
    }

    /**
     * 获取下载文件的本地 URI
     */
    fun getFileUri(task: AppDownloadTask): Uri? {
        if (task.status != AppDownloadStatus.COMPLETED) return null
        val directoryUri = Uri.parse(task.directoryUri)
        return fileDownloadRepository.getFileUri(directoryUri, task.fileName)
    }

    /**
     * 获取所有任务
     */
    fun getAllTasks(): List<AppDownloadTask> {
        return _state.value.tasks
    }

    /**
     * 获取进行中的任务数量
     */
    fun getActiveTaskCount(): Int {
        return _state.value.tasks.count {
            it.status == AppDownloadStatus.RUNNING || it.status == AppDownloadStatus.PENDING
        }
    }

    override fun onCleared() {
        super.onCleared()
        monitorJob?.cancel()
        permissionJob?.cancel()
        taskIdMap.clear()
    }
}
