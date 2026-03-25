package wang.zengye.dsm.ui.dashboard

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import wang.zengye.dsm.data.api.DsmApiHelper
import wang.zengye.dsm.data.model.dashboard.VolumeInfo
import wang.zengye.dsm.data.repository.SystemRepository
import wang.zengye.dsm.ui.base.BaseState
import wang.zengye.dsm.ui.base.BaseViewModel
import wang.zengye.dsm.util.SettingsManager
import javax.inject.Inject

/**
 * UI 层的系统信息数据类
 * 用于 Dashboard 显示，包含格式化后的数据
 */
data class SystemInfoUi(
    val model: String = "",
    val hostname: String = "",
    val uptime: String = "",
    val temperature: Int = 0,
    val temperatureWarning: Boolean = false,
    val dsmVersion: String = ""
)

data class UtilizationInfo(
    val cpuUsage: Int = 0,
    val memoryUsage: Int = 0,
    val memoryTotal: Long = 0,
    val memoryUsed: Long = 0,
    val cpuHistory: List<Int> = emptyList(),
    val memoryHistory: List<Int> = emptyList()
) {
    companion object {
        private const val MAX_HISTORY_SIZE = 20
    }

    fun addHistoryPoint(cpu: Int, memory: Int): UtilizationInfo {
        val newCpuHistory = (cpuHistory + cpu).takeLast(MAX_HISTORY_SIZE)
        val newMemoryHistory = (memoryHistory + memory).takeLast(MAX_HISTORY_SIZE)
        return copy(cpuHistory = newCpuHistory, memoryHistory = newMemoryHistory)
    }
}

/**
 * UI 层的卷信息数据类
 */
data class VolumeInfoUi(
    val id: String = "",
    val name: String = "",
    val totalSize: Long = 0,
    val usedSize: Long = 0,
    val status: String = "",
    val fsType: String = "",
    val volumeType: String = "",
    val usagePercent: Int? = null
) {
    val usage: Float
        get() = when {
            totalSize > 0 -> usedSize.toFloat() / totalSize
            usagePercent != null -> (usagePercent.coerceIn(0, 100) / 100f)
            else -> 0f
        }
}

/**
 * UI 层的磁盘信息数据类
 */
data class DiskInfoUi(
    val id: String = "",
    val name: String = "",
    val model: String = "",
    val serial: String = "",
    val totalSize: Long = 0,
    val temperature: Int = 0,
    val status: String = "",
    val health: String = "",
    val smartStatus: String = ""
)

data class NetworkInfo(
    val device: String = "",
    val name: String = "",
    val rxSpeed: Long = 0,
    val txSpeed: Long = 0,
    val ip: String = "",
    val rxHistory: List<Long> = emptyList(),
    val txHistory: List<Long> = emptyList()
) {
    companion object {
        private const val MAX_HISTORY_SIZE = 20
    }

    fun addHistoryPoint(rx: Long, tx: Long): NetworkInfo {
        val newRxHistory = (rxHistory + rx).takeLast(MAX_HISTORY_SIZE)
        val newTxHistory = (txHistory + tx).takeLast(MAX_HISTORY_SIZE)
        return copy(rxHistory = newRxHistory, txHistory = newTxHistory)
    }
}

