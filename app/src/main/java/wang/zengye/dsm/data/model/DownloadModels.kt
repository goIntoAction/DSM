package wang.zengye.dsm.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ============== 下载站 Moshi 模型 ==============

/**
 * 下载传输信息 (Moshi)
 */
@JsonClass(generateAdapter = true)
data class DownloadTransferDto(
    @Json(name = "size_downloaded") val sizeDownloaded: Long?,
    @Json(name = "size_uploaded") val sizeUploaded: Long?,
    @Json(name = "speed_download") val speedDownload: Long?,
    @Json(name = "speed_upload") val speedUpload: Long?,
    @Json(name = "progress") val progress: Double?,
    @Json(name = "downloaded_pieces") val downloadedPieces: Int?
)

/**
 * 下载任务详情信息 - 在任务列表的 additional.detail 中 (Moshi)
 */
@JsonClass(generateAdapter = true)
data class DownloadDetailInfoDto(
    @Json(name = "completed_time") val completedTime: Long?,
    @Json(name = "connected_leechers") val connectedLeechers: Int?,
    @Json(name = "connected_peers") val connectedPeers: Int?,
    @Json(name = "connected_seeders") val connectedSeeders: Int?,
    @Json(name = "created_time") val createdTime: Long?,
    @Json(name = "destination") val destination: String?,
    @Json(name = "extract_password") val extractPassword: String?,
    @Json(name = "seed_elapsed") val seedElapsed: Long?,
    @Json(name = "started_time") val startedTime: Long?,
    @Json(name = "total_peers") val totalPeers: Int?,
    @Json(name = "total_pieces") val totalPieces: Int?,
    @Json(name = "uri") val uri: String?,
    @Json(name = "waiting_seconds") val waitingSeconds: Int?
)

/**
 * 下载任务附加信息 (Moshi)
 */
@JsonClass(generateAdapter = true)
data class DownloadTaskAdditionalDto(
    @Json(name = "detail") val detail: DownloadDetailInfoDto?,
    @Json(name = "transfer") val transfer: DownloadTransferDto?
)

/**
 * 下载任务 (Moshi)
 */
@JsonClass(generateAdapter = true)
data class DownloadTaskDto(
    @Json(name = "id") val id: String?,
    @Json(name = "title") val title: String?,
    @Json(name = "size") val size: Long?,
    @Json(name = "status") val status: Int?,
    @Json(name = "type") val type: String?,
    @Json(name = "username") val username: String?,
    @Json(name = "additional") val additional: DownloadTaskAdditionalDto?
)

/**
 * 下载任务列表数据 (Moshi)
 */
@JsonClass(generateAdapter = true)
data class DownloadTaskListData(
    @Json(name = "offset") val offset: Int?,
    @Json(name = "task") val task: List<DownloadTaskDto>?,
    @Json(name = "total") val total: Int?
)

/**
 * 下载任务列表响应 (Moshi)
 * API: SYNO.DownloadStation2.Task
 * Method: list
 */
@JsonClass(generateAdapter = true)
data class DownloadTaskListDto(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "data") val data: DownloadTaskListData?
)

/**
 * BT 文件信息 (Moshi)
 */
@JsonClass(generateAdapter = true)
data class BtFileDto(
    @Json(name = "name") val name: String?,
    @Json(name = "size") val size: Long?,
    @Json(name = "priority") val priority: Int?,
    @Json(name = "selected") val selected: Boolean?
)

/**
 * BT 文件列表响应 (Moshi)
 * API: SYNO.DownloadStation.BTMedia
 * Method: list
 */
@JsonClass(generateAdapter = true)
data class BtFileListDto(
    @Json(name = "files") val files: List<BtFileDto>?
)

/**
 * Peer 信息 (Moshi)
 */
@JsonClass(generateAdapter = true)
data class BtPeerDto(
    @Json(name = "address") val address: String?,
    @Json(name = "client") val client: String?,
    @Json(name = "progress") val progress: Double?,
    @Json(name = "speed_download") val speedDownload: Long?,
    @Json(name = "speed_upload") val speedUpload: Long?,
    @Json(name = "status") val status: String?
)

/**
 * Peer 列表响应 (Moshi)
 * API: SYNO.DownloadStation.BTPeer
 * Method: list
 */
@JsonClass(generateAdapter = true)
data class BtPeerListDto(
    @Json(name = "peers") val peers: List<BtPeerDto>?
)

