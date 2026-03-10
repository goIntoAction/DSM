package wang.zengye.dsm.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import wang.zengye.dsm.MainActivity
import wang.zengye.dsm.R

/**
 * 本地通知管理器
 * 用于显示应用内的通知和提醒
 */
object NotificationHelper {

    private const val CHANNEL_ID = "dsm_notifications"
    const val REQUEST_CODE_NOTIFICATION = 1001

    private var channelName: String = appString(R.string.notification_channel_name)
    private var channelDescription: String = appString(R.string.notification_channel_description)

    private var notificationManager: NotificationManager? = null

    /**
     * 初始化通知渠道
     */
    fun init(context: Context) {
        channelName = context.getString(R.string.notification_channel_name)
        channelDescription = context.getString(R.string.notification_channel_description)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = channelDescription
                enableVibration(true)
                enableLights(true)
            }

            notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager?.createNotificationChannel(channel)
        } else {
            @Suppress("DEPRECATION")
            notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
    }

    /**
     * 检查是否有通知权限
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * 是否需要请求通知权限（Android 13+）
     */
    fun shouldRequestNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    /**
     * 显示通知
     */
    fun showNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = 0,
        icon: Int = android.R.drawable.ic_dialog_info
    ) {
        // Android 13+ 检查通知权限
        if (!hasNotificationPermission(context)) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager?.notify(notificationId, notification)
    }

    /**
     * 显示下载完成通知
     */
    fun showDownloadCompleteNotification(context: Context, fileName: String) {
        showNotification(
            context = context,
            title = context.getString(R.string.notification_download_complete),
            message = context.getString(R.string.notification_download_complete_message, fileName),
            notificationId = 1001,
            icon = android.R.drawable.stat_sys_download_done
        )
    }

    /**
     * 显示上传完成通知
     */
    fun showUploadCompleteNotification(context: Context, fileName: String) {
        showNotification(
            context = context,
            title = context.getString(R.string.notification_upload_complete),
            message = context.getString(R.string.notification_upload_complete_message, fileName),
            notificationId = 1002,
            icon = android.R.drawable.stat_sys_upload_done
        )
    }

    /**
     * 显示错误通知
     */
    fun showErrorNotification(context: Context, errorMessage: String) {
        showNotification(
            context = context,
            title = context.getString(R.string.notification_operation_failed),
            message = errorMessage,
            notificationId = 1003,
            icon = android.R.drawable.ic_dialog_alert
        )
    }

    /**
     * 显示成功通知
     */
    fun showSuccessNotification(context: Context, message: String) {
        showNotification(
            context = context,
            title = context.getString(R.string.notification_operation_success),
            message = message,
            notificationId = 1004,
            icon = android.R.drawable.ic_dialog_info
        )
    }

    /**
     * 取消通知
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager?.cancel(notificationId)
    }

    /**
     * 取消所有通知
     */
    fun cancelAllNotifications() {
        notificationManager?.cancelAll()
    }
}
