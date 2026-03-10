package wang.zengye.dsm.ui.packages

import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import wang.zengye.dsm.R
import wang.zengye.dsm.data.api.DsmApiHelper
import wang.zengye.dsm.data.model.PackageInfo
import wang.zengye.dsm.data.repository.PackageRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import wang.zengye.dsm.ui.base.BaseState
import wang.zengye.dsm.util.appString
import javax.inject.Inject

data class PackageTabData(
    val isLoading: Boolean = false,
    val packages: List<PackageInfo> = emptyList(),
    val error: String? = null
)

data class PackagesUiState(
    val selectedTab: Int = 0,  // 0=已安装, 1=全部套件, 2=社群
    val installedTab: PackageTabData = PackageTabData(),
    val allPackagesTab: PackageTabData = PackageTabData(),
    val communityTab: PackageTabData = PackageTabData(),
    val filter: String = "all",
    val operatingPackageId: String? = null,
    val operationMessage: String? = null
) : BaseState {
    val currentTabData: PackageTabData
        get() = when (selectedTab) {
            0 -> installedTab
            1 -> allPackagesTab
            2 -> communityTab
            else -> installedTab
        }

    // 实现 BaseState 接口，委托给当前 tab
    override val isLoading: Boolean
        get() = currentTabData.isLoading

    override val error: String?
        get() = currentTabData.error
}

