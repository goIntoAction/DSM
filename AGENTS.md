# DSM - Synology DSM Android 客户端

## 项目概述

DSM 是一个功能完整的 Synology DSM Android 客户端应用，旨在为用户提供便捷的移动端 NAS 管理体验。应用采用现代化的 Android 开发技术栈，使用 Kotlin + Jetpack Compose 构建，遵循 MVI（Model-View-Intent）架构模式。

**核心功能**：
- 仪表盘监控（系统资源、存储、网络等）
- 文件管理（浏览、上传、下载、分享）
- 照片管理（相册浏览、照片预览）
- 下载站管理（任务管理、BT/磁力链接）
- Docker 容器管理
- 控制面板（用户、共享文件夹、网络、存储等）
- SSH 终端
- 系统信息与性能监控

---

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| **语言** | Kotlin | 2.3.10 |
| **UI 框架** | Jetpack Compose | BOM 2026.02.01 |
| **架构模式** | MVI (Model-View-Intent) | - |
| **依赖注入** | Hilt | 2.58 |
| **网络** | Retrofit + OkHttp | 3.0.0 + 5.3.2 |
| **JSON 解析** | Moshi | 1.15.2 |
| **图片加载** | Coil | 2.7.0 |
| **数据持久化** | Room + DataStore | 2.7.0-alpha12 + 1.2.0 |
| **导航** | Navigation Compose | 2.9.7 |
| **图表** | Vico | 3.0.2 |
| **视频播放** | MPV Android | 0.1.9 |
| **SSH** | ConnectBot sshlib + termlib | 2.2.43 + 0.0.18 |

**编译配置**：
- **compileSdk**: 36
- **targetSdk**: 35
- **minSdk**: 24 (Android 7.0)
- **JDK**: 17
- **AGP**: 8.13.2

---

## 架构设计

### MVI 架构模式

每个功能模块遵循 MVI 架构，包含 4 个核心文件：

```
FeatureModule/
├── Screen.kt       # Composable UI，接收 State 并发送 Intent
├── ViewModel.kt    # 继承 BaseViewModel，处理 Intent 并更新 State
├── Intent.kt       # 密封类，定义所有用户意图
└── Event.kt        # 密封类，定义单次事件（如导航、Toast）
```

### 架构分层

```
┌─────────────────────────────────────────┐
│           UI Layer (Compose)            │
│  - Screen (UI 渲染)                     │
│  - ViewModel (状态管理 + Intent 处理)   │
│  - Intent (用户意图)                    │
│  - Event (单次事件)                     │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│         Domain Layer (可选)             │
│  - Use Cases (业务逻辑)                 │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│           Data Layer                    │
│  - Repository (数据协调)                │
│  - API (Retrofit 接口)                  │
│  - Database (Room)                      │
│  - DataStore (设置存储)                 │
└─────────────────────────────────────────┘
```

---

## 模块结构

```
app/src/main/java/wang/zengye/dsm/
├── data/                    # 数据层
│   ├── api/                 # Retrofit API 接口
│   ├── model/               # 数据模型
│   ├── repository/          # Repository 封装
│   ├── database/            # Room 数据库
│   ├── dao/                 # DAO 接口
│   └── entity/              # 数据库实体
├── ui/                      # UI 层
│   ├── dashboard/           # 仪表盘
│   ├── file/                # 文件管理
│   ├── download/            # 下载站
│   ├── photos/              # 照片管理
│   ├── docker/              # Docker 管理
│   ├── control_panel/       # 控制面板
│   ├── login/               # 登录
│   ├── settings/            # 设置
│   ├── components/          # 通用组件
│   ├── theme/               # 主题定义
│   └── base/                # 基基础类
├── navigation/              # 导航管理
├── di/                      # 依赖注入（Hilt）
├── util/                    # 工具类
├── terminal/                # SSH 终端
├── service/                 # 后台服务
├── DSMApplication.kt        # Application 入口
└── MainActivity.kt          # Activity 入口
```

---

## 构建与运行

### 环境要求
- Android Studio Ladybug (2024.2.1) 或更高版本
- JDK 17
- Android SDK 36

### 构建命令

```bash
# 调试版本
./gradlew assembleDebug

# 发布版本（多架构 APK）
./gradlew assembleRelease

# 运行单元测试
./gradlew test

# 运行 UI 测试
./gradlew connectedAndroidTest

# 清理构建
./gradlew clean
```

