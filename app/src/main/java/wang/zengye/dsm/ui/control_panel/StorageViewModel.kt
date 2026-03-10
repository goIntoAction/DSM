package wang.zengye.dsm.ui.control_panel

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import wang.zengye.dsm.data.model.dashboard.DiskInfo
import wang.zengye.dsm.data.model.dashboard.EnvInfo
import wang.zengye.dsm.data.model.dashboard.HotSpareInfo
import wang.zengye.dsm.data.model.dashboard.SsdCacheInfo
import wang.zengye.dsm.data.model.dashboard.StorageData
import wang.zengye.dsm.data.model.dashboard.StoragePoolInfo
import wang.zengye.dsm.data.model.dashboard.VolumeInfo
import wang.zengye.dsm.data.repository.SystemRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import javax.inject.Inject

data class VolumeInfoUi(
    val id: Int = 0,
    val volumeId: String = "",
    val name: String = "",
    val status: String = "",
    val totalSize: Long = 0,
    val usedSize: Long = 0,
    val availSize: Long = 0,
    val usagePercent: Int = 0,
    val fsType: String = "",
    val raidType: String = "",
    val poolPath: String = ""
) {
    val usage: Float
        get() = if (totalSize > 0) usedSize.toFloat() / totalSize else 0f
}

data class DiskInfoUi(
    val id: String = "",
    val numId: Int = 0,
    val name: String = "",
    val longName: String = "",
    val model: String = "",
    val vendor: String = "",
    val serial: String = "",
    val totalSize: Long = 0,
    val temperature: Int = 0,
    val status: String = "",
    val smartStatus: String = "",
    val overviewStatus: String = "",
    val health: String = "",
    val diskType: String = "",
    val isSsd: Boolean = false,
    val remainLife: Int = -1,
    val unc: Int = -1,
    val firm: String = "",
    val is4Kn: Boolean = false,
    val container: String = "",
    val usedBy: String = ""
)

data class StoragePoolUi(
    val id: String = "",
    val numId: Int = 0,
    val name: String = "",
    val status: String = "",
    val deviceType: String = "",
    val raidType: String = "",
    val totalSize: Long = 0,
    val usedSize: Long = 0,
    val diskIds: List<String> = emptyList(),
    val spares: List<String> = emptyList()
) {
    val usage: Float
        get() = if (totalSize > 0) usedSize.toFloat() / totalSize else 0f
}

data class SsdCacheUi(
    val id: String = "",
    val name: String = "",
    val status: String = "",
    val totalSize: Long = 0,
    val usedSize: Long = 0,
    val type: String = "",
    val readOnly: Boolean = false,
    val diskIds: List<String> = emptyList()
) {
    val usage: Float
        get() = if (totalSize > 0) usedSize.toFloat() / totalSize else 0f
}

data class HotSpareUi(
    val id: String = "",
    val name: String = "",
    val diskId: String = "",
    val status: String = "",
    val size: Long = 0
)

data class EnvUi(
    val bayNumber: Int = 0,
    val model: String = "",
    val temperature: Int = 0
)

data class StorageUiState(
    override val isLoading: Boolean = false,
    val volumes: List<VolumeInfoUi> = emptyList(),
    val disks: List<DiskInfoUi> = emptyList(),
    val storagePools: List<StoragePoolUi> = emptyList(),
    val ssdCaches: List<SsdCacheUi> = emptyList(),
    val hotSpares: List<HotSpareUi> = emptyList(),
    val env: EnvUi? = null,
    override val error: String? = null
) : wang.zengye.dsm.ui.base.BaseState

