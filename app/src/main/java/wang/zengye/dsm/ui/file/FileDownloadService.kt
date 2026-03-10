package wang.zengye.dsm.ui.file

import android.app.DownloadManager
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import wang.zengye.dsm.R
import wang.zengye.dsm.data.api.DsmApiHelper
import wang.zengye.dsm.util.appString

/**
 * 文件下载状态
 */
data class DownloadState(
    val downloadId: Long = -1,
    val fileName: String = "",
    val filePath: String = "",
    val status: Int = DownloadManager.STATUS_PENDING,
    val bytesDownloaded: Long = 0,
    val bytesTotal: Long = 0,
    val progress: Int = 0,
    val localUri: Uri? = null
) {
    val isCompleted: Boolean
        get() = status == DownloadManager.STATUS_SUCCESSFUL
    
    val isFailed: Boolean
        get() = status == DownloadManager.STATUS_FAILED
    
    val isPaused: Boolean
        get() = status == DownloadManager.STATUS_PAUSED
    
    val isPending: Boolean
        get() = status == DownloadManager.STATUS_PENDING
    
    val isDownloading: Boolean
        get() = status == DownloadManager.STATUS_RUNNING
    
    val statusText: String
        get() = when (status) {
            DownloadManager.STATUS_PENDING -> appString(R.string.file_download_status_pending)
            DownloadManager.STATUS_RUNNING -> appString(R.string.file_download_status_running)
            DownloadManager.STATUS_PAUSED -> appString(R.string.file_download_status_paused)
            DownloadManager.STATUS_SUCCESSFUL -> appString(R.string.file_download_status_successful)
            DownloadManager.STATUS_FAILED -> appString(R.string.file_download_status_failed)
            else -> appString(R.string.file_download_status_unknown)
        }
}

/**
 * 文件下载管理器
 * 使用 Android DownloadManager 进行下载
 */
class FileDownloadManager(private val context: Context) {
    
    companion object {
        private const val TAG = "FileDownloadManager"
    }
    
    private val downloadManager = context.getSystemService<DownloadManager>()
    
    private val _downloads = MutableStateFlow<Map<Long, DownloadState>>(emptyMap())
    val downloads: StateFlow<Map<Long, DownloadState>> = _downloads.asStateFlow()
    
    private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            updateAllDownloads()
        }
    }
    
    init {
        // 注册内容观察者监听下载进度
        context.contentResolver.registerContentObserver(
            Uri.parse("content://downloads/my_downloads"),
            true,
            contentObserver
        )
    }
    
    /**
     * 开始下载文件
     */
    fun startDownload(
        remotePath: String,
        fileName: String,
        notificationVisibility: Int = DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
    ): Long? {
        val downloadUrl = DsmApiHelper.getDownloadUrl(remotePath)
        Log.d(TAG, "Download URL: $downloadUrl")
        
        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
            setTitle(fileName)
            setDescription(context.getString(R.string.file_download_description, fileName))

            // 添加 Cookie 和认证信息
            val sid = DsmApiHelper.getSessionId()
            val cookie = DsmApiHelper.cookie
            if (cookie.isNotEmpty()) {
                addRequestHeader("Cookie", cookie)
            }
            if (sid.isNotEmpty()) {
                addRequestHeader("Cookie", "id=$sid")
            }
            
            // 设置通知可见性
            setNotificationVisibility(notificationVisibility)
            
            // 设置保存路径
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "DSM/$fileName")
            
            // 允许移动网络下载
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }
        
        return try {
            val downloadId = downloadManager?.enqueue(request) ?: return null
            Log.d(TAG, "Download started with ID: $downloadId")
            
            // 添加到下载列表
            _downloads.update { map ->
                map + (downloadId to DownloadState(
                    downloadId = downloadId,
                    fileName = fileName,
                    filePath = remotePath
                ))
            }
            
            downloadId
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start download", e)
            null
        }
    }
    
    /**
     * 更新所有下载状态
     */
    private fun updateAllDownloads() {
        _downloads.value.keys.toList().forEach { downloadId ->
            updateDownloadStatus(downloadId)
        }
    }
    
    /**
     * 更新单个下载状态
     */
    fun updateDownloadStatus(downloadId: Long) {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager?.query(query) ?: return
        
        if (cursor.moveToFirst()) {
            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            val localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
            
            val status = cursor.getInt(statusIndex)
            val bytesDownloaded = cursor.getLong(bytesDownloadedIndex)
            val bytesTotal = cursor.getLong(bytesTotalIndex)
            val localUri = if (cursor.getString(localUriIndex) != null) {
                Uri.parse(cursor.getString(localUriIndex))
            } else null
            
            val progress = if (bytesTotal > 0) {
                (bytesDownloaded * 100 / bytesTotal).toInt()
            } else 0
            
            _downloads.update { map ->
                map[downloadId]?.let { state ->
                    map + (downloadId to state.copy(
                        status = status,
                        bytesDownloaded = bytesDownloaded,
                        bytesTotal = bytesTotal,
                        progress = progress,
                        localUri = localUri
                    ))
                } ?: map
            }
        }
        cursor.close()
    }
    
    /**
     * 取消下载
     */
    fun cancelDownload(downloadId: Long) {
        downloadManager?.remove(downloadId)
        _downloads.update { map ->
            map - downloadId
        }
    }
    
    /**
     * 清除已完成的下载
     */
    fun clearCompletedDownloads() {
        val completedIds = _downloads.value.filter { it.value.isCompleted || it.value.isFailed }.keys
        completedIds.forEach { id ->
            downloadManager?.remove(id)
        }
        _downloads.update { map ->
            map.filterKeys { !completedIds.contains(it) }
        }
    }
    
    /**
     * 释放资源
     */
    fun release() {
        context.contentResolver.unregisterContentObserver(contentObserver)
    }
}

