[根目录](../../../../../../../../../CLAUDE.md) > app > src > main > java > wang > zengye > dsm > **util**

---

# Util 模块

## 模块职责

Util 模块提供应用的工具类和扩展函数，负责通用功能的封装。主要职责包括：
- 扩展函数（Kotlin Extensions）
- 设置管理（DataStore）
- 生物识别认证
- 手势密码管理
- 下载目录管理
- 通知管理

## 入口与启动

### 核心类
- **SettingsManager**: 单例对象，管理应用设置（DataStore）
- **BiometricHelper**: 生物识别认证帮助类
- **GesturePasswordManager**: 手势密码管理
- **DownloadDirectoryManager**: 下载目录管理
- **NotificationHelper**: 通知管理

### 初始化流程
```kotlin
// 在 DSMApplication.onCreate() 中初始化
SettingsManager.init(this)
```

## 对外接口

### SettingsManager

管理应用设置，使用 DataStore 持久化存储：

```kotlin
object SettingsManager {
    // 会话信息
    val sid: MutableStateFlow<String>
    val cookie: MutableStateFlow<String>
    val synoToken: MutableStateFlow<String>
    val host: Flow<String>
    val account: Flow<String>

    // 用户设置
    val launchAuth: Flow<Boolean>          // 启动认证
    val biometricAuth: Flow<Boolean>       // 生物识别认证
    val gesturePassword: Flow<String>      // 手势密码
    val downloadDirectory: Flow<String>    // 下载目录
    val autoDownload: Flow<Boolean>        // 自动下载
    val notificationEnabled: Flow<Boolean> // 通知开关

    // 方法
    suspend fun saveSession(sid: String, cookie: String, host: String, account: String)
    suspend fun clearSession()
    suspend fun getPassword(): String
    suspend fun savePassword(password: String)
    suspend fun migrateIfNeeded()
}
```

### BiometricHelper

生物识别认证帮助类：

```kotlin
object BiometricHelper {
    enum class BiometricStatus {
        AVAILABLE,           // 可用
        NOT_AVAILABLE,       // 不可用
        NO_HARDWARE,         // 无硬件
        NO_ENROLLED          // 未注册
    }

    fun isBiometricAvailable(context: Context): BiometricStatus

    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        negativeButtonText: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    )
}
```

### GesturePasswordManager

手势密码管理：

```kotlin
object GesturePasswordManager {
    suspend fun saveGesturePassword(password: String)
    suspend fun getGesturePassword(): String
    suspend fun clearGesturePassword()
    suspend fun verifyGesturePassword(password: String): Boolean
}
```

### DownloadDirectoryManager

下载目录管理（SAF - Storage Access Framework）：

```kotlin
object DownloadDirectoryManager {
    suspend fun getDownloadDirectory(): String
    suspend fun setDownloadDirectory(uri: Uri)
    suspend fun clearDownloadDirectory()
    fun hasDownloadDirectory(): Boolean
}
```

### NotificationHelper

通知管理：

```kotlin
object NotificationHelper {
    fun createNotificationChannel(context: Context)
    fun showNotification(context: Context, title: String, message: String)
    fun cancelNotification(context: Context, notificationId: Int)
}
```

### Extensions.kt

Kotlin 扩展函数：

```kotlin
// Context 扩展
val Context.appString: (Int) -> String

// String 扩展
fun String.toMD5(): String
fun String.toSHA256(): String
fun String.urlEncode(): String
fun String.urlDecode(): String

// Long 扩展
fun Long.formatFileSize(): String
fun Long.formatDuration(): String
fun Long.formatTimestamp(): String

// Flow 扩展
fun <T> Flow<T>.throttleFirst(windowDuration: Long): Flow<T>
```

## 关键依赖与配置

### 依赖
- **DataStore Preferences**: 1.2.0
- **Biometric**: 1.1.0
- **Security Crypto**: 1.1.0

### DataStore 配置
```kotlin
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "dsm_settings"
)
```

### 加密存储
使用 `EncryptedSharedPreferences` 存储敏感信息（如密码）：
```kotlin
private val encryptedPrefs = EncryptedSharedPreferences.create(
    context,
    "dsm_secure_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

## 数据模型

### 设置项
- **会话信息**: sid, cookie, synoToken, host, account
- **认证设置**: launchAuth, biometricAuth, gesturePassword
- **下载设置**: downloadDirectory, autoDownload
- **通知设置**: notificationEnabled

### 数据迁移
`SettingsManager.migrateIfNeeded()` 负责从旧版本迁移数据。

## 测试与质量

### 单元测试
- `util/ExtensionsTest.kt`: 扩展函数测试

### 测试策略
- 测试扩展函数的正确性
- 测试 DataStore 读写
- 测试加密存储

## 常见问题 (FAQ)

### Q: 如何添加新的设置项？
1. 在 `SettingsManager` 中定义 DataStore Key
2. 添加 Flow 属性和读写方法
3. 在 UI 中使用 `collectAsStateWithLifecycle()` 收集状态

### Q: 如何使用生物识别认证？
```kotlin
BiometricHelper.showBiometricPrompt(
    activity = this,
    title = "验证身份",
    subtitle = "使用生物识别验证",
    negativeButtonText = "取消",
    onSuccess = { /* 验证成功 */ },
    onError = { error -> /* 验证失败 */ },
    onCancel = { /* 用户取消 */ }
)
```

### Q: 如何使用扩展函数？
```kotlin
// 格式化文件大小
val size = 1024L.formatFileSize() // "1.0 KB"

// 格式化时长
val duration = 3661000L.formatDuration() // "01:01:01"

// URL 编码
val encoded = "hello world".urlEncode() // "hello%20world"
```

### Q: 如何管理下载目录？
```kotlin
// 设置下载目录（使用 SAF）
val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
startActivityForResult(intent, REQUEST_CODE)

// 在 onActivityResult 中
val uri = data?.data
DownloadDirectoryManager.setDownloadDirectory(uri)

// 获取下载目录
val directory = DownloadDirectoryManager.getDownloadDirectory()
```

## 相关文件清单

### 核心文件
- `Extensions.kt` - Kotlin 扩展函数
- `SettingsManager.kt` - 设置管理
- `BiometricHelper.kt` - 生物识别认证
- `GesturePasswordManager.kt` - 手势密码管理
- `DownloadDirectoryManager.kt` - 下载目录管理
- `NotificationHelper.kt` - 通知管理

---

## 变更记录 (Changelog)

### 2026-03-05 18:07:29
- 初始化模块文档
- 完成模块结构分析

---

**最后更新**: 2026-03-05 18:07:29
