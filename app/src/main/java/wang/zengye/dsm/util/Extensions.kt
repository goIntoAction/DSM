package wang.zengye.dsm.util

import androidx.annotation.StringRes
import wang.zengye.dsm.DSMApplication
import wang.zengye.dsm.R
import java.text.DecimalFormat

/**
 * 全局字符串资源快捷函数
 */
fun appString(@StringRes resId: Int): String = DSMApplication.instance.getString(resId)
fun appString(@StringRes resId: Int, vararg args: Any): String = DSMApplication.instance.getString(resId, *args)

/**
 * 文件大小格式化
 */
fun formatSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB")
    val df = DecimalFormat("#.##")
    var size = bytes.toDouble()
    var unitIndex = 0
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    return "${df.format(size)} ${units[unitIndex]}"
}

/**
 * 时间格式化
 */
fun formatDuration(seconds: Int): String {
    if (seconds < 0) return "00:00"
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%02d:%02d", minutes, secs)
    }
}

/**
 * 格式化时间为 "yyyy-MM-dd HH:mm:ss"
 */
fun formatDateTime(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp * 1000))
}

/**
 * 格式化时间为 "yyyy-MM-dd"
 */
fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp * 1000))
}

/**
 * 相对时间（如"3天前"）
 */
fun timeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis() / 1000
    val diff = now - timestamp
    
    return when {
        diff < 60 -> appString(R.string.time_just_now)
        diff < 3600 -> appString(R.string.time_minutes_ago, diff / 60)
        diff < 86400 -> appString(R.string.time_hours_ago, diff / 3600)
        diff < 2592000 -> appString(R.string.time_days_ago, diff / 86400)
        diff < 31536000 -> appString(R.string.time_months_ago, diff / 2592000)
        else -> appString(R.string.time_years_ago, diff / 31536000)
    }
}

/**
 * String 是否为空白
 */
val String?.isBlank: Boolean
    get() = this == null || this.trim().isEmpty()

/**
 * String 是否不为空白
 */
val String?.isNotBlank: Boolean
    get() = !this.isBlank

/**
 * Base64 编码
 */
fun String.base64Encode(): String {
    return android.util.Base64.encodeToString(this.toByteArray(), android.util.Base64.NO_WRAP)
}

/**
 * Base64 解码
 */
fun String.base64Decode(): String {
    return String(android.util.Base64.decode(this, android.util.Base64.DEFAULT))
}

/**
 * Int 时间戳转 Date
 */
fun Int.toDate(): java.util.Date = java.util.Date(this.toLong() * 1000)

/**
 * Long 时间戳转 Date
 */
fun Long.toDate(): java.util.Date = java.util.Date(this * 1000)

/**
 * 格式化运行时间
 */
fun formatUptime(seconds: Long): String {
    if (seconds <= 0) return appString(R.string.time_zero_seconds)

    val days = seconds / 86400
    val hours = (seconds % 86400) / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    val parts = mutableListOf<String>()
    if (days > 0) parts.add(appString(R.string.time_days, days))
    if (hours > 0) parts.add(appString(R.string.time_hours, hours))
    if (minutes > 0) parts.add(appString(R.string.time_minutes, minutes))
    if (secs > 0 && parts.isEmpty()) parts.add(appString(R.string.time_seconds, secs))
    
    return parts.joinToString(" ")
}
