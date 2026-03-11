package wang.zengye.dsm.terminal

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import wang.zengye.dsm.MainActivity
import wang.zengye.dsm.R
import javax.inject.Inject

/**
 * 终端前台服务
 * 保持SSH连接在后台运行，防止被系统杀死
 * 参考 ConnectBot 的 TerminalManager 实现
 */
@AndroidEntryPoint
class TerminalService : Service() {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "terminal_service_channel"
        private const val NOTIFICATION_ID = 1001

        // 单例引用，供 UI 层访问
        var instance: TerminalService? = null
            private set
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val binder = TerminalBinder()

    // SSH 终端桥接器
    private var _bridge: SshTerminalBridge? = null
    val bridge: SshTerminalBridge? get() = _bridge

    // 连接状态
    private val _connectionState = MutableStateFlow<SshTerminalBridge.State>(SshTerminalBridge.State.Idle)
    val connectionState: StateFlow<SshTerminalBridge.State> = _connectionState.asStateFlow()

    // 上次成功连接的参数
    private var lastConnectionParams: ConnectionParams? = null
    private var userDisconnected = false

    private data class ConnectionParams(
        val host: String,
        val port: Int,
        val username: String,
        val password: String
    )

    inner class TerminalBinder : Binder() {
        fun getService(): TerminalService = this@TerminalService
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 启动前台服务
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        _bridge?.release()
        _bridge = null
        instance = null
        scope.cancel()
    }

    /**
     * 获取或创建 Bridge
     */
    fun getOrCreateBridge(): SshTerminalBridge {
        return _bridge ?: SshTerminalBridge().also {
            _bridge = it
            // 监听连接状态
            scope.launch {
                it.state.collect { state ->
                    _connectionState.value = state
                }
            }
        }
    }

    /**
     * 连接到 SSH 服务器
     */
    fun connect(host: String, port: Int, username: String, password: String) {
        userDisconnected = false
        lastConnectionParams = ConnectionParams(host, port, username, password)

        val currentBridge = getOrCreateBridge()
        scope.launch {
            currentBridge.connect(host, port, username, password)
        }
    }

    /**
     * 断开连接
     */
    fun disconnect() {
        userDisconnected = true
        lastConnectionParams = null
        _bridge?.disconnect()
    }

    /**
     * 尝试自动重连
     */
    fun tryAutoReconnect() {
        val params = lastConnectionParams ?: return
        if (userDisconnected) return
        val currentState = _connectionState.value
        if (currentState is SshTerminalBridge.State.Disconnected) {
            val currentBridge = _bridge ?: return
            scope.launch {
                currentBridge.connect(params.host, params.port, params.username, params.password)
            }
        }
    }

    /**
     * 更新通知内容
     */
    fun updateNotification(host: String, username: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(host, username))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.terminal_ssh_terminal),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.terminal_service_notification_desc)
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(host: String? = null, username: String? = null): Notification {
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, pendingIntentFlags)

        val contentText = if (host != null && username != null) {
            getString(R.string.terminal_connected_to, "$username@$host")
        } else {
            getString(R.string.terminal_service_running)
        }

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .build()
    }
}