### 多架构编译

项目配置了 ABI 分离编译，会生成以下 APK：
- `app-armeabi-v7a-release.apk` (32-bit ARM)
- `app-arm64-v8a-release.apk` (64-bit ARM)
- `app-x86-release.apk` (x86)
- `app-x86_64-release.apk` (x86_64)
- `app-universal-release.apk` (包含所有架构)

### 开发服务器

应用需要连接到 Synology DSM 服务器：
1. 确保设备与 DSM 服务器在同一网络
2. 在登录界面输入 DSM 地址（如 `http://192.168.1.100:5000`）
3. 使用 DSM 账户登录

---

## 开发规范

### MVI 架构规范

```kotlin
// Intent: 用户意图定义
sealed interface FileIntent {
    data class LoadFiles(val path: String) : FileIntent
    data class DeleteFile(val path: String) : FileIntent
}

// Event: 单次事件定义
sealed interface FileEvent {
    data class ShowToast(val message: String) : FileEvent
    data class NavigateToDetail(val path: String) : FileEvent
}

// State: UI 状态定义
data class FileState(
    val files: List<FileItem> = emptyList(),
    val currentPath: String = "/",
    val isLoading: Boolean = false,
    val error: String? = null
)

// ViewModel: 状态管理
@HiltViewModel
class FileViewModel @Inject constructor(
    private val repository: FileRepository
) : BaseViewModel<FileIntent, FileState, FileEvent>() {

    override fun handleIntent(intent: FileIntent) {
        when (intent) {
            is FileIntent.LoadFiles -> loadFiles(intent.path)
            is FileIntent.DeleteFile -> deleteFile(intent.path)
        }
    }

    private fun loadFiles(path: String) {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            repository.getFiles(path)
                .onSuccess { files ->
                    updateState { copy(files = files, isLoading = false) }
                }
                .onFailure { error ->
                    updateState { copy(error = error.message, isLoading = false) }
                }
        }
    }
}

// Screen: UI 渲染
@Composable
fun FileScreen(
    viewModel: FileViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.handleIntent(FileIntent.LoadFiles("/"))
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is FileEvent.ShowToast -> { /* 显示 Toast */ }
                is FileEvent.NavigateToDetail -> onNavigateToDetail(event.path)
            }
        }
    }

    // UI 渲染
}
```

### 命名约定

| 类型 | 命名规则 | 示例 |
|------|---------|------|
| 文件 | PascalCase | `FileDetailScreen.kt` |
| 类/接口 | PascalCase | `FileRepository` |
| 函数/变量 | camelCase | `loadFiles()` |
| 常量 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| 资源 ID | snake_case | `file_detail_title` |
| 测试方法 | `should[ExpectedBehavior]_when[Condition]` | `shouldShowError_whenLoadFails()` |

### 代码风格
- 使用 Kotlin 官方代码风格
- 每行最大 120 字符
- 使用 4 空格缩进
- 优先使用表达式而非语句
- 使用命名参数提高可读性

---

## AI 使用指引

### 新增功能模块

1. 在 `ui` 目录下创建新模块目录
2. 创建 4 个核心文件：`Screen.kt`, `ViewModel.kt`, `Intent.kt`, `Event.kt`
3. 在 `navigation/Navigation.kt` 中添加路由定义
4. 在导航图中添加 `composable` 路由
5. 如需 API 支持：
   - 在 `data/api` 中创建 Retrofit 接口
   - 在 `di/ApiModule.kt` 中提供实例
   - 在 `data/model` 中定义数据模型
   - 在 `data/repository` 中封装业务逻辑

### 添加新 API 接口

```kotlin
// 1. 在 data/api 中创建 Retrofit 接口
interface NewApiRetrofit {
    @GET("webapi/entry.cgi")
    suspend fun getData(
        @Query("api") api: String = "SYNO.New.Api",
        @Query("version") version: Int = 1,
        @Query("method") method: String = "get"
    ): NewResponse
}

// 2. 在 di/ApiModule.kt 中提供实例
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    @Singleton
    fun provideNewApi(): NewApiRetrofit =
        DsmApiHelper.createRetrofitService()
}

// 3. 在 data/model 中定义数据模型
@JsonClass(generateAdapter = true)
data class NewResponse(
    val data: NewData
)

// 4. 在 data/repository 中封装业务逻辑
class NewRepository @Inject constructor(
    private val api: NewApiRetrofit
) {
    suspend fun getData(): Result<NewData> = runCatching {
        api.getData().data
    }
}
```