/**
 * 文件下载 ViewModel
 */
class FileDownloadViewModel(private val context: Context) : ViewModel() {
    
    private val downloadManager = FileDownloadManager(context)
    
    val downloads: StateFlow<Map<Long, DownloadState>> = downloadManager.downloads
    
    /**
     * 开始下载文件
     */
    fun downloadFile(remotePath: String, fileName: String): Long? {
        return downloadManager.startDownload(remotePath, fileName)
    }
    
    /**
     * 取消下载
     */
    fun cancelDownload(downloadId: Long) {
        downloadManager.cancelDownload(downloadId)
    }
    
    /**
     * 清除已完成的下载
     */
    fun clearCompleted() {
        downloadManager.clearCompletedDownloads()
    }
    
    /**
     * 获取下载列表
     */
    fun getDownloadList(): List<DownloadState> {
        return downloads.value.values.toList().sortedByDescending { it.downloadId }
    }
    
    override fun onCleared() {
        downloadManager.release()
        super.onCleared()
    }
}

/**
 * 下载结果
 */
data class DownloadResult(
    val downloadId: Long,
    val localPath: String
)

/**
 * 下载任务创建回调
 */
interface DownloadTaskCallback {
    fun onTaskCreated(
        downloadId: Long,
        fileName: String,
        filePath: String,
        downloadUrl: String,
        localPath: String
    )
}

/**
 * 文件下载助手函数
 */
object FileDownloadHelper {
    
    // 下载任务回调
    private var callback: DownloadTaskCallback? = null
    
    /**
     * 设置下载任务回调
     */
    fun setCallback(callback: DownloadTaskCallback?) {
        this.callback = callback
    }
    
    /**
     * 下载单个文件
     * @return DownloadResult 包含下载ID和本地路径，失败返回null
     */
    fun downloadFile(
        context: Context,
        remotePath: String,
        fileName: String
    ): DownloadResult? {
        val downloadManager = context.getSystemService<DownloadManager>() ?: return null
        val downloadUrl = DsmApiHelper.getDownloadUrl(remotePath)
        val localPath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/DSM/$fileName"
        
        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
            setTitle(fileName)
            setDescription(context.getString(R.string.file_download_description, fileName))

            // 添加认证信息
            val cookie = DsmApiHelper.cookie
            if (cookie.isNotEmpty()) {
                addRequestHeader("Cookie", cookie)
            }
            
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "DSM/$fileName")
        }
        
        val downloadId = downloadManager.enqueue(request)
        
        // 通知回调
        callback?.onTaskCreated(downloadId, fileName, remotePath, downloadUrl, localPath)
        
        return DownloadResult(downloadId, localPath)
    }
    
    /**
     * 批量下载文件
     */
    fun downloadFiles(
        context: Context,
        files: List<Pair<String, String>> // (remotePath, fileName)
    ): List<DownloadResult> {
        return files.mapNotNull { (path, name) ->
            downloadFile(context, path, name)
        }
    }
    
    /**
     * 检查下载状态
     */
    fun checkDownloadStatus(context: Context, downloadId: Long): DownloadState? {
        val downloadManager = context.getSystemService<DownloadManager>() ?: return null
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query) ?: return null
        
        if (!cursor.moveToFirst()) {
            cursor.close()
            return null
        }
        
        val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
        val bytesDownloaded = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
        val bytesTotal = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
        val localUri = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
        
        cursor.close()
        
        return DownloadState(
            downloadId = downloadId,
            status = status,
            bytesDownloaded = bytesDownloaded,
            bytesTotal = bytesTotal,
            progress = if (bytesTotal > 0) (bytesDownloaded * 100 / bytesTotal).toInt() else 0,
            localUri = if (localUri != null) Uri.parse(localUri) else null
        )
    }
}