/**
 * Tracker 信息 (Moshi)
 */
@JsonClass(generateAdapter = true)
data class BtTrackerDto(
    @Json(name = "url") val url: String?,
    @Json(name = "status") val status: String?,
    @Json(name = "seeds") val seeds: Int?,
    @Json(name = "peers") val peers: Int?,
    @Json(name = "leechers") val leechers: Int?,
    @Json(name = "last_update") val lastUpdate: Int?
)

/**
 * Tracker 列表响应 (Moshi)
 * API: SYNO.DownloadStation.BTTracker
 * Method: list
 */
@JsonClass(generateAdapter = true)
data class BtTrackerListDto(
    @Json(name = "trackers") val trackers: List<BtTrackerDto>?
)

/**
 * 下载位置响应 (Moshi)
 * API: SYNO.DownloadStation.Info
 * Method: getconfig
 */
@JsonClass(generateAdapter = true)
data class DownloadLocationDto(
    @Json(name = "destination") val destination: String?
)

/**
 * 下载任务详情响应 (Moshi)
 * API: SYNO.DownloadStation.Task
 * Method: getinfo
 */
@JsonClass(generateAdapter = true)
data class DownloadTaskDetailDto(
    @Json(name = "id") val id: String?,
    @Json(name = "title") val title: String?,
    @Json(name = "size") val size: Long?,
    @Json(name = "status") val status: Int?,
    @Json(name = "statusVerbose") val statusVerbose: String?,
    @Json(name = "create_time") val createTime: Int?,
    @Json(name = "completed_time") val completedTime: Int?,
    @Json(name = "additional") val additional: DownloadTaskAdditionalDto?
)

// ============== 下载站相关模型 ==============

/**
 * 下载站任务附加信息
 */
data class DownloadTaskAdditional(
    val transfer: DownloadTransfer? = null
)

/**
 * 下载站任务传输信息
 */
data class DownloadTransfer(
    val sizeDownloaded: Long = 0,
    val sizeUploaded: Long = 0,
    val speedDownload: Long = 0,
    val speedUpload: Long = 0,
    val progress: Double = 0.0
) {
    val progressPercent: Int
        get() = progress.toInt()
}

/**
 * 下载站任务数据模型
 */
data class DownloadTask(
    val id: String = "",
    val title: String = "",
    val size: Long = 0,
    val status: Int = 0,
    val additional: DownloadTaskAdditional? = null
) {
    val isDownloading: Boolean
        get() = status == 1 || status == 2 || status == 3

    val isFinished: Boolean
        get() = status == 5

    val isPaused: Boolean
        get() = status == 4

    val isError: Boolean
        get() = status == 8 || status == 9 || status == 10

    val isToggleable: Boolean
        get() = isDownloading || isPaused || isFinished

    val progressPercent: Int
        get() = additional?.transfer?.progress?.toInt() ?: 0

    val sizeDownloaded: Long
        get() = additional?.transfer?.sizeDownloaded ?: 0

    val speedDownload: Long
        get() = additional?.transfer?.speedDownload ?: 0

    val statusText: String
        get() = when (status) {
            0 -> "等待中"
            1 -> "下载中"
            2 -> "暂停中"
            3 -> "完成中"
            4 -> "已暂停"
            5 -> "已完成"
            6 -> "检查中"
            7 -> "队列中"
            8 -> "错误"
            else -> "未知"
        }
}

// ============== Docker 相关模型 ==============

/**
 * Docker 容器网络设置
 */
data class ContainerNetwork(
    val ipAddress: String = "",
    val gateway: String = "",
    val macAddress: String = ""
)

/**
 * Docker 容器信息
 */
data class DockerContainer(
    val name: String = "",
    val status: String = "",
    val image: String = "",
    val created: Long = 0,
    val networkSettings: ContainerNetwork? = null
) {
    val isRunning: Boolean
        get() = status == "running"
}

// ============== 文件下载管理模型（Android DownloadManager - 已废弃） ==============

/**
 * 文件下载任务状态（用于 Android DownloadManager - 已废弃）
 */
enum class FileDownloadStatus {
    PENDING,      // 等待中
    RUNNING,      // 下载中
    PAUSED,       // 已暂停
    SUCCESS,      // 已完成
    FAILED        // 失败
}

// ============== App 内文件下载模型（新实现） ==============

/**
 * App 内下载任务状态
 */
