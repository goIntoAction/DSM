[根目录](../../../../../../../../../CLAUDE.md) > app > src > main > java > wang > zengye > dsm > **terminal**

---

# Terminal 模块

## 模块职责

Terminal 模块负责 SSH 终端功能，提供完整的终端模拟器和 SSH 连接管理。主要职责包括：
- SSH 连接管理
- 终端模拟器（VT100/xterm）
- 终端输入输出处理
- 前台服务管理
- 终端修饰键管理

## 入口与启动

### 核心类
- **TerminalService**: 前台服务，管理 SSH 连接生命周期
- **TerminalEmulator**: 终端模拟器，处理终端输入输出
- **SshTerminalBridge**: SSH 连接桥接，连接 SSH 会话和终端模拟器
- **TerminalModifierManager**: 终端修饰键管理（Ctrl、Alt、Shift 等）

### 启动流程
```kotlin
// 启动终端服务
val intent = Intent(context, TerminalService::class.java).apply {
    putExtra("host", host)
    putExtra("port", port)
    putExtra("username", username)
    putExtra("password", password)
}
context.startForegroundService(intent)

// 绑定服务
context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
```

## 对外接口

### TerminalService

前台服务，管理 SSH 连接：

```kotlin
class TerminalService : Service() {
    private var sshBridge: SshTerminalBridge? = null
    private var terminalEmulator: TerminalEmulator? = null

    fun connect(host: String, port: Int, username: String, password: String)
    fun disconnect()
    fun sendInput(data: ByteArray)
    fun resize(columns: Int, rows: Int)
}
```

### TerminalEmulator

终端模拟器，基于 ConnectBot termlib：

```kotlin
class TerminalEmulator(
    columns: Int,
    rows: Int,
    private val onOutput: (String) -> Unit
) {
    fun processInput(data: ByteArray)
    fun resize(columns: Int, rows: Int)
    fun getScreen(): Array<CharArray>
    fun getCursorPosition(): Pair<Int, Int>
}
```

### SshTerminalBridge

SSH 连接桥接：

```kotlin
class SshTerminalBridge(
    private val host: String,
    private val port: Int,
    private val username: String,
    private val password: String,
    private val terminalEmulator: TerminalEmulator
) {
    fun connect()
    fun disconnect()
    fun sendInput(data: ByteArray)
    fun resize(columns: Int, rows: Int)
}
```

### TerminalModifierManager

终端修饰键管理：

```kotlin
object TerminalModifierManager {
    var ctrlPressed: Boolean = false
    var altPressed: Boolean = false
    var shiftPressed: Boolean = false

    fun reset()
    fun handleKeyEvent(keyCode: Int, event: KeyEvent): ByteArray?
}
```

## 关键依赖与配置

### 依赖
- **ConnectBot sshlib**: 2.2.43 (SSH 连接)
- **ConnectBot termlib**: 0.0.18 (终端模拟器)

### 前台服务配置

在 `AndroidManifest.xml` 中声明：

```xml
<service
    android:name=".terminal.TerminalService"
    android:enabled="true"
    android:exported="false"
    android:foregroundServiceType="dataSync" />
```

### 权限要求
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
```

### 通知配置
前台服务需要显示通知：

```kotlin
private fun createNotification(): Notification {
    val channel = NotificationChannel(
        CHANNEL_ID,
        "SSH 终端",
        NotificationManager.IMPORTANCE_LOW
    )
    notificationManager.createNotificationChannel(channel)

    return NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("SSH 终端")
        .setContentText("已连接到 $host")
        .setSmallIcon(R.drawable.ic_notification)
        .build()
}
```

## 数据模型

### 终端状态
```kotlin
data class TerminalState(
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val error: String? = null,
    val output: String = "",
    val cursorPosition: Pair<Int, Int> = 0 to 0
)
```

### SSH 连接参数
```kotlin
data class SshConnectionParams(
    val host: String,
    val port: Int = 22,
    val username: String,
    val password: String,
    val columns: Int = 80,
    val rows: Int = 24
)
```

## 测试与质量

### 测试策略
- 测试 SSH 连接建立和断开
- 测试终端输入输出处理
- 测试终端大小调整
- 测试修饰键处理

### 注意事项
- SSH 连接需要在后台线程执行
- 终端输出需要在主线程更新 UI
- 服务需要正确处理生命周期（onCreate、onDestroy）

## 常见问题 (FAQ)

### Q: 如何连接 SSH 服务器？
```kotlin
val intent = Intent(context, TerminalService::class.java).apply {
    putExtra("host", "192.168.1.100")
    putExtra("port", 22)
    putExtra("username", "admin")
    putExtra("password", "password")
}
context.startForegroundService(intent)
```

### Q: 如何发送终端输入？
```kotlin
// 发送文本
val data = "ls -la\n".toByteArray()
terminalService.sendInput(data)

// 发送特殊键（如 Ctrl+C）
val ctrlC = byteArrayOf(0x03)
terminalService.sendInput(ctrlC)
```

### Q: 如何调整终端大小？
```kotlin
terminalService.resize(columns = 120, rows = 40)
```

### Q: 如何处理终端输出？
```kotlin
// 在 TerminalEmulator 中设置回调
val emulator = TerminalEmulator(
    columns = 80,
    rows = 24,
    onOutput = { output ->
        // 更新 UI
        updateTerminalOutput(output)
    }
)
```

### Q: 如何处理修饰键（Ctrl、Alt、Shift）？
```kotlin
// 按下 Ctrl 键
TerminalModifierManager.ctrlPressed = true

// 处理按键事件
val data = TerminalModifierManager.handleKeyEvent(keyCode, event)
if (data != null) {
    terminalService.sendInput(data)
}

// 释放修饰键
TerminalModifierManager.reset()
```

## 相关文件清单

### 核心文件
- `TerminalService.kt` - 前台服务
- `TerminalEmulator.kt` - 终端模拟器
- `SshTerminalBridge.kt` - SSH 连接桥接
- `TerminalModifierManager.kt` - 修饰键管理

---

## 变更记录 (Changelog)

### 2026-03-05 18:07:29
- 初始化模块文档
- 完成模块结构分析

---

**最后更新**: 2026-03-05 18:07:29
