[根目录](../../../../../../../../../CLAUDE.md) > app > src > main > java > wang > zengye > dsm > **di**

---

# DI 模块

## 模块职责

DI（Dependency Injection）模块负责应用的依赖注入配置，使用 Hilt 框架管理依赖。主要职责包括：
- 提供 API 接口实例
- 提供网络客户端（OkHttpClient、Retrofit）
- 提供数据库实例（Room）
- 管理单例对象的生命周期

## 入口与启动

### 核心文件
- **ApiModule**: 提供所有 API 接口实例
- **NetworkModule**: 提供网络相关依赖（OkHttpClient、Retrofit）
- **DatabaseModule**: 提供数据库实例（Room）

### Hilt 配置
```kotlin
// 在 DSMApplication 中启用 Hilt
@HiltAndroidApp
class DSMApplication : Application() { ... }

// 在 MainActivity 中注入
@AndroidEntryPoint
class MainActivity : FragmentActivity() { ... }

// 在 ViewModel 中注入
@HiltViewModel
class FileViewModel @Inject constructor(
    private val repository: FileRepository
) : BaseViewModel<FileIntent, FileState, FileEvent>() { ... }
```

## 对外接口

### ApiModule

提供所有 API 接口实例：

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideAuthApi(): AuthApiRetrofit =
        DsmApiClient.createRetrofitService()

    @Provides
    @Singleton
    fun provideFileApi(): FileApiRetrofit =
        DsmApiClient.createRetrofitService()

    @Provides
    @Singleton
    fun providePhotoApi(): PhotoApiRetrofit =
        DsmApiClient.createRetrofitService()

    @Provides
    @Singleton
    fun provideDownloadApi(): DownloadApiRetrofit =
        DsmApiClient.createRetrofitService()

    @Provides
    @Singleton
    fun provideDockerApi(): DockerApiRetrofit =
        DsmApiClient.createRetrofitService()

    @Provides
    @Singleton
    fun provideControlPanelApi(): ControlPanelApiRetrofit =
        DsmApiClient.createRetrofitService()

    @Provides
    @Singleton
    fun provideSystemApi(): SystemApiRetrofit =
        DsmApiClient.createRetrofitService()

    @Provides
    @Singleton
    fun providePackageApi(): PackageApiRetrofit =
        DsmApiClient.createRetrofitService()

    @Provides
    @Singleton
    fun provideIscsiApi(): IscsiApiRetrofit =
        DsmApiClient.createRetrofitService()

    @Provides
    @Singleton
    fun provideVirtualMachineApi(): VirtualMachineApiRetrofit =
        DsmApiClient.createRetrofitService()
}
```

### NetworkModule

提供网络相关依赖：

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        DsmApiClient.okHttpClient

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(DsmApiClient.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
}
```

### DatabaseModule

提供数据库实例：

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "dsm_database"
    ).build()

    @Provides
    @Singleton
    fun provideDownloadTaskDao(
        database: AppDatabase
    ): DownloadTaskDao = database.downloadTaskDao()
}
```

## 关键依赖与配置

### 依赖
- **Hilt**: 2.58
- **Hilt Navigation Compose**: 1.3.0
- **KSP**: 2.3.5

### Hilt 作用域
- **SingletonComponent**: 应用级单例（API、数据库）
- **ViewModelComponent**: ViewModel 级别（Repository）
- **ActivityComponent**: Activity 级别

### 注入方式

#### 构造函数注入（推荐）
```kotlin
@HiltViewModel
class FileViewModel @Inject constructor(
    private val repository: FileRepository
) : BaseViewModel<FileIntent, FileState, FileEvent>()
```

#### 字段注入
```kotlin
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject lateinit var settingsManager: SettingsManager
}
```

#### 方法注入
```kotlin
@Inject
fun init(repository: FileRepository) {
    // 初始化逻辑
}
```

## 数据模型

### 依赖图
```
DSMApplication (HiltAndroidApp)
    ↓
MainActivity (AndroidEntryPoint)
    ↓
ViewModel (HiltViewModel)
    ↓
Repository (Inject)
    ↓
API / Database (Singleton)
```

## 测试与质量

### 测试配置
使用 Hilt Testing 进行测试：

```kotlin
@HiltAndroidTest
class FileViewModelTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: FileRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun testLoadFiles() {
        // 测试逻辑
    }
}
```

### Mock 依赖
使用 `@TestInstallIn` 替换生产依赖：

```kotlin
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ApiModule::class]
)
object FakeApiModule {
    @Provides
    @Singleton
    fun provideFileApi(): FileApiRetrofit = mockk()
}
```

## 常见问题 (FAQ)

### Q: 如何添加新的依赖？
1. 在对应的 Module 中添加 `@Provides` 方法
2. 使用 `@Singleton` 注解标记单例
3. 在需要注入的地方使用 `@Inject` 注解

### Q: 如何处理循环依赖？
- 使用 `Provider<T>` 延迟注入
- 重构代码，消除循环依赖
- 使用接口解耦

### Q: 如何在 Composable 中获取 ViewModel？
```kotlin
@Composable
fun FileScreen(
    viewModel: FileViewModel = hiltViewModel()
) {
    // UI 逻辑
}
```

### Q: 如何在非 Android 类中注入依赖？
使用 `@EntryPoint` 定义入口点：

```kotlin
@EntryPoint
@InstallIn(SingletonComponent::class)
interface RepositoryEntryPoint {
    fun fileRepository(): FileRepository
}

// 使用
val entryPoint = EntryPointAccessors.fromApplication(
    context,
    RepositoryEntryPoint::class.java
)
val repository = entryPoint.fileRepository()
```

## 相关文件清单

### 核心文件
- `ApiModule.kt` - API 接口提供
- `NetworkModule.kt` - 网络依赖提供
- `DatabaseModule.kt` - 数据库依赖提供

---

## 变更记录 (Changelog)

### 2026-03-05 18:07:29
- 初始化模块文档
- 完成模块结构分析

---

**最后更新**: 2026-03-05 18:07:29
