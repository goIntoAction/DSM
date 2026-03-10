package wang.zengye.dsm.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import wang.zengye.dsm.data.model.*

/**
 * 使用 Retrofit + Moshi 的文件管理 API 接口
 */
interface FileApiRetrofit {

    // ==================== 文件列表 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getShareList(
        @Field("api") api: String = "SYNO.FileStation.List",
        @Field("version") version: String = "2",
        @Field("method") method: String = "list_share",
        @Field("additional") additional: String = "[\"perm\",\"time\",\"size\",\"real_path\",\"mount_point_type\"]"
    ): Response<ShareListDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getFileList(
        @Field("api") api: String = "SYNO.FileStation.List",
        @Field("version") version: String = "2",
        @Field("method") method: String = "list",
        @Field("folder_path") folderPath: String,
        @Field("offset") offset: Int = 0,
        @Field("limit") limit: Int = 5000,
        @Field("sort_by") sortBy: String = "name",
        @Field("sort_direction") sortDirection: String = "asc",
        @Field("filetype") filetype: String = "all",
        @Field("additional") additional: String = "[\"perm\",\"time\",\"size\",\"mount_point_type\",\"real_path\"]"
    ): Response<FileListDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getFileInfo(
        @Field("api") api: String = "SYNO.FileStation.List",
        @Field("version") version: String = "2",
        @Field("method") method: String = "getinfo",
        @Field("path") path: String,
        @Field("additional") additional: String = "[\"real_path\",\"owner\",\"time\",\"perm\",\"size\"]"
    ): Response<FileListDto>

    // ==================== 搜索 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun searchStart(
        @Field("api") api: String = "SYNO.FileStation.Search",
        @Field("version") version: String = "2",
        @Field("method") method: String = "start",
        @Field("folder_path") folderPath: String,
        @Field("pattern") pattern: String,
        @Field("recursive") recursive: Boolean = true
    ): Response<SearchStartDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun searchList(
        @Field("api") api: String = "SYNO.FileStation.Search",
        @Field("version") version: String = "2",
        @Field("method") method: String = "list",
        @Field("taskid") taskId: String,
        @Field("offset") offset: Int = 0,
        @Field("limit") limit: Int = 5000,
        @Field("additional") additional: String = "[\"real_path\",\"size\",\"owner\",\"time\",\"perm\",\"type\"]"
    ): Response<SearchListDto>

    // ==================== 文件操作 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun createFolder(
        @Field("api") api: String = "SYNO.FileStation.CreateFolder",
        @Field("version") version: String = "2",
        @Field("method") method: String = "create",
        @Field("folder_path") folderPath: String,
        @Field("name") name: String
    ): Response<FileListDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun rename(
        @Field("api") api: String = "SYNO.FileStation.Rename",
        @Field("version") version: String = "2",
        @Field("method") method: String = "rename",
        @Field("path") path: String,
        @Field("name") name: String
    ): Response<FileListDto>

    // ==================== 删除 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun deleteStart(
        @Field("api") api: String = "SYNO.FileStation.Delete",
        @Field("version") version: String = "2",
        @Field("method") method: String = "start",
        @Field("path") path: String,
        @Field("accurate_progress") accurateProgress: Boolean = true
    ): Response<FileTaskDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun deleteStatus(
        @Field("api") api: String = "SYNO.FileStation.Delete",
        @Field("version") version: String = "2",
        @Field("method") method: String = "status",
        @Field("taskid") taskId: String
    ): Response<FileTaskStatusDto>

    // ==================== 复制/移动 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun copyMoveStart(
        @Field("api") api: String = "SYNO.FileStation.CopyMove",
        @Field("version") version: String = "3",
        @Field("method") method: String = "start",
        @Field("path") path: String,
        @Field("dest_folder_path") destFolderPath: String,
        @Field("overwrite") overwrite: Boolean = true,
        @Field("remove_src") removeSrc: Boolean = false,
        @Field("accurate_progress") accurateProgress: Boolean = true
    ): Response<FileTaskDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun copyMoveStatus(
        @Field("api") api: String = "SYNO.FileStation.CopyMove",
        @Field("version") version: String = "3",
        @Field("method") method: String = "status",
        @Field("taskid") taskId: String
    ): Response<FileTaskStatusDto>

    // ==================== 压缩 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun compressStart(
        @Field("api") api: String = "SYNO.FileStation.Compress",
        @Field("version") version: String = "3",
        @Field("method") method: String = "start",
        @Field("path") path: String,
        @Field("dest_file_path") destFilePath: String,
        @Field("level") level: String = "normal",
        @Field("format") format: String = "zip",
        @Field("mode") mode: String = "replace",
        @Field("password") password: String? = null
    ): Response<FileTaskDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun compressStatus(
        @Field("api") api: String = "SYNO.FileStation.Compress",
        @Field("version") version: String = "3",
        @Field("method") method: String = "status",
        @Field("taskid") taskId: String
    ): Response<FileTaskStatusDto>

    // ==================== 解压 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun extractStart(
        @Field("api") api: String = "SYNO.FileStation.Extract",
        @Field("version") version: String = "2",
        @Field("method") method: String = "start",
        @Field("file_path") filePath: String,
        @Field("dest_folder_path") destFolderPath: String,
        @Field("overwrite") overwrite: Boolean = false,
        @Field("keep_dir") keepDir: Boolean = true,
        @Field("create_subfolder") createSubfolder: Boolean = false,
        @Field("password") password: String? = null
    ): Response<FileTaskDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun extractStatus(
        @Field("api") api: String = "SYNO.FileStation.Extract",
        @Field("version") version: String = "2",
        @Field("method") method: String = "status",
        @Field("taskid") taskId: String
    ): Response<FileTaskStatusDto>

    // ==================== 收藏夹 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun favoriteList(
        @Field("api") api: String = "SYNO.FileStation.Favorite",
        @Field("version") version: String = "2",
        @Field("method") method: String = "list",
        @Field("offset") offset: Int = 0,
        @Field("limit") limit: Int = 1000,
        @Field("additional") additional: String = "[\"perm\",\"time\",\"size\",\"real_path\"]"
    ): Response<FavoriteListDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun favoriteAdd(
        @Field("api") api: String = "SYNO.FileStation.Favorite",
        @Field("version") version: String = "2",
        @Field("method") method: String = "add",
        @Field("name") name: String,
        @Field("path") path: String,
        @Field("index") index: Int = -1
    ): Response<FavoriteOperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun favoriteEdit(
        @Field("api") api: String = "SYNO.FileStation.Favorite",
        @Field("version") version: String = "2",
        @Field("method") method: String = "edit",
        @Field("path") path: String,
        @Field("name") name: String,
        @Field("index") index: Int = -1
    ): Response<FavoriteOperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun favoriteDelete(
        @Field("api") api: String = "SYNO.FileStation.Favorite",
        @Field("version") version: String = "2",
        @Field("method") method: String = "delete",
        @Field("path") path: String
    ): Response<FavoriteOperationDto>

    // ==================== 共享链接 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun shareCreate(
        @Field("api") api: String = "SYNO.FileStation.Sharing",
        @Field("version") version: String = "3",
        @Field("method") method: String = "create",
        @Field("path") path: String,
        @Field("file_request") fileRequest: Boolean = false,
        @Field("date_expired") dateExpired: Long? = null
    ): Response<ShareCreateDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun shareList(
        @Field("api") api: String = "SYNO.FileStation.Sharing",
        @Field("version") version: String = "3",
        @Field("method") method: String = "list",
        @Field("offset") offset: Int = 0,
        @Field("limit") limit: Int = 100
    ): Response<ShareLinkListDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun shareDelete(
        @Field("api") api: String = "SYNO.FileStation.Sharing",
        @Field("version") version: String = "3",
        @Field("method") method: String = "delete",
        @Field("id") id: String
    ): Response<ShareOperationDto>

    // ==================== 目录大小 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun startDirSizeTask(
        @Field("api") api: String = "SYNO.FileStation.DirSize",
        @Field("version") version: String = "2",
        @Field("method") method: String = "start",
        @Field("path") path: String
    ): Response<FileTaskDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getDirSizeStatus(
        @Field("api") api: String = "SYNO.FileStation.DirSize",
        @Field("version") version: String = "2",
        @Field("method") method: String = "status",
        @Field("taskid") taskId: String
    ): Response<DirSizeStatusDto>

    // ==================== 后台任务 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getBackgroundTasks(
        @Field("api") api: String = "SYNO.FileStation.BackgroundTask",
        @Field("version") version: String = "3",
        @Field("method") method: String = "list",
        @Field("offset") offset: Int = 0,
        @Field("limit") limit: Int = 100,
        @Field("sort_direction") sortDirection: String = "desc",
        @Field("additional") additional: String = "[\"status_percent\"]"
    ): Response<BackgroundTaskListDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun cancelBackgroundTask(
        @Field("api") api: String = "SYNO.FileStation.BackgroundTask",
        @Field("version") version: String = "3",
        @Field("method") method: String = "cancel",
        @Field("taskid") taskId: String
    ): Response<OperationDto>

    // ==================== 回收站 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun clearRecycleBin(
        @Field("api") api: String = "SYNO.FileStation.RecycleBin",
        @Field("version") version: String = "2",
        @Field("method") method: String = "clear",
        @Field("path") path: String
    ): Response<OperationDto>

    // ==================== 远程文件夹 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getSmbFolders(
        @Field("api") api: String = "SYNO.FileStation.VirtualFolder",
        @Field("version") version: String = "2",
        @Field("method") method: String = "list",
        @Field("node") node: String = "fm_rf_root",
        @Field("type") type: String = "[\"cifs\",\"nfs\"]",
        @Field("additional") additional: String = "[\"real_path\",\"owner\",\"time\",\"perm\",\"mount_point_type\",\"volume_status\"]"
    ): Response<FileListDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun disconnectRemote(
        @Field("api") api: String = "SYNO.FileStation.VFS.Connection",
        @Field("version") version: String = "1",
        @Field("method") method: String = "delete",
        @Field("id") connectionId: String
    ): Response<OperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun mountRemoteFolder(
        @Field("api") api: String = "SYNO.FileStation.Mount",
        @Field("version") version: String = "1",
        @Field("method") method: String = "mount_remote",
        @Field("mount_type") mountType: String = "CIFS",
        @Field("server_ip") serverIp: String,
        @Field("mount_point") mountPoint: String,
        @Field("account") account: String = "",
        @Field("passwd") password: String = "",
        @Field("user_set") userSet: Boolean = false,
        @Field("auto_mount") autoMount: Boolean = true
    ): Response<OperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun unmountRemoteFolder(
        @Field("api") api: String = "SYNO.FileStation.Mount",
        @Field("version") version: String = "1",
        @Field("method") method: String = "unmount",
        @Field("mount_point") mountPoint: String,
        @Field("is_mount_point") isMountPoint: Boolean = true,
        @Field("mount_type") mountType: String = "remote"
    ): Response<OperationDto>
}

