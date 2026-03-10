package wang.zengye.dsm.data.model

/**
 * 套件信息数据类
 * 用于 UI 层显示
 */
data class PackageInfo(
    val id: String,
    val name: String,
    val displayName: String,
    val version: String,
    val description: String,
    val status: String,
    val url: String,
    val launchable: Boolean,
    val installed: Boolean,
    val thumbnailUrl: String
) {
    /**
     * 是否正在运行
     */
    val isRunning: Boolean
        get() = status == "running"

    /**
     * 是否已停止
     */
    val isStopped: Boolean
        get() = status == "stopped"

    /**
     * 状态文本（用于 UI 显示）
     */
    val statusText: String
        get() = when (status) {
            "running" -> "运行中"
            "stopped" -> "已停止"
            else -> status
        }
}