### 添加新路由

```kotlin
// 1. 在 navigation/Navigation.kt 中定义路由
object DsmRoute {
    @Serializable data object NewFeature
    @Serializable data class NewDetail(val id: String)
}

// 2. 在导航图中添加 composable
composable<DsmRoute.NewFeature> {
    NewFeatureScreen(
        onNavigateToDetail = { id ->
            navController.navigate(DsmRoute.NewDetail(id))
        }
    )
}

composable<DsmRoute.NewDetail> { backStackEntry ->
    val args = backStackEntry.toRoute<DsmRoute.NewDetail>()
    NewDetailScreen(id = args.id)
}
```

### 添加新设置项

```kotlin
// 在 util/SettingsManager.kt 中添加
object SettingsManager {
    private object PreferencesKeys {
        val NEW_SETTING = booleanPreferencesKey("new_setting")
    }

    val newSetting: Flow<Boolean> = dataStore.data
        .map { it[PreferencesKeys.NEW_SETTING] ?: false }

    suspend fun setNewSetting(value: Boolean) {
        dataStore.edit { it[PreferencesKeys.NEW_SETTING] = value }
    }
}
```

### 添加新数据库表

```kotlin
// 1. 在 data/entity 中定义 Entity
@Entity(tableName = "new_table")
data class NewEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: Long
)

// 2. 在 data/dao 中定义 DAO
@Dao
interface NewDao {
    @Query("SELECT * FROM new_table")
    suspend fun getAll(): List<NewEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NewEntity)

    @Delete
    suspend fun delete(entity: NewEntity)
}

// 3. 在 AppDatabase 中添加 DAO
@Database(
    entities = [NewEntity::class, DownloadTaskEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun newDao(): NewDao
    abstract fun downloadTaskDao(): DownloadTaskDao
}
```

---

## 关键注意事项

### 会话管理
- 所有网络请求必须通过 `DsmApiHelper` 进行，以确保会话管理
- 会话过期自动检测并重登录
- 重登录失败会发送 `sessionExpiredEvent`，UI 层监听后跳转登录页

### UI 状态更新
- 状态更新必须通过 ViewModel 的 `updateState` 方法
- 单次事件必须通过 `sendEvent` 发送，避免重复触发
- 使用 `collectAsStateWithLifecycle()` 收集 StateFlow

### 文件路径处理
- 文件路径使用绝对路径，避免相对路径问题
- 路径参数传递时需要 URL 编码

### 错误处理
- Repository 方法返回 `Result<T>`
- ViewModel 层通过 `onSuccess`/`onFailure` 处理结果
- 网络错误统一通过拦截器处理

### 安全性
- 敏感信息使用 `EncryptedSharedPreferences` 存储
- SSL 支持自签名证书（用于私有 NAS）
- 日志脱敏处理敏感信息

---

## 测试策略

### 单元测试
- **位置**: `app/src/test/java/wang/zengye/dsm`
- **框架**: JUnit 4, MockK, Turbine, Truth, Coroutines Test
- **覆盖**: ViewModel、Repository、Util

### UI 测试
- **位置**: `app/src/androidTest/java/wang/zengye/dsm`
- **框架**: Espresso, Compose UI Test
- **覆盖**: 关键用户流程、UI 组件交互

---

## 文档资源

- [根目录文档](./CLAUDE.md) - 项目总览
- [Data 模块](./app/src/main/java/wang/zengye/dsm/data/CLAUDE.md) - 数据层详解
- [UI 模块](./app/src/main/java/wang/zengye/dsm/ui/CLAUDE.md) - UI 层详解
- [Navigation 模块](./app/src/main/java/wang/zengye/dsm/navigation/CLAUDE.md) - 导航详解
- [DI 模块](./app/src/main/java/wang/zengye/dsm/di/CLAUDE.md) - 依赖注入详解
- [Util 模块](./app/src/main/java/wang/zengye/dsm/util/CLAUDE.md) - 工具类详解
- [Terminal 模块](./app/src/main/java/wang/zengye/dsm/terminal/CLAUDE.md) - SSH 终端详解

---

**最后更新**: 2026-03-08