// ==================== 额外的响应模型 ====================

@JsonClass(generateAdapter = true)
data class DirSizeStatusDto(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "error") val error: ApiError? = null,
    @Json(name = "data") val data: DirSizeDataDto? = null
)

@JsonClass(generateAdapter = true)
data class DirSizeDataDto(
    @Json(name = "dir_path") val dirPath: String = "",
    @Json(name = "size") val size: Long = 0,
    @Json(name = "num_dir") val numDir: Int = 0,
    @Json(name = "num_file") val numFile: Int = 0,
    @Json(name = "finish") val finish: Boolean = false
)

@JsonClass(generateAdapter = true)
data class BackgroundTaskListDto(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "error") val error: ApiError? = null,
    @Json(name = "data") val data: BackgroundTaskListDataDto? = null
)

@JsonClass(generateAdapter = true)
data class BackgroundTaskListDataDto(
    @Json(name = "tasks") val tasks: List<BackgroundTaskMoshi>? = null
)

@JsonClass(generateAdapter = true)
data class BackgroundTaskMoshi(
    @Json(name = "taskid") val taskId: String = "",
    @Json(name = "api") val api: String = "",
    @Json(name = "method") val method: String = "",
    @Json(name = "status") val status: String = "",
    @Json(name = "additional") val additional: BackgroundTaskAdditionalDto? = null
)

@JsonClass(generateAdapter = true)
data class BackgroundTaskAdditionalDto(
    @Json(name = "status_percent") val statusPercent: Double = 0.0
)
