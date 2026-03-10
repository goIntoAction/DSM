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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import wang.zengye.dsm.data.api.DsmApiHelper
import wang.zengye.dsm.data.api.ProgressRequestBody
import wang.zengye.dsm.util.formatSize
import java.io.File
import java.io.FileOutputStream

/**
 * 上传任务类型
 */
enum class UploadType {
    PHOTO,      // 照片上传到 Photos 应用 (SYNO.Foto.Upload.Item)
    FILE        // 文件上传到指定目录 (SYNO.FileStation.Upload)
}

/**
 * 上传任务状态
 */
data class UploadTask(
    val id: String,
    val fileName: String,
    val uri: Uri,
    val folderId: Long = 0,           // 用于照片上传
    val destFolderPath: String = "",  // 用于文件上传
    val uploadType: UploadType = UploadType.PHOTO,
    val bytesUploaded: Long = 0,
    val bytesTotal: Long = 0,
    val status: UploadStatus = UploadStatus.PENDING,
    val error: String? = null
) {
    val progress: Int
        get() = if (bytesTotal > 0) (bytesUploaded * 100 / bytesTotal).toInt() else 0

    val isCompleted: Boolean
        get() = status == UploadStatus.COMPLETED

    val isFailed: Boolean
        get() = status == UploadStatus.FAILED

    val isUploading: Boolean
        get() = status == UploadStatus.UPLOADING
}

enum class UploadStatus {
    PENDING, UPLOADING, COMPLETED, FAILED, CANCELLED
}

/**
 * 前台上传服务
 * 支持后台上传照片/视频和文件，带进度通知
 */
class UploadService : Service() {

    companion object {
        private const val TAG = "UploadService"
        private const val CHANNEL_ID = "upload_channel"
        private const val CHANNEL_NAME = "上传服务"
        private const val NOTIFICATION_ID = 1002

        const val ACTION_START_UPLOAD = "wang.zengye.dsm.action.START_UPLOAD"
        const val ACTION_CANCEL_UPLOAD = "wang.zengye.dsm.action.CANCEL_UPLOAD"
        const val ACTION_STOP_SERVICE = "wang.zengye.dsm.action.STOP_SERVICE"

        const val EXTRA_URI = "uri"
        const val EXTRA_FILE_NAME = "file_name"
        const val EXTRA_FOLDER_ID = "folder_id"
        const val EXTRA_DEST_FOLDER_PATH = "dest_folder_path"
        const val EXTRA_UPLOAD_TYPE = "upload_type"
        const val EXTRA_TASK_ID = "task_id"

        private val _tasks = MutableStateFlow<Map<String, UploadTask>>(emptyMap())
        val tasks: StateFlow<Map<String, UploadTask>> = _tasks.asStateFlow()

        /**
         * 上传照片到 Photos 应用
         */
        fun startPhotoUpload(context: Context, uri: Uri, fileName: String, folderId: Long = 0) {
            val intent = Intent(context, UploadService::class.java).apply {
                action = ACTION_START_UPLOAD
                putExtra(EXTRA_URI, uri)
                putExtra(EXTRA_FILE_NAME, fileName)
                putExtra(EXTRA_FOLDER_ID, folderId)
                putExtra(EXTRA_UPLOAD_TYPE, UploadType.PHOTO.name)
            }
            context.startService(intent)
        }

        /**
         * 上传多个照片到 Photos 应用
         */
        fun startPhotoUploadMultiple(context: Context, uris: List<Uri>, fileNames: List<String>, folderId: Long = 0) {
            uris.forEachIndexed { index, uri ->
                startPhotoUpload(context, uri, fileNames.getOrNull(index) ?: "file_$index", folderId)
            }
        }

        /**
         * 上传文件到指定目录
         * @return 生成的任务 ID
         */
        fun startFileUpload(context: Context, uri: Uri, fileName: String, destFolderPath: String): String {
            val taskId = System.currentTimeMillis().toString() + "_" + fileName.hashCode()
            val intent = Intent(context, UploadService::class.java).apply {
                action = ACTION_START_UPLOAD
                putExtra(EXTRA_URI, uri)
                putExtra(EXTRA_FILE_NAME, fileName)
                putExtra(EXTRA_DEST_FOLDER_PATH, destFolderPath)
                putExtra(EXTRA_UPLOAD_TYPE, UploadType.FILE.name)
                putExtra(EXTRA_TASK_ID, taskId)  // 传递预生成的 taskId
            }
            context.startService(intent)
            return taskId
        }

        /**
         * 上传多个文件到指定目录
         * @return 生成的任务 ID 列表（与 uris 顺序对应）
         */
        fun startFileUploadMultiple(context: Context, uris: List<Uri>, fileNames: List<String>, destFolderPath: String): List<String> {
            return uris.mapIndexed { index, uri ->
                startFileUpload(context, uri, fileNames.getOrNull(index) ?: "file_$index", destFolderPath)
            }
        }

        fun cancelUpload(context: Context, taskId: String) {
            val intent = Intent(context, UploadService::class.java).apply {
                action = ACTION_CANCEL_UPLOAD
                putExtra(EXTRA_TASK_ID, taskId)
            }
            context.startService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, UploadService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val uploadJobs = mutableMapOf<String, Job>()
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): UploadService = this@UploadService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(TAG, "UploadService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_UPLOAD -> {
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_URI, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_URI)
                } ?: return START_STICKY
                val fileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: return START_STICKY
                val uploadTypeStr = intent.getStringExtra(EXTRA_UPLOAD_TYPE) ?: UploadType.PHOTO.name
                val uploadType = try { UploadType.valueOf(uploadTypeStr) } catch (e: Exception) { UploadType.PHOTO }
                // 优先使用传递过来的 taskId，否则生成新的
                val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: System.currentTimeMillis().toString()

