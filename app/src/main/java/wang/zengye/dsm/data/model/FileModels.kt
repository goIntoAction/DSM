package wang.zengye.dsm.data.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ============== File Moshi Models ==============

/**
 * 文件信息 (Moshi)
 */
@JsonClass(generateAdapter = true)
data class FileInfoDto(
    @Json(name = "name") val name: String = "",
    @Json(name = "path") val path: String = "",
    @Json(name = "isdir") val isdir: Boolean = false,
    @Json(name = "additional") val additional: FileAdditionalDto? = null,
    @Json(name = "filename") val filename: String? = null,  // 搜索结果使用 filename
    @Json(name = "hidden") val hidden: Boolean = false
)

@JsonClass(generateAdapter = true)
data class FileAdditionalDto(
    @Json(name = "size") val size: Long = 0,
    @Json(name = "owner") val owner: FileOwnerDto? = null,
    @Json(name = "time") val time: FileTimeDto? = null,
    @Json(name = "perm") val perm: FilePermDto? = null,
    @Json(name = "real_path") val realPath: String = "",
    @Json(name = "mount_point_type") val mountPointType: String = ""
)

@JsonClass(generateAdapter = true)
data class FileOwnerDto(
    @Json(name = "name") val name: String = "",
    @Json(name = "gid") val gid: Int = 0,
    @Json(name = "uid") val uid: Int = 0
)

@JsonClass(generateAdapter = true)
data class FileTimeDto(
    @Json(name = "mtime") val mTime: Long = 0,
    @Json(name = "ctime") val cTime: Long = 0,
    @Json(name = "crtime") val crTime: Long = 0,
    @Json(name = "atime") val aTime: Long = 0
)

@JsonClass(generateAdapter = true)
data class FilePermDto(
    @Json(name = "acl") val acl: FileAclDto? = null,
    @Json(name = "is_acl_mode") val isAclMode: Boolean = false,
    @Json(name = "posix") val posix: String = ""
)

@JsonClass(generateAdapter = true)
data class FileAclDto(
    @Json(name = "read") val read: Boolean = false,
    @Json(name = "write") val write: Boolean = false,
    @Json(name = "exec") val exec: Boolean = false,
    @Json(name = "del") val del: Boolean = false,
    @Json(name = "append") val append: Boolean = false
)

/**
 * 文件列表响应 (Moshi)
 * API: SYNO.FileStation.List
 */
@JsonClass(generateAdapter = true)
data class FileListDto(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "error") val error: ApiError? = null,
    @Json(name = "data") val data: FileListDataDto? = null
)

@JsonClass(generateAdapter = true)
data class FileListDataDto(
    @Json(name = "total") val total: Int = 0,
    @Json(name = "offset") val offset: Int = 0,
    @Json(name = "files") val files: List<FileInfoDto>? = null
)

/**
 * 共享文件夹列表响应 (Moshi)
 * API: SYNO.FileStation.List - list_share
 */
@JsonClass(generateAdapter = true)
data class ShareListDto(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "error") val error: ApiError? = null,
    @Json(name = "data") val data: ShareListDataDto? = null
)

@JsonClass(generateAdapter = true)
data class ShareListDataDto(
    @Json(name = "shares") val shares: List<FileInfoDto>? = null
)

/**
 * 文件操作任务响应 (Moshi)
 * 用于 delete, copy_move, compress, extract 等操作
 */
@JsonClass(generateAdapter = true)
data class FileTaskDto(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "error") val error: ApiError? = null,
    @Json(name = "data") val data: FileTaskDataDto? = null
)

@JsonClass(generateAdapter = true)
data class FileTaskDataDto(
    @Json(name = "taskid") val taskid: String = ""
)

/**
 * 文件任务状态响应 (Moshi)
 */
@JsonClass(generateAdapter = true)
data class FileTaskStatusDto(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "error") val error: ApiError? = null,
    @Json(name = "data") val data: FileTaskStatusDataDto? = null
)

@JsonClass(generateAdapter = true)
data class FileTaskStatusDataDto(
    @Json(name = "progress") val progress: Double = 0.0,
    @Json(name = "finished") val finished: Boolean = false,
    @Json(name = "failed") val failed: Boolean = false,
    @Json(name = "path") val path: String = ""
)

/**
 * 搜索开始响应 (Moshi)
 */
@JsonClass(generateAdapter = true)
data class SearchStartDto(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "error") val error: ApiError? = null,
    @Json(name = "data") val data: FileTaskDataDto? = null
)

/**
 * 搜索结果响应 (Moshi)
 */