enum class AppDownloadStatus {
    PENDING,       // 等待中
    RUNNING,       // 下载中
    PAUSED,        // 已暂停
    COMPLETED,     // 已完成
    FAILED,        // 失败
    CANCELLED      // 已取消
}

/**
 * App 内下载任务数据模型
 * 使用应用内下载，配合 SAF 存储访问框架
 */
data class AppDownloadTask(
    val id: String = "",                                          // 任务唯一ID
    val fileName: String,                                         // 文件名
    val remotePath: String,                                       // DSM 远程路径
    val downloadUrl: String,                                      // 下载 URL
    val directoryUri: String,                                     // 下载目录 URI (SAF)
    val totalBytes: Long = 0,                                     // 总大小
    val downloadedBytes: Long = 0,                                // 已下载大小
    val status: AppDownloadStatus = AppDownloadStatus.PENDING,   // 下载状态
    val createTime: Long = System.currentTimeMillis(),           // 创建时间
    val finishTime: Long? = null,                                 // 完成时间
    val errorMessage: String? = null                              // 错误信息
) {
    /**
     * 下载进度百分比 (0-100)
     */
    val progress: Int
        get() = if (totalBytes > 0) {
            ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 100)
        } else {
            0
        }

    /**
     * 格式化的已下载大小
     */
    val downloadedSizeFormatted: String
        get() = formatBytes(downloadedBytes)

    /**
     * 格式化的总大小
     */
    val totalSizeFormatted: String
        get() = formatBytes(totalBytes)

    /**
     * 是否可取消（下载中或等待中）
     */
    val isCancellable: Boolean
        get() = status == AppDownloadStatus.RUNNING || status == AppDownloadStatus.PENDING

    /**
     * 是否可重试（失败状态）
     */
    val isRetryable: Boolean
        get() = status == AppDownloadStatus.FAILED

    /**
     * 是否可打开（完成状态）
     */
    val isOpenable: Boolean
        get() = status == AppDownloadStatus.COMPLETED

    /**
     * 是否可删除（已完成或失败）
     */
    val isDeletable: Boolean
        get() = status == AppDownloadStatus.COMPLETED ||
                status == AppDownloadStatus.FAILED ||
                status == AppDownloadStatus.CANCELLED

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }

    companion object {
        /**
         * 创建新的下载任务
         */
        fun create(
            fileName: String,
            remotePath: String,
            downloadUrl: String,
            directoryUri: String
        ): AppDownloadTask {
            return AppDownloadTask(
                id = generateTaskId(),
                fileName = fileName,
                remotePath = remotePath,
                downloadUrl = downloadUrl,
                directoryUri = directoryUri
            )
        }

        private fun generateTaskId(): String {
            return "app_download_${System.currentTimeMillis()}_${(0..9999).random()}"
        }
    }
}

/**
 * App 下载任务 UI 状态
 */
data class AppDownloadUiState(
    val tasks: List<AppDownloadTask> = emptyList(),
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val currentDirectoryUri: String? = null,  // 当前选择的下载目录 URI
    val hasDirectoryPermission: Boolean = false  // 是否已有目录权限
) : wang.zengye.dsm.ui.base.BaseState

/**
 * 文件下载任务数据模型（用于 Android DownloadManager）
 * 注：简化版，不使用 Room 数据库持久化
 */
data class FileDownloadTask(
    val id: Long = 0,
    val downloadManagerId: Long,      // 系统 DownloadManager 的 ID
    val fileName: String,              // 文件名
    val filePath: String,              // DSM 文件路径
    val downloadUrl: String,           // 下载 URL
    val localPath: String,             // 本地保存路径
    val totalBytes: Long = 0,          // 总大小
    val downloadedBytes: Long = 0,     // 已下载大小
    val status: FileDownloadStatus = FileDownloadStatus.PENDING,
    val createTime: Long = System.currentTimeMillis(),
    val finishTime: Long? = null,
    val errorMessage: String? = null
) {
    /**
     * 下载进度百分比 (0-100)
     */
    val progress: Int
        get() = if (totalBytes > 0) {
            ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 100)
        } else {
            0
        }

    /**
     * 格式化的已下载大小
     */
    val downloadedSizeFormatted: String
        get() = formatBytes(downloadedBytes)

    /**
     * 格式化的总大小
     */
    val totalSizeFormatted: String
        get() = formatBytes(totalBytes)

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }
}