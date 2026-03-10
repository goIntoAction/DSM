[根目录](../../../../../../../../../CLAUDE.md) > app > src > main > java > wang > zengye > dsm > **data**

---

# Data 模块

## 模块职责

Data 模块是应用的数据层，负责所有数据的获取、存储和管理。主要职责包括：
- 与 Synology DSM API 通信
- 本地数据库管理（Room）
- 数据模型定义
- Repository 模式封装业务逻辑
- 会话管理与自动重登录

## 入口与启动

### 核心类
- **DsmApiClient**: 单例对象，管理所有 API 请求、会话状态、自动重登录
- **AppDatabase**: Room 数据库实例
- **BaseRepository**: Repository 基类，提供通用错误处理

### 初始化流程
```kotlin
// 在 DSMApplication.onCreate() 中初始化
DsmApiClient.initOkHttpClient()  // 同步初始化 OkHttpClient
DsmApiClient.loadSessionAndInit() // 异步加载会话状态
```

## 对外接口

### API 层 (data/api)
所有 Retrofit 接口定义，按功能模块划分：
- `AuthApiRetrofit`: 认证相关（登录、登出、OTP）
- `FileApiRetrofit`: 文件管理（列表、上传、下载、删除）
- `PhotoApiRetrofit`: 照片管理（相册、照片列表）
- `DownloadApiRetrofit`: 下载站（任务管理、BT/磁力链接）
- `DockerApiRetrofit`: Docker 容器管理
- `ControlPanelApiRetrofit`: 控制面板（用户、共享文件夹、网络等）
- `SystemApiRetrofit`: 系统信息（资源监控、日志、通知）
- `PackageApiRetrofit`: 套件管理
- `IscsiApiRetrofit`: iSCSI 管理
- `VirtualMachineApiRetrofit`: 虚拟机管理

### Repository 层 (data/repository)
封装业务逻辑，提供给 ViewModel 调用：
- `AuthRepository`: 登录、登出、账户管理
- `FileRepository`: 文件操作、搜索、收藏
- `FileDownloadRepository`: 文件下载管理
- `DownloadRepository`: 下载站任务管理
- `DockerRepository`: Docker 容器操作
- `ControlPanelRepository`: 控制面板操作
- `SystemRepository`: 系统信息获取
- `PackageRepository`: 套件管理
- `IscsiRepository`: iSCSI 管理
- `VirtualMachineRepository`: 虚拟机管理
- `PerformanceHistoryRepository`: 性能历史数据
- `DownloadTaskRepository`: 本地下载任务管理（Room）

## 关键依赖与配置

### 网络层
- **Retrofit 3.0.0**: HTTP 客户端
- **OkHttp 5.3.2**: 底层网络库
- **Moshi 1.15.2**: JSON 解析

### 拦截器
- `SessionInterceptor`: 自动添加会话 Cookie 和 SynoToken
- `SafeLoggingInterceptor`: 安全日志记录（脱敏敏感信息）

### 超时配置
```kotlin
TIMEOUT = 30L              // 连接超时
READ_TIMEOUT = 60L         // 读取超时
WRITE_TIMEOUT = 60L        // 写入超时
UPLOAD_READ_TIMEOUT = 300L // 上传读取超时（5分钟）
UPLOAD_WRITE_TIMEOUT = 300L // 上传写入超时（5分钟）
```

### SSL 配置
支持自签名证书，使用 `UnsafeTrustManager` 信任所有证书（仅用于私有 NAS）。

## 数据模型

### Model 层 (data/model)
按功能模块划分：
- `FileModels.kt`: 文件、文件夹、搜索结果
- `PhotoModels.kt`: 相册、照片
- `SystemModels.kt`: 系统信息、资源监控
- `PackageInfo.kt`: 套件信息（UI 层数据类）
- `dashboard/*`: 仪表盘相关模型（存储、网络、进程等）
- `control_panel/*`: 控制面板相关模型（用户、共享文件夹、网络等）
- `docker/*`: Docker 相关模型
- `iscsi/*`: iSCSI 相关模型
- `virtual_machine/*`: 虚拟机相关模型
- `update/*`: 系统更新相关模型

### 数据转换
- API 响应模型（带 `@JsonClass` 注解）→ UI 数据类
- 使用扩展函数进行转换（如 `toPackageInfo()`）

## 数据库 (Room)

### Entity
- `DownloadTaskEntity`: 下载任务实体

### DAO
- `DownloadTaskDao`: 下载任务数据访问对象

### 数据库配置
```kotlin
@Database(
    entities = [DownloadTaskEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun downloadTaskDao(): DownloadTaskDao
}
```

## 测试与质量

### 单元测试
- `data/api/ApiResultTest.kt`: API 结果处理测试
- `data/model/PackageInfoTest.kt`: 数据模型测试
- `data/repository/*Test.kt`: Repository 层测试

### 测试策略
- 使用 MockK 模拟 API 响应
- 使用 Turbine 测试 Flow
- 使用 Truth 进行断言

## 常见问题 (FAQ)

### Q: 如何添加新的 API 接口？
1. 在 `data/api` 中创建 Retrofit 接口（如 `NewApiRetrofit.kt`）
2. 在 `di/ApiModule.kt` 中提供实例
3. 在 `data/model` 中定义响应模型
4. 在 `data/repository` 中创建 Repository 封装业务逻辑

### Q: 会话过期如何处理？
`DsmApiClient` 会自动检测会话过期（错误码 106/107/109/409），并尝试使用存储的凭据重新登录。如果重登录失败，会发送 `sessionExpiredEvent`，UI 层监听后跳转到登录页。

### Q: 如何处理 API 错误？
所有 Repository 方法返回 `Result<T>`，使用 `runCatching` 捕获异常。ViewModel 层通过 `onSuccess`/`onFailure` 处理结果。

### Q: 如何添加新的数据库表？
1. 在 `data/entity` 中定义 Entity
2. 在 `data/dao` 中定义 DAO
3. 在 `AppDatabase` 中添加 DAO 抽象方法
4. 更新数据库版本号并提供迁移策略

## 相关文件清单

### API 层
- `api/DsmApiClient.kt` - API 客户端核心
- `api/SessionInterceptor.kt` - 会话拦截器
- `api/SafeLoggingInterceptor.kt` - 安全日志拦截器
- `api/ApiConstants.kt` - API 常量定义
- `api/*ApiRetrofit.kt` - 各功能模块 API 接口

### Repository 层
- `repository/BaseRepository.kt` - Repository 基类
- `repository/*Repository.kt` - 各功能模块 Repository

### 数据库层
- `database/AppDatabase.kt` - Room 数据库
- `dao/DownloadTaskDao.kt` - 下载任务 DAO
- `entity/DownloadTaskEntity.kt` - 下载任务实体

### 模型层
- `model/*.kt` - 各功能模块数据模型

---

## 变更记录 (Changelog)

### 2026-03-05 18:07:29
- 初始化模块文档
- 完成模块结构分析

---

**最后更新**: 2026-03-05 18:07:29