@JsonClass(generateAdapter = true)
data class SearchListDto(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "error") val error: ApiError? = null,
    @Json(name = "data") val data: SearchListDataDto? = null
)

@JsonClass(generateAdapter = true)
data class SearchListDataDto(
    @Json(name = "status") val status: String = "",
    @Json(name = "total") val total: Int = 0,
    @Json(name = "items") val items: List<SearchItemDto>? = null
)

@JsonClass(generateAdapter = true)
data class SearchItemDto(
    @Json(name = "file") val file: FileInfoDto? = null
)

/**
 * 收藏夹项 (Moshi)
 */
@JsonClass(generateAdapter = true)
data class FavoriteItemDto(
    @Json(name = "name") val name: String = "",
    @Json(name = "path") val path: String = "",
    @Json(name = "status") val status: String = "valid"
)

/**
 * 收藏夹列表响应 (Moshi)
 * API: SYNO.FileStation.Favorite
 */
@JsonClass(generateAdapter = true)
data class FavoriteListDto(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "error") val error: ApiError? = null,
    @Json(name = "data") val data: FavoriteListDataDto? = null
)

@JsonClass(generateAdapter = true)
data class FavoriteListDataDto(
    @Json(name = "favorites") val favorites: List<FavoriteItemDto>? = null
)

/**
 * 收藏夹操作响应 (Moshi)
 */
@JsonClass(generateAdapter = true)
data class FavoriteOperationDto(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "error") val error: ApiError? = null
)

/**
 * 共享链接项 (Moshi)
 */
@JsonClass(generateAdapter = true)
data class ShareLinkItemDto(
    @Json(name = "id") val id: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "path") val path: String = "",
    @Json(name = "url") val url: String = "",
    @Json(name = "date_expired") val dateExpired: Long = 0,
    @Json(name = "date_created") val dateCreated: Long = 0,
    @Json(name = "is_valid") val isValid: Boolean = true
)

/**
 * 共享链接列表响应 (Moshi)
 * API: SYNO.FileStation.Sharing
 */
@JsonClass(generateAdapter = true)
data class ShareLinkListDto(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "error") val error: ApiError? = null,
    @Json(name = "data") val data: ShareLinkListDataDto? = null
)

@JsonClass(generateAdapter = true)
data class ShareLinkListDataDto(
    @Json(name = "links") val links: List<ShareLinkItemDto>? = null
)

/**
 * 创建共享链接响应 (Moshi)
 */
@JsonClass(generateAdapter = true)
data class ShareCreateDto(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "error") val error: ApiError? = null,
    @Json(name = "data") val data: ShareLinkListDataDto? = null
)

/**
 * 共享链接操作响应 (Moshi)
 */
@JsonClass(generateAdapter = true)
data class ShareOperationDto(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "error") val error: ApiError? = null
)

/**
 * 通用操作响应 (Moshi)
 */
@JsonClass(generateAdapter = true)
data class OperationDto(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "error") val error: ApiError? = null
)

/**
 * 文件类型枚举
 */
enum class FileType(val extension: Set<String>) {
    IMAGE(setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico", "tiff", "raw", "heic", "heif")),
    VIDEO(setOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "rmvb", "rm", "3gp", "ts", "m2ts", "mts")),
    AUDIO(setOf("mp3", "flac", "wav", "aac", "m4a", "ogg", "wma", "ape", "dts", "ac3")),
    DOCUMENT(setOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf", "odt", "ods", "odp")),
    ARCHIVE(setOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz", "tgz")),
    CODE(setOf("js", "ts", "kt", "java", "py", "cpp", "c", "h", "json", "xml", "html", "css", "md", "yaml", "yml")),
    APK(setOf("apk")),
    ISO(setOf("iso", "img", "bin", "mds", "nrg", "daa")),
    TORRENT(setOf("torrent")),
    UNKNOWN(emptySet());
    
    companion object {
        fun fromExtension(ext: String): FileType {
            val lowerExt = ext.lowercase()
            return entries.find { it.extension.contains(lowerExt) } ?: UNKNOWN
        }
        
        fun fromName(name: String): FileType {
            val ext = name.substringAfterLast(".", "")
            return if (ext.isNotEmpty()) fromExtension(ext) else UNKNOWN
        }
    }
    
    val isMedia: Boolean
        get() = this == IMAGE || this == VIDEO || this == AUDIO
    
    val isPreviewable: Boolean
        get() = this == IMAGE || this == VIDEO || this == AUDIO || this == DOCUMENT || this == CODE
}