                when (uploadType) {
                    UploadType.PHOTO -> {
                        val folderId = intent.getLongExtra(EXTRA_FOLDER_ID, 0)
                        startPhotoUploadTask(uri, fileName, folderId, taskId)
                    }
                    UploadType.FILE -> {
                        val destFolderPath = intent.getStringExtra(EXTRA_DEST_FOLDER_PATH) ?: return START_STICKY
                        startFileUploadTask(uri, fileName, destFolderPath, taskId)
                    }
                }
            }
            ACTION_CANCEL_UPLOAD -> {
                val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return START_STICKY
                cancelUploadTask(taskId)
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
        Log.d(TAG, "UploadService destroyed")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "文件上传进度通知"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * 照片上传任务 (SYNO.Foto.Upload.Item)
     */
    private fun startPhotoUploadTask(uri: Uri, fileName: String, folderId: Long, taskId: String) {
        val task = UploadTask(
            id = taskId,
            fileName = fileName,
            uri = uri,
            folderId = folderId,
            uploadType = UploadType.PHOTO
        )

        _tasks.value = _tasks.value + (taskId to task)
        startForeground(NOTIFICATION_ID, createNotification())

        val job = serviceScope.launch {
            var tempFile: File? = null
            try {
                updateTask(taskId) { it.copy(status = UploadStatus.UPLOADING) }
                updateNotification()

                tempFile = File(cacheDir, "upload_$taskId")
                val totalSize = copyUriToTempFile(uri, tempFile, taskId)
                updateTask(taskId) { it.copy(bytesTotal = totalSize) }

                val isDsm7 = DsmApiHelper.dsmVersion >= 7
                val apiName = if (isDsm7) "SYNO.Foto.Upload.Item" else "SYNO.Photo.Upload"

                Log.d(TAG, "[$taskId] Photo upload: fileName=$fileName, folderId=$folderId, size=$totalSize")

                val uploadUrl = buildString {
                    append(DsmApiHelper.baseUrl)
                    append("/webapi/entry.cgi")
                    append("?api=").append(apiName)
                    append("&version=1")
                    append("&method=upload")
                    append("&_sid=").append(DsmApiHelper.getSessionId())
                }

                val mediaType = contentResolver.getType(uri)?.toMediaTypeOrNull() ?: "image/jpeg".toMediaTypeOrNull()
                val mtime = tempFile.lastModified()

                // 使用 ProgressRequestBody 追踪上传进度
                val progressRequestBody = ProgressRequestBody(
                    file = tempFile,
                    contentType = mediaType!!,
                    progressCallback = { bytesWritten, _ ->
                        updateTask(taskId) { it.copy(bytesUploaded = bytesWritten) }
                        updateNotification()
                    },
                    isCancelled = {
                        uploadJobs[taskId]?.isCancelled == true
                    }
                )

                val multipartBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("path", "\"/\"")
                    .addFormDataPart("folder_id", folderId.toString())
                    .addFormDataPart("name", "\"$fileName\"")
                    .addFormDataPart("size", totalSize.toString())
                    .addFormDataPart("mtime", (mtime / 1000).toString())
                    .addFormDataPart("create_parents", "true")
                    .addFormDataPart("overwrite", "false")
                    .addFormDataPart("duplicate", "\"rename\"")
                    .addFormDataPart("file", fileName, progressRequestBody)
                    .build()

                val request = okhttp3.Request.Builder()
                    .url(uploadUrl)
                    .post(multipartBody)
                    .build()

                val response: Response = DsmApiHelper.fileTransferClient.newCall(request).execute()
                handleUploadResponse(taskId, response)

            } catch (e: CancellationException) {
                updateTask(taskId) { it.copy(status = UploadStatus.CANCELLED) }
            } catch (e: Exception) {
                Log.e(TAG, "Photo upload failed", e)
                updateTask(taskId) { it.copy(status = UploadStatus.FAILED, error = e.message) }
            } finally {
                tempFile?.delete()
                updateNotification()
            }
        }

        uploadJobs[taskId] = job
    }

    /**
     * 文件上传任务 (SYNO.FileStation.Upload)
     */
    private fun startFileUploadTask(uri: Uri, fileName: String, destFolderPath: String, taskId: String) {
        val task = UploadTask(
            id = taskId,
            fileName = fileName,
            uri = uri,
            destFolderPath = destFolderPath,
            uploadType = UploadType.FILE
        )

        _tasks.value = _tasks.value + (taskId to task)
        startForeground(NOTIFICATION_ID, createNotification())

        val job = serviceScope.launch {
            var tempFile: File? = null
            try {
                updateTask(taskId) { it.copy(status = UploadStatus.UPLOADING) }
                updateNotification()

                tempFile = File(cacheDir, "upload_$taskId")
                val totalSize = copyUriToTempFile(uri, tempFile, taskId)
                updateTask(taskId) { it.copy(bytesTotal = totalSize) }

                Log.d(TAG, "[$taskId] File upload: fileName=$fileName, destFolderPath=$destFolderPath, size=$totalSize")

                val uploadUrl = buildString {
                    append(DsmApiHelper.baseUrl)
                    append("/webapi/entry.cgi")
                    append("?api=SYNO.FileStation.Upload")
                    append("&version=2")
                    append("&method=upload")
                    append("&_sid=").append(DsmApiHelper.getSessionId())
                }

                val mediaType = contentResolver.getType(uri)?.toMediaTypeOrNull() ?: "application/octet-stream".toMediaTypeOrNull()
                val mtime = tempFile.lastModified()

                // 使用 ProgressRequestBody 追踪上传进度
                val progressRequestBody = ProgressRequestBody(
                    file = tempFile,
                    contentType = mediaType!!,
                    progressCallback = { bytesWritten, _ ->
                        updateTask(taskId) { it.copy(bytesUploaded = bytesWritten) }
                        updateNotification()
                    },
                    isCancelled = {
                        uploadJobs[taskId]?.isCancelled == true
                    }
                )

                val multipartBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("path", destFolderPath)
                    .addFormDataPart("create_parents", "true")
                    .addFormDataPart("size", totalSize.toString())
                    .addFormDataPart("mtime", mtime.toString())
                    .addFormDataPart("overwrite", "true")
                    .addFormDataPart("file", fileName, progressRequestBody)
                    .build()

                val request = okhttp3.Request.Builder()
                    .url(uploadUrl)
                    .post(multipartBody)
                    .build()

                val response: Response = DsmApiHelper.fileTransferClient.newCall(request).execute()
                handleUploadResponse(taskId, response)

            } catch (e: CancellationException) {
                updateTask(taskId) { it.copy(status = UploadStatus.CANCELLED) }
            } catch (e: Exception) {
                Log.e(TAG, "File upload failed", e)
                updateTask(taskId) { it.copy(status = UploadStatus.FAILED, error = e.message) }
            } finally {
                tempFile?.delete()
                updateNotification()
            }
        }

        uploadJobs[taskId] = job
    }

    private fun handleUploadResponse(taskId: String, response: Response) {
        Log.d(TAG, "[$taskId] Response: code=${response.code}")
        val responseBody = response.body?.string()
        Log.d(TAG, "[$taskId] Response body: $responseBody")

        if (response.isSuccessful) {
            val json = responseBody?.let { org.json.JSONObject(it) }
            val success = json?.optBoolean("success", false) ?: false
            if (success) {
                Log.d(TAG, "[$taskId] Upload COMPLETED")
                updateTask(taskId) { it.copy(status = UploadStatus.COMPLETED) }
            } else {
                val error = json?.optJSONObject("error")
                val errorCode = error?.optInt("code", -1) ?: -1
                val errorMsg = error?.optString("msg") ?: error?.optString("message") ?: "Upload failed (code=$errorCode)"
                Log.e(TAG, "[$taskId] Upload FAILED: $errorMsg")
                updateTask(taskId) { it.copy(status = UploadStatus.FAILED, error = errorMsg) }
            }
        } else {
            Log.e(TAG, "[$taskId] Upload FAILED: HTTP ${response.code}")
            updateTask(taskId) { it.copy(status = UploadStatus.FAILED, error = "HTTP ${response.code}: ${response.message}") }
        }
    }

    private suspend fun copyUriToTempFile(uri: Uri, tempFile: File, taskId: String): Long {
        var totalSize = 0L

        withContext(Dispatchers.IO) {
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        ensureActive()
                        output.write(buffer, 0, bytesRead)
                        totalSize += bytesRead
                        updateTask(taskId) { it.copy(bytesTotal = totalSize) }
                    }
                }
            }
        }

        return totalSize
    }

    private fun cancelUploadTask(taskId: String) {
        uploadJobs[taskId]?.cancel()
        uploadJobs.remove(taskId)

        _tasks.value = _tasks.value[taskId]?.let { task ->
            _tasks.value + (taskId to task.copy(status = UploadStatus.CANCELLED))
        } ?: _tasks.value

        updateNotification()
    }

    private fun updateTask(taskId: String, update: (UploadTask) -> UploadTask) {
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
        val activeTasks = tasks.filter { it.isUploading }
        val completedTasks = tasks.filter { it.isCompleted }
        val failedTasks = tasks.filter { it.isFailed }

        val title = when {
            activeTasks.isNotEmpty() -> "正在上传 (${activeTasks.size})"
            completedTasks.isNotEmpty() -> "上传完成 (${completedTasks.size})"
            failedTasks.isNotEmpty() -> "上传失败 (${failedTasks.size})"
            else -> "上传服务"
        }

        val contentText = if (activeTasks.isNotEmpty()) {
            val task = activeTasks.first()
            "${task.fileName}: ${formatSize(task.bytesUploaded)} / ${formatSize(task.bytesTotal)} (${task.progress}%)"
        } else {
            "共 ${tasks.size} 个任务"
        }

        val progress = if (activeTasks.isNotEmpty()) {
            val task = activeTasks.first()
            task.progress
        } else 0

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setOngoing(activeTasks.isNotEmpty())
            .setProgress(100, progress, activeTasks.isEmpty() && progress == 0)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .build()
    }
}