@HiltViewModel
class StorageViewModel @Inject constructor(
    private val systemRepository: SystemRepository
) : BaseViewModel<StorageUiState, StorageIntent, StorageEvent>() {

    companion object {
        private const val TAG = "StorageViewModel"
    }

    private val _uiState = MutableStateFlow(StorageUiState())
    override val state: StateFlow<StorageUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<StorageEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    init {
        sendIntent(StorageIntent.LoadStorage)
    }

    override suspend fun processIntent(intent: StorageIntent) {
        when (intent) {
            is StorageIntent.LoadStorage -> loadStorage()
        }
    }

    private suspend fun loadStorage() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        systemRepository.getStorageInfo()
            .onSuccess { result -> handleStorageSuccess(result.data) }
            .onFailure { error -> handleStorageError(error) }
    }

    private fun handleStorageSuccess(data: StorageData?) {
        val volumes = parseVolumes(data?.volumes)
        val disks = parseDisks(data?.disks)
        val storagePools = parseStoragePools(data?.storagePools)
        val ssdCaches = parseSsdCaches(data?.ssdCaches)
        val hotSpares = parseHotSpares(data?.hotSpares)
        val env = parseEnv(data?.env)

        _uiState.update {
            it.copy(
                volumes = volumes,
                disks = disks,
                storagePools = storagePools,
                ssdCaches = ssdCaches,
                hotSpares = hotSpares,
                env = env,
                isLoading = false
            )
        }
    }

    private suspend fun handleStorageError(error: Throwable) {
        Log.e(TAG, "Load storage error", error)
        _uiState.update { it.copy(error = error.message, isLoading = false) }
        _events.emit(StorageEvent.Error(error.message ?: "Failed to load storage"))
    }

    private fun parseVolumes(volumes: List<VolumeInfo>?): List<VolumeInfoUi> {
        return volumes?.map { volume ->
            VolumeInfoUi(
                id = volume.numId ?: 0,
                volumeId = volume.id ?: "",
                name = volume.displayName ?: volume.name ?: "",
                status = volume.status ?: "",
                totalSize = volume.getTotalSize(),
                usedSize = volume.getUsedSize(),
                availSize = volume.sizeAvail?.toLongOrNull() ?: 0,
                usagePercent = volume.usagePercent ?: 0,
                fsType = volume.fsType ?: "",
                raidType = volume.raidType ?: "",
                poolPath = volume.poolPath ?: ""
            )
        }?.sortedBy { it.id } ?: emptyList()
    }

    private fun parseDisks(disks: List<DiskInfo>?): List<DiskInfoUi> {
        return disks?.map { disk ->
            DiskInfoUi(
                id = disk.id ?: "",
                numId = disk.numId ?: 0,
                name = disk.name ?: "",
                longName = disk.longName ?: "",
                model = "${disk.vendor?.trim() ?: ""} ${disk.model?.trim() ?: ""}".trim(),
                vendor = disk.vendor?.trim() ?: "",
                serial = disk.serial ?: "",
                totalSize = disk.getSizeLong(),
                temperature = disk.temp ?: -1,
                status = disk.status ?: "",
                smartStatus = disk.smartStatus ?: "",
                overviewStatus = disk.overviewStatus ?: "",
                health = disk.health ?: "",
                diskType = disk.diskType ?: "",
                isSsd = disk.isSsd ?: false,
                remainLife = disk.remainLife ?: -1,
                unc = disk.unc ?: -1,
                firm = disk.firm ?: "",
                is4Kn = disk.is4Kn ?: false,
                container = disk.container?.str ?: "",
                usedBy = disk.usedBy ?: ""
            )
        }?.sortedBy { it.numId } ?: emptyList()
    }

    private fun parseStoragePools(pools: List<StoragePoolInfo>?): List<StoragePoolUi> {
        return pools?.map { pool ->
            StoragePoolUi(
                id = pool.id ?: "",
                numId = pool.numId ?: 0,
                name = pool.name ?: "",
                status = pool.status ?: "",
                deviceType = pool.deviceType ?: "",
                raidType = pool.raidType ?: "",
                totalSize = pool.getTotalSize(),
                usedSize = pool.getUsedSize(),
                diskIds = pool.disks ?: emptyList(),
                spares = pool.spares ?: emptyList()
            )
        }?.sortedByDescending { it.numId } ?: emptyList()
    }

    private fun parseSsdCaches(caches: List<SsdCacheInfo>?): List<SsdCacheUi> {
        return caches?.map { cache ->
            SsdCacheUi(
                id = cache.id ?: "",
                name = cache.name ?: "",
                status = cache.status ?: "",
                totalSize = cache.getTotalSize(),
                usedSize = cache.getUsedSize(),
                type = cache.type ?: "",
                readOnly = cache.readOnly ?: false,
                diskIds = cache.disks ?: emptyList()
            )
        } ?: emptyList()
    }

    private fun parseHotSpares(spares: List<HotSpareInfo>?): List<HotSpareUi> {
        return spares?.map { spare ->
            HotSpareUi(
                id = spare.id ?: "",
                name = spare.name ?: "",
                diskId = spare.diskId ?: "",
                status = spare.status ?: "",
                size = spare.size?.toLongOrNull() ?: 0
            )
        } ?: emptyList()
    }

    private fun parseEnv(env: EnvInfo?): EnvUi? {
        return env?.let {
            EnvUi(
                bayNumber = it.bayNumber?.toIntOrNull() ?: 0,
                model = it.model ?: "",
                temperature = it.temperature ?: 0
            )
        }
    }
}
