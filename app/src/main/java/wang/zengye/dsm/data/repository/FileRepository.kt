package wang.zengye.dsm.data.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wang.zengye.dsm.data.api.DsmApiHelper
import wang.zengye.dsm.data.api.FileApiRetrofit
import wang.zengye.dsm.data.model.*
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 文件管理 Repository
 * 使用 Retrofit + Moshi 进行 API 调用
 */
@Singleton
class FileRepository @Inject constructor(
    private val fileApi: FileApiRetrofit
) : BaseRepository() {

    // ==================== 文件列表 ====================

    suspend fun getShareList(): Result<ShareListDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.getShareList()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getFileList(
        folderPath: String,
        offset: Int = 0,
        limit: Int = 100,
        sortBy: String = "name",
        sortDirection: String = "asc"
    ): Result<FileListDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.getFileList(
                    folderPath = folderPath,
                    offset = offset,
                    limit = limit,
                    sortBy = sortBy,
                    sortDirection = sortDirection
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getFileInfo(path: String): Result<FileListDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.getFileInfo(path = path)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 搜索 ====================

    suspend fun searchStart(
        folderPath: String,
        pattern: String,
        recursive: Boolean = true
    ): Result<SearchStartDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.searchStart(
                    folderPath = folderPath,
                    pattern = pattern,
                    recursive = recursive
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun searchList(
        taskId: String,
        offset: Int = 0,
        limit: Int = 100
    ): Result<SearchListDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.searchList(
                    taskId = taskId,
                    offset = offset,
                    limit = limit
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 文件操作 ====================

    suspend fun createFolder(
        folderPath: String,
        name: String
    ): Result<FileListDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.createFolder(
                    folderPath = folderPath,
                    name = name
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun rename(
        path: String,
        name: String
    ): Result<FileListDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.rename(path = path, name = name)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 删除 ====================

    suspend fun deleteStart(
        paths: List<String>
    ): Result<FileTaskDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.deleteStart(
                    path = paths.joinToString(",", "[", "]")
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteStatus(
        taskId: String
    ): Result<FileTaskStatusDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.deleteStatus(taskId = taskId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 复制/移动 ====================

    suspend fun copyMoveStart(
        paths: List<String>,
        destFolderPath: String,
        overwrite: Boolean = true,
        removeSrc: Boolean = false
    ): Result<FileTaskDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.copyMoveStart(
                    path = paths.joinToString(",", "[", "]"),
                    destFolderPath = destFolderPath,
                    overwrite = overwrite,
                    removeSrc = removeSrc
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun copyMoveStatus(
        taskId: String
    ): Result<FileTaskStatusDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.copyMoveStatus(taskId = taskId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 压缩 ====================

    suspend fun compressStart(
        paths: List<String>,
        destFilePath: String,
        level: String = "normal",
        password: String? = null,
        mode: String = "replace",
        format: String = "zip"
    ): Result<FileTaskDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.compressStart(
                    path = paths.joinToString(",", "[", "]"),
                    destFilePath = destFilePath,
                    level = level,
                    password = password,
                    mode = mode,
                    format = format
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun compressStatus(
        taskId: String
    ): Result<FileTaskStatusDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.compressStatus(taskId = taskId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 解压 ====================

    suspend fun extractStart(
        filePath: String,
        destFolderPath: String,
        password: String? = null,
        overwrite: Boolean = false,
        keepDir: Boolean = true,
        createSubfolder: Boolean = false
    ): Result<FileTaskDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.extractStart(
                    filePath = filePath,
                    destFolderPath = destFolderPath,
                    password = password,
                    overwrite = overwrite,
                    keepDir = keepDir,
                    createSubfolder = createSubfolder
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun extractStatus(
        taskId: String
    ): Result<FileTaskStatusDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.extractStatus(taskId = taskId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 收藏夹 ====================

    suspend fun favoriteList(): Result<FavoriteListDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.favoriteList()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun favoriteAdd(
        name: String,
        path: String
    ): Result<FavoriteOperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.favoriteAdd(name = name, path = path)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun favoriteEdit(
        path: String,
        name: String
    ): Result<FavoriteOperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.favoriteEdit(path = path, name = name)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun favoriteDelete(
        path: String
    ): Result<FavoriteOperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.favoriteDelete(path = path)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 共享链接 ====================

    suspend fun shareCreate(
        paths: List<String>,
        dateExpired: Long? = null
    ): Result<ShareCreateDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.shareCreate(
                    path = paths.joinToString(",", "[", "]"),
                    dateExpired = dateExpired
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun shareList(): Result<ShareLinkListDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.shareList()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun shareDelete(
        id: String
    ): Result<ShareOperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.shareDelete(id = "[\"$id\"]")
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 远程文件夹 ====================

    suspend fun getSmbFolders(): Result<FileListDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.getSmbFolders()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== URL 生成 ====================

    fun getDownloadUrl(path: String): String {
        return wang.zengye.dsm.data.api.DsmApiHelper.getDownloadUrl(path)
    }

    fun getThumbnailUrl(path: String, size: String = "large"): String {
        return wang.zengye.dsm.data.api.DsmApiHelper.getDownloadUrl(path)
    }

    // ==================== 远程文件夹 ====================

    /**
     * 断开远程连接
     */
    suspend fun disconnectRemote(connectionId: String): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.disconnectRemote(connectionId = connectionId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 挂载远程文件夹（SMB/CIFS）
     */
    suspend fun mountRemoteFolder(
        serverIp: String,
        mountPoint: String,
        account: String = "",
        password: String = ""
    ): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.mountRemoteFolder(
                    serverIp = serverIp,
                    mountPoint = mountPoint,
                    account = account,
                    password = password
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 卸载远程文件夹
     */
    suspend fun unmountRemoteFolder(mountPoint: String): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileApi.unmountRemoteFolder(mountPoint = mountPoint)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 上传 ====================

    /**
     * 上传文件（带取消和进度回调）
     * 使用 OkHttp 直接调用，支持进度追踪和取消
     */
    suspend fun uploadFile(
        context: Context,
        uri: Uri,
        destFolderPath: String,
        isCancelled: (() -> Boolean)? = null,
        onProgress: ((uploaded: Long, total: Long) -> Unit)? = null
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // 获取文件信息
                val contentResolver = context.contentResolver
                val fileName = getFileName(context, uri)
                android.util.Log.d("FileRepository", "uploadFile: fileName=$fileName, destFolderPath=$destFolderPath")

                // 将 Uri 内容复制到临时文件
                val tempFile = File(context.cacheDir, fileName)
                contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: return@withContext Result.failure(Exception("Cannot open file"))

                val sid = DsmApiHelper.getSessionId()
                val baseUrl = DsmApiHelper.baseUrl
                
                android.util.Log.d("FileRepository", "uploadFile: baseUrl=$baseUrl, sid=$sid")

                // 构建上传 URL - 参数作为 query 参数
                val params = mutableMapOf(
                    "api" to "SYNO.FileStation.Upload",
                    "version" to "2",
                    "method" to "upload"
                )
                if (sid.isNotEmpty()) {
                    params["_sid"] = sid
                }
                
                val urlBuilder = StringBuilder("$baseUrl/webapi/entry.cgi")
                urlBuilder.append("?")
                params.entries.forEachIndexed { index, entry ->
                    if (index > 0) urlBuilder.append("&")
                    urlBuilder.append("${entry.key}=${java.net.URLEncoder.encode(entry.value, "UTF-8")}")
                }
                
                val uploadUrl = urlBuilder.toString()
                android.util.Log.d("FileRepository", "uploadFile: uploadUrl=$uploadUrl")

                val fileSize = tempFile.length()
                val fileTime = tempFile.lastModified()
                android.util.Log.d("FileRepository", "uploadFile: fileSize=$fileSize, fileTime=$fileTime")

                // 创建进度追踪的 RequestBody
                val progressRequestBody = wang.zengye.dsm.data.api.ProgressRequestBody(
                    file = tempFile,
                    progressCallback = onProgress,
                    isCancelled = isCancelled
                )

                // 构建 multipart body - formData 参数
                val multipartBody = okhttp3.MultipartBody.Builder()
                    .setType(okhttp3.MultipartBody.FORM)
                    .addFormDataPart("path", destFolderPath)
                    .addFormDataPart("create_parents", "true")
                    .addFormDataPart("size", fileSize.toString())
                    .addFormDataPart("mtime", fileTime.toString())
                    .addFormDataPart("overwrite", "true")
                    .addFormDataPart("file", fileName, progressRequestBody)
                    .build()

                android.util.Log.d("FileRepository", "uploadFile: multipart body size=${multipartBody.contentLength()}")

                // 构建请求 - uploadHttpClient 的拦截器会自动添加 Cookie、X-SYNO-TOKEN、Origin、Referer 等头部
                val request = okhttp3.Request.Builder()
                    .url(uploadUrl)
                    .post(multipartBody)
                    .build()

                android.util.Log.d("FileRepository", "uploadFile: Sending request...")
                
                // 使用 uploadHttpClient（更长超时，拦截器会自动添加认证头部）
                val response = DsmApiHelper.uploadHttpClient.newCall(request).execute()

                android.util.Log.d("FileRepository", "uploadFile: Response code=${response.code}, message=${response.message}")
                android.util.Log.d("FileRepository", "uploadFile: Response headers: ${response.headers}")
                
                // 删除临时文件
                tempFile.delete()

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "No error body"
                    android.util.Log.e("FileRepository", "uploadFile: HTTP failed, body=$errorBody")
                    return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
                }

                val body = response.body?.string()
                android.util.Log.d("FileRepository", "uploadFile: Response body length=${body?.length}")
                
                if (body.isNullOrEmpty()) {
                    android.util.Log.e("FileRepository", "uploadFile: Empty response body")
                    return@withContext Result.failure(Exception("Empty response"))
                }

                android.util.Log.d("FileRepository", "uploadFile: Response body=$body")
                
                val json = org.json.JSONObject(body)
                if (json.optBoolean("success", false)) {
                    android.util.Log.d("FileRepository", "uploadFile: SUCCESS")
                    Result.success(Unit)
                } else {
                    val error = json.optJSONObject("error")
                    val code = error?.optInt("code", -1) ?: -1
                    val msg = error?.optString("msg") ?: error?.optString("message") ?: DsmApiHelper.getApiErrorMessage(code)
                    android.util.Log.e("FileRepository", "uploadFile: API error code=$code, msg=$msg, full json=$json")
                    Result.failure(Exception(msg))
                }
            } catch (e: Exception) {
                android.util.Log.e("FileRepository", "uploadFile: Exception", e)
                if (e is java.io.IOException && e.message?.contains("cancelled", ignoreCase = true) == true) {
                    Result.failure(Exception("Upload cancelled"))
                } else {
                    Result.failure(e)
                }
            }
        }
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
}