@HiltViewModel
class PackagesViewModel @Inject constructor(
    private val repository: PackageRepository
) : BaseViewModel<PackagesUiState, PackagesIntent, PackagesEvent>() {

    companion object {
        private const val TAG = "PackagesViewModel"
    }

    private val _state = MutableStateFlow(PackagesUiState())
    override val state: StateFlow<PackagesUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PackagesEvent>(
        extraBufferCapacity = 10
    )
    override val events = _events.asSharedFlow()

    // 缓存server packages数据（包含thumbnail），用于合并到已安装套件
    private var serverPackagesCache: List<PackageInfo> = emptyList()

    init {
        sendIntent(PackagesIntent.Refresh)
    }

    override suspend fun processIntent(intent: PackagesIntent) {
        when (intent) {
            is PackagesIntent.SelectTab -> selectTab(intent.index)
            is PackagesIntent.Refresh -> refresh()
            is PackagesIntent.SetFilter -> setFilter(intent.filter)
            is PackagesIntent.StartPackage -> startPackage(intent.packageId)
            is PackagesIntent.StopPackage -> stopPackage(intent.packageId)
            is PackagesIntent.InstallPackage -> installPackage(intent.packageName)
            is PackagesIntent.UninstallPackage -> uninstallPackage(intent.packageId, intent.removeData)
            is PackagesIntent.ClearOperationMessage -> clearOperationMessage()
        }
    }

    private fun selectTab(index: Int) {
        _state.update { it.copy(selectedTab = index) }
    }

    private suspend fun refresh() {
        loadInstalledPackages()
        loadAllPackages()
        loadCommunityPackages()
    }

    /**
     * 加载已安装套件
     */
    private suspend fun loadInstalledPackages() {
        _state.update { it.copy(installedTab = it.installedTab.copy(isLoading = true, error = null)) }

        val result = repository.getInstalledPackages()
        result.fold(
            onSuccess = { response ->
                val packages = parseInstalledPackages(response)
                _state.update {
                    it.copy(installedTab = PackageTabData(
                        isLoading = false,
                        packages = packages,
                        error = null
                    ))
                }
            },
            onFailure = { e ->
                _state.update {
                    it.copy(installedTab = PackageTabData(
                        isLoading = false,
                        packages = emptyList(),
                        error = e.message
                    ))
                }
            }
        )
    }

    /**
     * 加载所有可用套件（官方）
     */
    private suspend fun loadAllPackages() {
        _state.update { it.copy(allPackagesTab = it.allPackagesTab.copy(isLoading = true, error = null)) }

        val result = repository.getServerPackages()
        result.fold(
            onSuccess = { response ->
                Log.d(TAG, "Server packages raw response: $response")
                val packages = parseServerPackages(response)
                Log.d(TAG, "Server packages parsed count: ${packages.size}")
                // 缓存server packages数据，用于已安装套件合并thumbnail
                serverPackagesCache = packages
                _state.update {
                    it.copy(allPackagesTab = PackageTabData(
                        isLoading = false,
                        packages = packages,
                        error = null
                    ))
                }
            },
            onFailure = { e ->
                Log.e(TAG, "Server packages error", e)
                _state.update {
                    it.copy(allPackagesTab = PackageTabData(
                        isLoading = false,
                        packages = emptyList(),
                        error = e.message
                    ))
                }
            }
        )
    }

    /**
     * 加载社群套件（第三方）
     */
    private suspend fun loadCommunityPackages() {
        _state.update { it.copy(communityTab = it.communityTab.copy(isLoading = true, error = null)) }

        val result = repository.getCommunityPackages()
        result.fold(
            onSuccess = { response ->
                val packages = parseServerPackages(response)
                _state.update {
                    it.copy(communityTab = PackageTabData(
                        isLoading = false,
                        packages = packages,
                        error = null
                    ))
                }
            },
            onFailure = { e ->
                _state.update {
                    it.copy(communityTab = PackageTabData(
                        isLoading = false,
                        packages = emptyList(),
                        error = e.message
                    ))
                }
            }
        )
    }

    /**
     * 解析已安装套件响应
     */
    private fun parseInstalledPackages(response: wang.zengye.dsm.data.model.control_panel.InstalledPackagesDto): List<PackageInfo> {
        val packages = mutableListOf<PackageInfo>()
        val dataArray = response.data?.packages ?: return emptyList()

        // 创建server packages的映射表，用于快速查找thumbnail
        val serverPackagesMap = serverPackagesCache.associateBy { it.id }

        dataArray.forEach { element ->
            try {
                val packageId = element.id ?: return@forEach
                val packageName = element.packageName ?: packageId

                // 运行状态在 additional.status 中
                val actualStatus = element.additional?.status ?: element.status

                // 从server packages cache获取thumbnail（Flutter方式）
                val serverPackage = serverPackagesMap[packageId]
                val thumbnailUrl = serverPackage?.thumbnailUrl ?: buildDefaultIconUrl(packageName)

                Log.d(TAG, "Installed package: id=$packageId, thumbnailUrl=$thumbnailUrl, fromCache=${serverPackage != null}")

                val pkg = PackageInfo(
                    id = packageId,
                    name = packageName,
                    displayName = element.displayName
                        ?: element.displayNameAlt
                        ?: element.name
                        ?: serverPackage?.displayName
                        ?: "",
                    version = element.version ?: serverPackage?.version ?: "",
                    description = element.description ?: serverPackage?.description ?: "",
                    status = actualStatus ?: "",
                    url = element.url ?: serverPackage?.url ?: "",
                    launchable = parseBoolean(element.launchable),
                    installed = true,
                    thumbnailUrl = thumbnailUrl
                )
                packages.add(pkg)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing package", e)
            }
        }

        Log.d(TAG, "parseInstalledPackages - Parsed ${packages.size} packages")
        return packages.sortedBy { it.displayName.lowercase() }
    }

    /**
     * 解析服务器套件响应（全部套件/社群套件）
     */
    private fun parseServerPackages(response: wang.zengye.dsm.data.model.control_panel.ServerPackagesDto): List<PackageInfo> {
        val packages = mutableListOf<PackageInfo>()

        // 尝试从 data.packages 或 data.data 获取数据
        val dataArray = response.data?.packages ?: response.data?.data

        Log.d(TAG, "parseServerPackages - dataArray: $dataArray, size: ${dataArray?.size}")

        if (dataArray == null || dataArray.isEmpty()) {
            Log.w(TAG, "parseServerPackages - No packages found in response")
            return emptyList()
        }

        dataArray.forEach { element ->
            try {
                // 获取packageName用于默认图标
                val packageName = element.packageName ?: element.id

                // 获取缩略图URL
                val thumbnailUrl = getThumbnailUrl(element, packageName)

                Log.d(TAG, "Parsing package: id=${element.id}, dname=${element.displayName}, thumbnail=$thumbnailUrl")

                val pkg = PackageInfo(
                    id = element.id ?: "",
                    name = packageName ?: "",  // 使用 id 作为 name
                    displayName = element.displayName
                        ?: element.displayNameAlt
                        ?: element.name
                        ?: "",
                    version = element.version ?: "",
                    description = element.description ?: "",
                    status = element.status ?: "",
                    url = element.url ?: "",
                    launchable = parseBoolean(element.launchable),
                    installed = parseBoolean(element.installed),
                    thumbnailUrl = thumbnailUrl
                )
                packages.add(pkg)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing package", e)
            }
        }

        Log.d(TAG, "parseServerPackages - Parsed ${packages.size} packages")
        return packages.sortedBy { it.displayName.lowercase() }
    }

    /**
     * 安全解析布尔值（可能是 Boolean 或 Array）
     */
    private fun parseBoolean(value: Any?): Boolean {
        return when (value) {
            is Boolean -> value
            is List<*> -> value.isNotEmpty()
            else -> false
        }
    }

    /**
     * 获取套件图标URL
     */
    private fun getThumbnailUrl(element: wang.zengye.dsm.data.model.control_panel.ServerPackageDataDto, packageName: String? = null): String {
        return try {
            val thumbnails = element.thumbnail
            if (!thumbnails.isNullOrEmpty()) {
                val url = thumbnails.last()
                if (url.isNotEmpty() && !url.startsWith("http")) {
                    DsmApiHelper.baseUrl + url
                } else {
                    url
                }
            } else {
                buildDefaultIconUrl(packageName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting thumbnail URL", e)
            buildDefaultIconUrl(packageName)
        }
    }

    /**
     * 构建默认图标URL
     */
    private fun buildDefaultIconUrl(packageName: String?): String {
        if (packageName.isNullOrEmpty()) return ""
        val baseUrl = DsmApiHelper.baseUrl
        val sid = DsmApiHelper.getSessionId()
        return "$baseUrl/webman/3rdparty/$packageName/images/icon_64x64.png?_sid=$sid"
    }

    private fun setFilter(filter: String) {
        Log.d(TAG, "setFilter: $filter")
        _state.update { it.copy(filter = filter) }
    }

    fun getFilteredPackages(): List<PackageInfo> {
        val packages = _state.value.currentTabData.packages
        val isInstalledTab = _state.value.selectedTab == 0
        Log.d(TAG, "getFilteredPackages - filter=${_state.value.filter}, isInstalledTab=$isInstalledTab, total=${packages.size}")

        // 打印每个套件的状态用于调试
        packages.forEach {
            Log.d(TAG, "Package: ${it.displayName}, status=${it.status}, isRunning=${it.isRunning}, isStopped=${it.isStopped}, installed=${it.installed}")
        }

        // 状态过滤只在已安装tab生效
        return when {
            !isInstalledTab -> packages  // 非已安装tab，不过滤
            _state.value.filter == "running" -> packages.filter { it.isRunning }
            _state.value.filter == "stopped" -> packages.filter { it.isStopped }
            else -> packages
        }
    }

    private suspend fun startPackage(packageId: String) {
        _state.update { it.copy(operatingPackageId = packageId, operationMessage = appString(R.string.package_starting)) }

        repository.startPackage(packageId).fold(
            onSuccess = {
                _state.update { it.copy(operationMessage = appString(R.string.package_start_success)) }
                _events.emit(PackagesEvent.ShowSuccess(appString(R.string.package_start_success)))
                refresh()
            },
            onFailure = { e ->
                val errorMsg = appString(R.string.package_start_failed, e.message ?: "")
                _state.update { it.copy(operationMessage = errorMsg) }
                _events.emit(PackagesEvent.ShowError(errorMsg))
            }
        )

        _state.update { it.copy(operatingPackageId = null) }
    }

    private suspend fun stopPackage(packageId: String) {
        _state.update { it.copy(operatingPackageId = packageId, operationMessage = appString(R.string.package_stopping)) }

        repository.stopPackage(packageId).fold(
            onSuccess = {
                _state.update { it.copy(operationMessage = appString(R.string.package_stop_success)) }
                _events.emit(PackagesEvent.ShowSuccess(appString(R.string.package_stop_success)))
                refresh()
            },
            onFailure = { e ->
                val errorMsg = appString(R.string.package_stop_failed, e.message ?: "")
                _state.update { it.copy(operationMessage = errorMsg) }
                _events.emit(PackagesEvent.ShowError(errorMsg))
            }
        )

        _state.update { it.copy(operatingPackageId = null) }
    }

    /**
     * 安装套件
     */
    private suspend fun installPackage(packageName: String) {
        _state.update { it.copy(operatingPackageId = packageName, operationMessage = appString(R.string.package_installing)) }

        repository.installPackage(packageName).fold(
            onSuccess = {
                _state.update { it.copy(operationMessage = appString(R.string.package_install_success), operatingPackageId = null) }
                _events.emit(PackagesEvent.InstallSuccess(packageName))
                refresh()
            },
            onFailure = { e ->
                _state.update { it.copy(operatingPackageId = null) }
                _events.emit(PackagesEvent.ShowError(e.message ?: appString(R.string.package_install_failed)))
            }
        )
    }

    /**
     * 卸载套件
     */
    private suspend fun uninstallPackage(packageId: String, removeData: Boolean = false) {
        _state.update { it.copy(operatingPackageId = packageId, operationMessage = appString(R.string.package_uninstalling)) }

        repository.uninstallPackage(packageId, removeData).fold(
            onSuccess = {
                _state.update { it.copy(operationMessage = appString(R.string.package_uninstall_success), operatingPackageId = null) }
                _events.emit(PackagesEvent.UninstallSuccess(packageId))
                refresh()
            },
            onFailure = { e ->
                _state.update { it.copy(operatingPackageId = null) }
                _events.emit(PackagesEvent.ShowError(e.message ?: appString(R.string.package_uninstall_failed)))
            }
        )
    }

    private fun clearOperationMessage() {
        _state.update { it.copy(operationMessage = null) }
    }
}
