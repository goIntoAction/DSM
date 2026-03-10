package wang.zengye.dsm.data.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wang.zengye.dsm.data.api.DsmApiHelper
import wang.zengye.dsm.data.model.AppDownloadTask
import wang.zengye.dsm.util.DownloadDirectoryManager
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 文件下载 Repository
 * 封装应用内下载逻辑，使用 OkHttp 和 SAF 文件操作
 */
@Singleton
class FileDownloadRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "FileDownloadRepository"
    }

    /**
     * 下载文件到指定目录
     * @param taskId 任务ID（用于临时文件命名，支持断点续传）
     * @param remotePath DSM 远程文件路径
     * @param directoryUri 下载目标目录 URI (SAF)
     * @param fileName 文件名
     * @param onProgress 进度回调 (已下载字节, 总字节)
     * @param isCancelled 取消检查 lambda
     * @return Result<Uri> 成功返回文件 URI，失败返回异常
     */
    suspend fun downloadFile(
        taskId: String,
        remotePath: String,
        directoryUri: Uri,
        fileName: String,
        onProgress: ((Long, Long) -> Unit)? = null,
        isCancelled: (() -> Boolean)? = null
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d(TAG, "downloadFile: taskId=$taskId, remotePath=$remotePath, fileName=$fileName")
            
            // 生成下载 URL
            val downloadUrl = DsmApiHelper.getDownloadUrl(remotePath)
            android.util.Log.d(TAG, "downloadFile: downloadUrl=$downloadUrl")

            // 生成唯一文件名（处理重名）
            val uniqueFileName = DownloadDirectoryManager.generateUniqueFileName(
                context, directoryUri, fileName
            )
            android.util.Log.d(TAG, "downloadFile: uniqueFileName=$uniqueFileName")

            // 使用 taskId 作为临时文件名，便于断点续传时找到
            val tempFile = File(context.cacheDir, "download_$taskId.tmp")
            
            // 检查是否有已存在的临时文件（断点续传）
            val existingBytes = if (tempFile.exists()) {
                val existingSize = tempFile.length()
                android.util.Log.d(TAG, "downloadFile: Found existing temp file, size=$existingSize bytes")
                existingSize
            } else {
                0L
            }

            // 构建 OkHttp 请求
            val requestBuilder = okhttp3.Request.Builder()
                .url(downloadUrl)
                .header("Cookie", DsmApiHelper.cookie)
                .get()

            // 断点续传：添加 Range header
            if (existingBytes > 0) {
                requestBuilder.header("Range", "bytes=$existingBytes-")
                android.util.Log.d(TAG, "downloadFile: Resuming from byte $existingBytes")
            }

            val request = requestBuilder.build()
            
            android.util.Log.d(TAG, "downloadFile: Starting download from byte $existingBytes")
            val response = DsmApiHelper.fileTransferClient.newCall(request).execute()

            // 206 Partial Content 表示支持断点续传，200 OK 表示从头开始
            val isPartialContent = response.code == 206
            if (!response.isSuccessful && response.code != 206) {
                android.util.Log.e(TAG, "downloadFile: HTTP ${response.code} ${response.message}")
                return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }

            // Content-Length 在断点续传时是剩余长度
            val remainingLength = response.body?.contentLength() ?: -1
            val totalLength = if (isPartialContent && remainingLength > 0) {
                existingBytes + remainingLength
            } else {
                remainingLength
            }
            android.util.Log.d(TAG, "downloadFile: Content-Length = $remainingLength bytes, Total = $totalLength bytes")

            response.body?.byteStream()?.use { input ->
                // 断点续传时使用 append 模式
                val appendMode = existingBytes > 0 && tempFile.exists()
                FileOutputStream(tempFile, appendMode).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Long = 0
                    var read: Int
                    var lastLogTime = System.currentTimeMillis()

                    while (input.read(buffer).also { read = it } != -1) {
                        // 检查是否已取消
                        if (isCancelled?.invoke() == true) {
                            throw DownloadCancelledException("Download cancelled by user")
                        }
                        
                        output.write(buffer, 0, read)
                        bytesRead += read

                        // 每 1 秒打印一次进度
                        val now = System.currentTimeMillis()
                        if (now - lastLogTime > 1000) {
                            val totalRead = existingBytes + bytesRead
                            android.util.Log.d(TAG, "downloadFile: Progress $totalRead / $totalLength bytes")
                            lastLogTime = now
                        }

                        // 进度需要加上已下载的字节
                        onProgress?.invoke(existingBytes + bytesRead, totalLength)
                    }
                }
            }

            android.util.Log.d(TAG, "downloadFile: Download successful, copying to SAF...")
            
            // 通过 SAF 将文件保存到目标目录
            val finalUri = DownloadDirectoryManager.writeFileContent(
                context, directoryUri, uniqueFileName, tempFile
            )

            // 成功后才删除临时文件
            tempFile.delete()

            if (finalUri != null) {
                android.util.Log.d(TAG, "downloadFile: File saved to $finalUri")
                Result.success(finalUri)
            } else {
                android.util.Log.e(TAG, "downloadFile: Failed to save file to SAF")
                // 保留临时文件以便重试
                Result.failure(Exception("保存文件失败"))
            }
        } catch (e: DownloadCancelledException) {
            // 保留临时文件以便续传
            android.util.Log.w(TAG, "downloadFile: Cancelled, temp file kept for resume")
            Result.failure(Exception("下载已取消"))
        } catch (e: Exception) {
            // 保留临时文件以便重试
            android.util.Log.e(TAG, "downloadFile: Exception, temp file kept for retry", e)
            Result.failure(e)
        }
    }
    
    /**
     * 获取任务的临时文件
     * @param taskId 任务ID
     * @return 临时文件，如果不存在返回 null
     */
    fun getTempFile(taskId: String): File? {
        val tempFile = File(context.cacheDir, "download_$taskId.tmp")
        return if (tempFile.exists()) tempFile else null
    }
    
    /**
     * 删除任务的临时文件
     * @param taskId 任务ID
     */
    fun deleteTempFile(taskId: String) {
        val tempFile = File(context.cacheDir, "download_$taskId.tmp")
        if (tempFile.exists()) {
            if (tempFile.delete()) {
                android.util.Log.d(TAG, "deleteTempFile: Deleted temp file for task $taskId")
            }
        }
    }

    /**
     * 获取已下载文件的 URI
     * @param directoryUri 下载目录 URI
     * @param fileName 文件名
     * @return 文件 URI，如果不存在返回 null
     */
    fun getFileUri(directoryUri: Uri, fileName: String): Uri? {
        return DownloadDirectoryManager.getFileUri(context, directoryUri, fileName)
    }

    /**
     * 删除已下载的文件
     * @param directoryUri 下载目录 URI
     * @param fileName 文件名
     * @return 是否删除成功
     */
    fun deleteFile(directoryUri: Uri, fileName: String): Boolean {
        return DownloadDirectoryManager.deleteFile(context, directoryUri, fileName)
    }

    /**
     * 创建下载任务
     * @param remotePath DSM 远程文件路径
     * @param directoryUri 下载目录 URI
     * @return AppDownloadTask 任务对象
     */
    fun createDownloadTask(
        remotePath: String,
        directoryUri: Uri
    ): AppDownloadTask {
        val fileName = remotePath.substringAfterLast("/")
        val downloadUrl = DsmApiHelper.getDownloadUrl(remotePath)

        return AppDownloadTask.create(
            fileName = fileName,
            remotePath = remotePath,
            downloadUrl = downloadUrl,
            directoryUri = directoryUri.toString()
        )
    }

    /**
     * 下载取消异常
     */
    private class DownloadCancelledException(message: String) : Exception(message)
}