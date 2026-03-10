package wang.zengye.dsm.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.flow.first
import java.io.File

/**
 * 下载目录管理器
 * 管理默认下载目录和 SAF (Storage Access Framework) URI 持久化
 */
object DownloadDirectoryManager {

    private const val DEFAULT_DOWNLOAD_FOLDER = "DSM"

    /**
     * 获取默认下载目录
     * 使用系统 Downloads 目录下的 DSM 子目录
     */
    fun getDefaultDownloadDirectory(): File {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return File(downloadsDir, DEFAULT_DOWNLOAD_FOLDER).apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * 保存下载目录 URI
     * 持久化 URI 权限，以便重启后仍可访问
     */
    suspend fun saveDownloadDirectoryUri(context: Context, uri: Uri) {
        // 持久化 URI 权限
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        // 保存 URI 字符串
        SettingsManager.setDownloadDirectoryUri(uri.toString())
    }

    /**
     * 获取已保存的下载目录 URI
     */
    suspend fun getDownloadDirectoryUri(context: Context): Uri? {
        val uriString = SettingsManager.downloadDirectoryUri.first()
        return if (uriString.isNotEmpty()) {
            val uri = Uri.parse(uriString)
            // 验证权限是否仍然有效
            if (hasUriPermission(context, uri)) {
                uri
            } else {
                // 权限已失效，清除保存的 URI
                SettingsManager.setDownloadDirectoryUri("")
                null
            }
        } else {
            null
        }
    }

    /**
     * 检查 URI 权限是否有效
     */
    private fun hasUriPermission(context: Context, uri: Uri): Boolean {
        return context.contentResolver.persistedUriPermissions.any {
            it.uri == uri
        }
    }

    /**
     * 清除下载目录 URI 和权限
     */
    suspend fun clearDownloadDirectoryUri(context: Context, uri: Uri?) {
        uri?.let {
            context.contentResolver.releasePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }
        SettingsManager.setDownloadDirectoryUri("")
    }

    /**
     * 在指定目录创建文件
     * 使用 SAF DocumentFile API
     */
    fun createFileInDirectory(context: Context, directoryUri: Uri, fileName: String): DocumentFile? {
        val directory = DocumentFile.fromTreeUri(context, directoryUri)
        return directory?.createFile("*/*", fileName)
    }

    /**
     * 检查目录中是否已存在同名文件
     */
    fun fileExists(context: Context, directoryUri: Uri, fileName: String): Boolean {
        val directory = DocumentFile.fromTreeUri(context, directoryUri)
        return directory?.findFile(fileName) != null
    }

    /**
     * 生成唯一的文件名（处理重名）
     */
    fun generateUniqueFileName(context: Context, directoryUri: Uri, baseName: String): String {
        if (!fileExists(context, directoryUri, baseName)) {
            return baseName
        }

        val nameWithoutExt = baseName.substringBeforeLast(".")
        val extension = if (baseName.contains(".")) ".${baseName.substringAfterLast(".")}" else ""

        var counter = 1
        while (true) {
            val newName = "${nameWithoutExt}_$counter$extension"
            if (!fileExists(context, directoryUri, newName)) {
                return newName
            }
            counter++
        }
    }

    /**
     * 通过 DocumentFile 写入文件内容
     */
    fun writeFileContent(
        context: Context,
        directoryUri: Uri,
        fileName: String,
        sourceFile: File
    ): Uri? {
        val directory = DocumentFile.fromTreeUri(context, directoryUri) ?: return null

        // 检查是否已存在同名文件，若存在则删除
        directory.findFile(fileName)?.delete()

        // 创建新文件
        val documentFile = directory.createFile("*/*", fileName) ?: return null

        // 写入内容
        return try {
            context.contentResolver.openOutputStream(documentFile.uri)?.use { output ->
                sourceFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            }
            documentFile.uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 获取目录中文件的 URI
     */
    fun getFileUri(context: Context, directoryUri: Uri, fileName: String): Uri? {
        val directory = DocumentFile.fromTreeUri(context, directoryUri) ?: return null
        return directory.findFile(fileName)?.uri
    }

    /**
     * 删除目录中的文件
     */
    fun deleteFile(context: Context, directoryUri: Uri, fileName: String): Boolean {
        val directory = DocumentFile.fromTreeUri(context, directoryUri) ?: return false
        return directory.findFile(fileName)?.delete() ?: false
    }
}