data class DashboardUiState(
    override val isLoading: Boolean = true,
    override val error: String? = null,
    val systemInfo: SystemInfoUi = SystemInfoUi(),
    val utilization: UtilizationInfo = UtilizationInfo(),
    val volumes: List<VolumeInfoUi> = emptyList(),
    val disks: List<DiskInfoUi> = emptyList(),
    val networks: List<NetworkInfo> = emptyList(),
    val connectedUsers: Int = 0,
    val refreshDuration: Int = 10,
    val isRefreshing: Boolean = false,
    val installedApps: List<String> = emptyList()
) : BaseState

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val systemRepository: SystemRepository
) : BaseViewModel<DashboardUiState, DashboardIntent, DashboardEvent>(), DefaultLifecycleObserver {

    companion object {
        private const val TAG = "DashboardViewModel"
    }

    private val _uiState = MutableStateFlow(DashboardUiState())
    override val state: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DashboardEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    private var refreshJob: Job? = null

    init {
        viewModelScope.launch {
            val duration = SettingsManager.refreshDuration.first()
            _uiState.update { it.copy(refreshDuration = duration) }
        }
        sendIntent(DashboardIntent.LoadData)
    }

    override suspend fun processIntent(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.LoadData -> loadData(showLoading = true, isManualRefresh = false)
            is DashboardIntent.Refresh -> loadData(showLoading = false, isManualRefresh = true)
            is DashboardIntent.Shutdown -> shutdown()
            is DashboardIntent.Reboot -> reboot()
            is DashboardIntent.StartAutoRefresh -> startAutoRefresh()
            is DashboardIntent.StopAutoRefresh -> refreshJob?.cancel()
        }
    }

    private fun hasData(state: DashboardUiState): Boolean {
        return state.systemInfo.model.isNotEmpty() ||
                state.systemInfo.hostname.isNotEmpty() ||
                state.utilization.memoryTotal > 0 ||
                state.volumes.isNotEmpty() ||
                state.disks.isNotEmpty() ||
                state.networks.isNotEmpty()
    }

    private suspend fun loadData(showLoading: Boolean = true, isManualRefresh: Boolean = false) {
        val shouldShowLoading = showLoading && !hasData(_uiState.value)
        _uiState.update {
            it.copy(
                isLoading = shouldShowLoading,
                isRefreshing = isManualRefresh,
                error = null
            )
        }

            // 使用 Moshi API 直接获取强类型
            val systemResult = systemRepository.getSystemInfo()
            val utilizationResult = systemRepository.getUtilization()
            val storageResult = systemRepository.getStorageInfo()
            val networkInterfacesResult = systemRepository.getNetworkInterfaces()
            val initdataResult = systemRepository.getDesktopInitdata()

            // 处理系统信息
            systemResult.onSuccess { response ->
                val data = response.data
                val hostname = data?.hostname ?: data?.serverName ?: data?.hostName ?: ""
                val dsmVersionString = data?.firmwareVer ?: data?.version ?: ""
                val majorVersion = dsmVersionString.split(".").firstOrNull()?.toIntOrNull() ?: 7
                DsmApiHelper.updateDsmVersion(majorVersion)

                _uiState.update { state ->
                    state.copy(
                        systemInfo = SystemInfoUi(
                            model = data?.model ?: "",
                            hostname = hostname,
                            uptime = data?.upTime ?: "",
                            temperature = data?.sysTemp ?: 0,
                            temperatureWarning = data?.temperatureWarning ?: false,
                            dsmVersion = dsmVersionString
                        )
                    )
                }
                if (hostname.isNotEmpty()) {
                    DsmApiHelper.updateHostname(hostname)
                }
            }.onFailure { error ->
                Log.e(TAG, "SystemInfo API failed: $error")
            }

            // 如果没有获取到 hostname，尝试单独获取
            if (_uiState.value.systemInfo.hostname.isEmpty()) {
                systemRepository.getHostname().onSuccess { response ->
                    val hostnameData = response.data
                    val hostname = hostnameData?.hostname
                        ?: hostnameData?.serverName
                        ?: hostnameData?.hostName
                        ?: hostnameData?.name
                        ?: ""
                    if (hostname.isNotEmpty()) {
                        _uiState.update { state ->
                            state.copy(systemInfo = state.systemInfo.copy(hostname = hostname))
                        }
                        DsmApiHelper.updateHostname(hostname)
                    }
                }
            }

            // 处理利用率
            utilizationResult.onSuccess { response ->
                val data = response.data
                val cpuUsage = (data?.cpu?.userLoad ?: 0) + (data?.cpu?.systemLoad ?: 0)
                val memoryUsage = data?.memory?.realUsage ?: 0
                val memoryTotal = (data?.memory?.memorySize ?: 0L) * 1024
                val memoryUsed = memoryUsage * memoryTotal / 100

                _uiState.update { state ->
                    val newUtilization =
                        state.utilization.addHistoryPoint(cpuUsage, memoryUsage).copy(
                            cpuUsage = cpuUsage,
                            memoryUsage = memoryUsage,
                            memoryTotal = memoryTotal,
                            memoryUsed = memoryUsed
                        )
                    state.copy(utilization = newUtilization)
                }

                // 从 utilization 中获取网络速度
                data?.network?.firstOrNull()?.let { networkUtil ->
                    _uiState.update { state ->
                        if (state.networks.isNotEmpty()) {
                            val updated = state.networks.mapIndexed { index, existing ->
                                if (index == 0) {
                                    existing.addHistoryPoint(
                                        networkUtil.rx ?: 0,
                                        networkUtil.tx ?: 0
                                    ).copy(
                                        rxSpeed = networkUtil.rx ?: 0,
                                        txSpeed = networkUtil.tx ?: 0
                                    )
                                } else existing
                            }
                            state.copy(networks = updated)
                        } else {
                            state.copy(
                                networks = listOf(
                                    NetworkInfo(
                                        device = networkUtil.device ?: "",
                                        name = networkUtil.device ?: "",
                                        rxSpeed = networkUtil.rx ?: 0,
                                        txSpeed = networkUtil.tx ?: 0
                                    )
                                )
                            )
                        }
                    }
                }
            }.onFailure { error ->
                Log.e(TAG, "Utilization API failed: $error")
            }

            // 处理存储信息
            storageResult.onSuccess { response ->
                val data = response.data
                val volumesList = data?.volumes?.map { it.toVolumeInfoUi() } ?: emptyList()
                val disksList = data?.disks?.map { it.toDiskInfoUi() } ?: emptyList()
                _uiState.update { state ->
                    state.copy(
                        volumes = if (volumesList.isNotEmpty()) mergeVolumeUsage(
                            volumesList,
                            state.volumes
                        ) else state.volumes,
                        disks = if (disksList.isNotEmpty()) disksList else state.disks
                    )
                }
            }.onFailure { error ->
                Log.e(TAG, "Storage API failed: $error")
            }

            // 处理网络接口
            networkInterfacesResult.onSuccess { response ->
                try {
                    val networkList = response.data?.mapNotNull { iface ->
                        iface.ip?.let { ipAddr ->
                            NetworkInfo(
                                device = iface.ifname ?: "",
                                name = iface.ifname ?: "",
                                ip = ipAddr,
                                rxSpeed = 0,
                                txSpeed = 0
                            )
                        }
                    } ?: emptyList()

                    if (networkList.isNotEmpty()) {
                        _uiState.update { state ->
                            val currentNetworks = state.networks
                            val updatedNetworks: List<NetworkInfo> =
                                if (currentNetworks.isNotEmpty()) {
                                    val firstSpeedData =
                                        currentNetworks.firstOrNull { it.rxSpeed > 0 || it.txSpeed > 0 }
                                    if (firstSpeedData != null && networkList.isNotEmpty()) {
                                        val updated = networkList.toMutableList()
                                        updated[0] = updated[0].copy(
                                            rxSpeed = firstSpeedData.rxSpeed,
                                            txSpeed = firstSpeedData.txSpeed
                                        )
                                        updated
                                    } else {
                                        networkList
                                    }
                                } else {
                                    networkList
                                }
                            state.copy(networks = updatedNetworks)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Parse network info failed", e)
                }
            }.onFailure { error ->
                Log.e(TAG, "Network API failed: $error")
            }

            // 处理桌面应用
            initdataResult.onSuccess { response ->
                val apps = response.data?.getEnabledAppIds() ?: emptyList()
                Log.d(TAG, "Desktop apps loaded: ${apps.size}, ids: $apps")
                _uiState.update { it.copy(installedApps = apps) }
            }.onFailure { error ->
                Log.e(TAG, "DesktopInitdata API failed: $error")
            }

            _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
        startAutoRefresh()
    }

    private fun VolumeInfo.toVolumeInfoUi(): VolumeInfoUi {
        val sizeObj = this.size
        val totalSize = sizeObj?.total?.toLongOrNull() ?: this.sizeTotal?.toLongOrNull() ?: 0L
        val usedSize = sizeObj?.used?.toLongOrNull() ?: this.sizeUsed?.toLongOrNull() ?: 0L
        val volumeName = this.deployPath ?: this.volumeLabel ?: this.displayName ?: this.id ?: ""
        return VolumeInfoUi(
            id = this.id ?: this.volumeId ?: "",
            name = volumeName,
            totalSize = totalSize,
            usedSize = usedSize,
            status = this.status ?: "",
            fsType = this.fsType ?: "",
            volumeType = this.volType ?: this.type ?: ""
        )
    }

    private fun wang.zengye.dsm.data.model.dashboard.DiskInfo.toDiskInfoUi(): DiskInfoUi {
        return DiskInfoUi(
            id = this.id ?: "",
            name = this.name ?: "",
            model = (this.model ?: "").trim(),
            serial = this.serial ?: "",
            totalSize = this.getSizeLong(),
            temperature = this.temp ?: -1,
            status = this.status ?: "",
            health = this.health ?: ""
        )
    }

    private fun mergeVolumeUsage(
        primary: List<VolumeInfoUi>,
        secondary: List<VolumeInfoUi>
    ): List<VolumeInfoUi> {
        if (primary.isEmpty()) return secondary
        if (secondary.isEmpty()) return primary
        val map = secondary.associateBy { it.id.ifEmpty { it.name } }
        return primary.map { volume ->
            val key = volume.id.ifEmpty { volume.name }
            val match = map[key]
            if (match == null) {
                volume
            } else {
                volume.copy(usagePercent = match.usagePercent ?: volume.usagePercent)
            }
        }
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        val duration = _uiState.value.refreshDuration
        if (duration > 0) {
            refreshJob = viewModelScope.launch {
                delay(duration * 1000L)
                loadData(showLoading = false)
            }
        }
    }

    private suspend fun refresh() {
        loadData(showLoading = false, isManualRefresh = true)
    }

    private suspend fun shutdown() {
        systemRepository.shutdown().onSuccess {
            _events.emit(DashboardEvent.ShutdownSuccess)
        }.onFailure { error ->
            _events.emit(DashboardEvent.Error(error.message ?: "Shutdown failed"))
        }
    }

    private suspend fun reboot() {
        systemRepository.reboot().onSuccess {
            _events.emit(DashboardEvent.RebootSuccess)
        }.onFailure { error ->
            _events.emit(DashboardEvent.Error(error.message ?: "Reboot failed"))
        }
    }

    // ===== LifecycleObserver: 退后台/锁屏时停止自动刷新 =====
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        refreshJob?.cancel()
        refreshJob = null
        Log.d(TAG, "onStop: 自动刷新已停止")
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        // 回到前台时恢复自动刷新
        if (_uiState.value.refreshDuration > 0) {
            startAutoRefresh()
            Log.d(TAG, "onStart: 自动刷新已恢复")
        }
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }
}
