package wang.zengye.dsm.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * 下载任务数据库实体
 */
@Entity(tableName = "download_tasks")
data class DownloadTaskEntity(
    @PrimaryKey
    val id: String,
    val fileName: String,
    val remotePath: String,
    val downloadUrl: String,
    val directoryUri: String,
    val totalBytes: Long = 0,
    val downloadedBytes: Long = 0,
    val status: String,  // 存储枚举名称
    val createTime: Long = System.currentTimeMillis(),
    val finishTime: Long? = null,
    val errorMessage: String? = null
)
