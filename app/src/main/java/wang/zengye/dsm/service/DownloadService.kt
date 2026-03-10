package wang.zengye.dsm.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import wang.zengye.dsm.data.repository.FileDownloadRepository
import wang.zengye.dsm.util.formatSize
import javax.inject.Inject

/**
 * 下载任务状态
 */
data class DownloadTask(
    val id: String,
    val remotePath: String,
    val fileName: String,
    val directoryUri: Uri,
    val bytesDownloaded: Long = 0,
    val bytesTotal: Long = 0,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val error: String? = null
) {
    val progress: Int
        get() = if (bytesTotal > 0) (bytesDownloaded * 100 / bytesTotal).toInt() else 0
    
    val isCompleted: Boolean
        get() = status == DownloadStatus.COMPLETED
    
    val isFailed: Boolean
        get() = status == DownloadStatus.FAILED
    
    val isDownloading: Boolean
        get() = status == DownloadStatus.DOWNLOADING
}

enum class DownloadStatus {
    PENDING, DOWNLOADING, PAUSED, COMPLETED, FAILED, CANCELLED
}

/**
 * 前台下载服务
 * 支持后台下载和进度通知，使用 SAF 存储文件
 */
@AndroidEntryPoint
class DownloadService : Service() {
    
    companion object {
        private const val TAG = "DownloadService"
        private const val CHANNEL_ID = "download_channel"
        private const val CHANNEL_NAME = "下载服务"
        private const val NOTIFICATION_ID = 1001
        
        const val ACTION_START_DOWNLOAD = "wang.zengye.dsm.action.START_DOWNLOAD"
        const val ACTION_CANCEL_DOWNLOAD = "wang.zengye.dsm.action.CANCEL_DOWNLOAD"
        const val ACTION_STOP_SERVICE = "wang.zengye.dsm.action.STOP_SERVICE"
        
        const val EXTRA_REMOTE_PATH = "remote_path"
        const val EXTRA_FILE_NAME = "file_name"
        const val EXTRA_DIRECTORY_URI = "directory_uri"
        const val EXTRA_TASK_ID = "task_id"
        
        private val _tasks = MutableStateFlow<Map<String, DownloadTask>>(emptyMap())
        val tasks: StateFlow<Map<String, DownloadTask>> = _tasks.asStateFlow()
        
        fun startDownload(context: Context, taskId: String, remotePath: String, fileName: String, directoryUri: Uri) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_START_DOWNLOAD
                putExtra(EXTRA_TASK_ID, taskId)
                putExtra(EXTRA_REMOTE_PATH, remotePath)
                putExtra(EXTRA_FILE_NAME, fileName)
                putExtra(EXTRA_DIRECTORY_URI, directoryUri.toString())
            }
            context.startService(intent)
        }
        
        fun cancelDownload(context: Context, taskId: String) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_CANCEL_DOWNLOAD
                putExtra(EXTRA_TASK_ID, taskId)
            }
            context.startService(intent)
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }
    }
    
    @Inject
    lateinit var fileDownloadRepository: FileDownloadRepository
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val downloadJobs = mutableMapOf<String, Job>()
    private val cancelFlags = mutableMapOf<String, Boolean>()
    private val binder = LocalBinder()
    
    inner class LocalBinder : Binder() {
        fun getService(): DownloadService = this@DownloadService
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(TAG, "DownloadService created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: action=${intent?.action}")
        when (intent?.action) {
            ACTION_START_DOWNLOAD -> {
                val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return START_STICKY
                val remotePath = intent.getStringExtra(EXTRA_REMOTE_PATH) ?: return START_STICKY
                val fileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: return START_STICKY
                val directoryUriStr = intent.getStringExtra(EXTRA_DIRECTORY_URI) ?: return START_STICKY
                val directoryUri = Uri.parse(directoryUriStr)
                Log.d(TAG, "onStartCommand: Starting download task $taskId for $fileName")
                startDownloadTask(taskId, remotePath, fileName, directoryUri)
            }
            ACTION_CANCEL_DOWNLOAD -> {
                val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return START_STICKY
                cancelDownloadTask(taskId)
            }
            ACTION_STOP_SERVICE -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
        Log.d(TAG, "DownloadService destroyed")
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "文件下载进度通知"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun startDownloadTask(taskId: String, remotePath: String, fileName: String, directoryUri: Uri) {
        // 使用传入的 taskId
        val task = DownloadTask(
            id = taskId,
            remotePath = remotePath,
            fileName = fileName,
            directoryUri = directoryUri
        )
        
        Log.d(TAG, "startDownloadTask: taskId=$taskId, fileName=$fileName")
        _tasks.value = _tasks.value + (taskId to task)
        
        // 启动前台服务
        startForeground(NOTIFICATION_ID, createNotification())
        
        val job = serviceScope.launch {
            try {
                Log.d(TAG, "[$taskId] Setting status to DOWNLOADING")
                updateTask(taskId) { it.copy(status = DownloadStatus.DOWNLOADING) }
                updateNotification()
                
                var lastProgressLog = 0L
                val result = fileDownloadRepository.downloadFile(
                    taskId = taskId,
                    remotePath = remotePath,
                    directoryUri = directoryUri,
                    fileName = fileName,
                    onProgress = { downloaded, total ->
                        updateTask(taskId) { 
                            it.copy(bytesDownloaded = downloaded, bytesTotal = total) 
                        }
                        // 每 500KB 或每 2 秒打印一次进度
                        if (downloaded - lastProgressLog > 500 * 1024 || 
                            (downloaded > 0 && System.currentTimeMillis() - lastProgressLog > 2000)) {
                            val progress = if (total > 0) (downloaded * 100 / total) else 0
                            Log.d(TAG, "[$taskId] Progress: $downloaded / $total bytes ($progress%)")
                            lastProgressLog = downloaded
                        }
                        // 每 100KB 更新一次通知
                        if (downloaded % (100 * 1024) < 8192) {
                            updateNotification()
                        }
                    },
                    isCancelled = { cancelFlags[taskId] == true }
                )
                
                Log.d(TAG, "[$taskId] Download result: ${if (result.isSuccess) "Success" else "Failed"}")
                result.fold(
                    onSuccess = {
                        Log.d(TAG, "[$taskId] Download COMPLETED")
                        updateTask(taskId) { it.copy(status = DownloadStatus.COMPLETED) }
                    },
                    onFailure = { error ->
                        val isCancelled = cancelFlags[taskId] == true || error.message?.contains("取消") == true
                        Log.e(TAG, "[$taskId] Download FAILED: ${error.message}, isCancelled=$isCancelled")
                        updateTask(taskId) { 
                            it.copy(
                                status = if (isCancelled) DownloadStatus.CANCELLED else DownloadStatus.FAILED, 
                                error = if (isCancelled) "已取消" else error.message
                            ) 
                        }
                    }
                )
                
            } catch (e: CancellationException) {
                Log.w(TAG, "[$taskId] Download CANCELLED")
                updateTask(taskId) { it.copy(status = DownloadStatus.CANCELLED) }
            } catch (e: Exception) {
                Log.e(TAG, "[$taskId] Download failed with exception", e)
                updateTask(taskId) { 
                    it.copy(status = DownloadStatus.FAILED, error = e.message) 
                }
            } finally {
                updateNotification()
            }
        }
        
        downloadJobs[taskId] = job
    }
    
    private fun cancelDownloadTask(taskId: String) {
        cancelFlags[taskId] = true
        downloadJobs[taskId]?.cancel()
        downloadJobs.remove(taskId)
        
        _tasks.value = _tasks.value[taskId]?.let { task ->
            _tasks.value + (taskId to task.copy(status = DownloadStatus.CANCELLED))
        } ?: _tasks.value
        
        updateNotification()
    }
    
    private fun updateTask(taskId: String, update: (DownloadTask) -> DownloadTask) {
        _tasks.value = _tasks.value[taskId]?.let { task ->
            _tasks.value + (taskId to update(task))
        } ?: _tasks.value
    }
    
    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, createNotification())
    }
    
    private fun createNotification(): Notification {
        val tasks = _tasks.value.values.toList()
        val activeTasks = tasks.filter { it.isDownloading }
        val completedTasks = tasks.filter { it.isCompleted }
        val failedTasks = tasks.filter { it.isFailed }
        val hasActiveTasks = activeTasks.isNotEmpty()
        
        val title = when {
            hasActiveTasks -> "正在下载 (${activeTasks.size})"
            completedTasks.isNotEmpty() -> "下载完成"
            failedTasks.isNotEmpty() -> "下载失败"
            else -> "下载服务"
        }
        
        val contentText = when {
            hasActiveTasks -> {
                val task = activeTasks.first()
                val moreCount = activeTasks.size - 1
                if (moreCount > 0) {
                    "${task.fileName} 及其他 ${moreCount} 个文件"
                } else {
                    "${task.fileName}: ${formatSize(task.bytesDownloaded)} / ${formatSize(task.bytesTotal)} (${task.progress}%)"
                }
            }
            completedTasks.size == 1 -> completedTasks.first().fileName
            completedTasks.isNotEmpty() -> "${completedTasks.size} 个文件下载成功"
            failedTasks.size == 1 -> "${failedTasks.first().fileName}: ${failedTasks.first().error ?: "下载失败"}"
            failedTasks.isNotEmpty() -> "${failedTasks.size} 个文件下载失败"
            else -> "暂无任务"
        }
        
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setOngoing(hasActiveTasks)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
        
        if (hasActiveTasks) {
            // 下载中：显示进度条
            val task = activeTasks.first()
            val progress = if (task.bytesTotal > 0) {
                (task.bytesDownloaded * 100 / task.bytesTotal).toInt().coerceIn(0, 100)
            } else 0
            builder.setSmallIcon(android.R.drawable.stat_sys_download)
                .setProgress(100, progress, false)
        } else {
            // 下载完成或失败：显示完成图标，不显示进度条
            builder.setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setProgress(0, 0, false)
        }
        
        return builder.build()
    }
}