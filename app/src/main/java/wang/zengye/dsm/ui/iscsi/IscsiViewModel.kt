package wang.zengye.dsm.ui.iscsi

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wang.zengye.dsm.data.model.iscsi.LunItemDto
import wang.zengye.dsm.data.model.iscsi.StoragePoolItemDto
import wang.zengye.dsm.data.model.iscsi.TargetItemDto
import wang.zengye.dsm.data.repository.IscsiRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import javax.inject.Inject

data class LunInfo(
    val id: Int = 0,
    val name: String = "",
    val location: String = "",
    val size: Long = 0,
    val usedSize: Long = 0,
    val status: String = "normal",
    val isThin: Boolean = false,
    val targetIds: List<Int> = emptyList()
)

data class TargetInfo(
    val id: Int = 0,
    val name: String = "",
    val iqn: String = "",
    val status: String = "",
    val enabled: Boolean = true,
    val connectedSessions: Int = 0,
    val mappedLunIds: List<Int> = emptyList()
)

data class StoragePoolInfo(
    val id: Int = 0,
    val name: String = "",
    val sizeTotal: Long = 0,
    val sizeFree: Long = 0,
    val raidType: String = "",
    val status: String = ""
)

data class IscsiUiState(
    override val isLoading: Boolean = false,
    val isOperating: Boolean = false,
    val currentTab: Int = 0,
    val luns: List<LunInfo> = emptyList(),
    val targets: List<TargetInfo> = emptyList(),
    val storagePools: List<StoragePoolInfo> = emptyList(),
    override val error: String? = null,
    val successMessage: String? = null,
    // 对话框状态
    val showCreateLunDialog: Boolean = false,
    val showCreateTargetDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val deleteTargetType: String = "", // "lun" or "target"
    val deleteTargetId: Int = 0
) : wang.zengye.dsm.ui.base.BaseState

@HiltViewModel
class IscsiViewModel @Inject constructor(
    private val repository: IscsiRepository
) : BaseViewModel<IscsiUiState, IscsiIntent, IscsiEvent>() {

    private val _uiState = MutableStateFlow(IscsiUiState())
    override val state: StateFlow<IscsiUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<IscsiEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    init {
        sendIntent(IscsiIntent.LoadData)
    }

    override suspend fun processIntent(intent: IscsiIntent) {
        when (intent) {
            is IscsiIntent.LoadData -> loadData()
            is IscsiIntent.SetTab -> setTab(intent.tab)

            // LUN 操作
            is IscsiIntent.CreateLun -> createLun(intent.name, intent.location, intent.size, intent.thinProvision)
            is IscsiIntent.DeleteLun -> deleteLun(intent.lunId)

            // Target 操作
            is IscsiIntent.CreateTarget -> createTarget(intent.name, intent.iqn, intent.mappedLunIds)
            is IscsiIntent.DeleteTarget -> deleteTarget(intent.targetId)
            is IscsiIntent.SetTargetEnabled -> setTargetEnabled(intent.targetId, intent.enabled)
            is IscsiIntent.MapLunToTarget -> mapLunToTarget(intent.targetId, intent.lunId)
            is IscsiIntent.UnmapLunFromTarget -> unmapLunFromTarget(intent.targetId, intent.lunId)

            // UI 状态
            is IscsiIntent.ShowCreateLunDialog -> _uiState.update { it.copy(showCreateLunDialog = intent.show) }
            is IscsiIntent.ShowCreateTargetDialog -> _uiState.update { it.copy(showCreateTargetDialog = intent.show) }
            is IscsiIntent.ShowDeleteConfirmDialog -> _uiState.update {
                it.copy(
                    showDeleteConfirmDialog = intent.show,
                    deleteTargetType = intent.type,
                    deleteTargetId = intent.id
                )
            }
        }
    }

    private fun setTab(tab: Int) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    private suspend fun loadData() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        try {
            val lunResult = fetchLuns()
            val targetResult = fetchTargets()
            val poolResult = fetchStoragePools()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    luns = lunResult,
                    targets = targetResult,
                    storagePools = poolResult
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = e.message) }
            _events.emit(IscsiEvent.ShowError(e.message ?: "加载失败"))
        }
    }

    private suspend fun fetchLuns(): List<LunInfo> {
        return repository.getLunList().fold(
            onSuccess = { response ->
                response.data?.luns?.map { it.toLunInfo() } ?: emptyList()
            },
            onFailure = { error ->
                _uiState.update { it.copy(error = error.message) }
                emptyList()
            }
        )
    }

    private suspend fun fetchTargets(): List<TargetInfo> {
        return repository.getTargetList().fold(
            onSuccess = { response ->
                response.data?.targets?.map { it.toTargetInfo() } ?: emptyList()
            },
            onFailure = { error ->
                _uiState.update { it.copy(error = error.message) }
                emptyList()
            }
        )
    }

    private suspend fun fetchStoragePools(): List<StoragePoolInfo> {
        return repository.getStoragePools().fold(
            onSuccess = { response ->
                response.data?.pools?.map { it.toStoragePoolInfo() } ?: emptyList()
            },
            onFailure = { emptyList() }
        )
    }

    // ==================== LUN 操作 ====================

    private suspend fun createLun(name: String, location: String, size: Long, thinProvision: Boolean) {
        _uiState.update { it.copy(isOperating = true, showCreateLunDialog = false) }
        repository.createLun(name, location, size, thinProvision).fold(
            onSuccess = {
                _uiState.update { it.copy(isOperating = false, successMessage = "LUN 创建成功") }
                _events.emit(IscsiEvent.ShowSuccess("LUN 创建成功"))
                loadData()
            },
            onFailure = { error ->
                _uiState.update { it.copy(isOperating = false, error = error.message) }
                _events.emit(IscsiEvent.ShowError(error.message ?: "创建失败"))
            }
        )
    }

    private suspend fun deleteLun(lunId: Int) {
        _uiState.update { it.copy(isOperating = true, showDeleteConfirmDialog = false) }
        repository.deleteLun(lunId).fold(
            onSuccess = {
                _uiState.update { it.copy(isOperating = false, successMessage = "LUN 删除成功") }
                _events.emit(IscsiEvent.ShowSuccess("LUN 删除成功"))
                loadData()
            },
            onFailure = { error ->
                _uiState.update { it.copy(isOperating = false, error = error.message) }
                _events.emit(IscsiEvent.ShowError(error.message ?: "删除失败"))
            }
        )
    }

    // ==================== Target 操作 ====================

    private suspend fun createTarget(name: String, iqn: String?, mappedLunIds: List<Int>) {
        _uiState.update { it.copy(isOperating = true, showCreateTargetDialog = false) }
        repository.createTarget(name, iqn, mappedLunIds).fold(
            onSuccess = {
                _uiState.update { it.copy(isOperating = false, successMessage = "Target 创建成功") }
                _events.emit(IscsiEvent.ShowSuccess("Target 创建成功"))
                loadData()
            },
            onFailure = { error ->
                _uiState.update { it.copy(isOperating = false, error = error.message) }
                _events.emit(IscsiEvent.ShowError(error.message ?: "创建失败"))
            }
        )
    }

    private suspend fun deleteTarget(targetId: Int) {
        _uiState.update { it.copy(isOperating = true, showDeleteConfirmDialog = false) }
        repository.deleteTarget(targetId).fold(
            onSuccess = {
                _uiState.update { it.copy(isOperating = false, successMessage = "Target 删除成功") }
                _events.emit(IscsiEvent.ShowSuccess("Target 删除成功"))
                loadData()
            },
            onFailure = { error ->
                _uiState.update { it.copy(isOperating = false, error = error.message) }
                _events.emit(IscsiEvent.ShowError(error.message ?: "删除失败"))
            }
        )
    }

    private suspend fun setTargetEnabled(targetId: Int, enabled: Boolean) {
        _uiState.update { it.copy(isOperating = true) }
        repository.setTargetEnabled(targetId, enabled).fold(
            onSuccess = {
                val action = if (enabled) "启用" else "禁用"
                _uiState.update { it.copy(isOperating = false, successMessage = "Target 已$action") }
                _events.emit(IscsiEvent.ShowSuccess("Target 已$action"))
                loadData()
            },
            onFailure = { error ->
                _uiState.update { it.copy(isOperating = false, error = error.message) }
                _events.emit(IscsiEvent.ShowError(error.message ?: "操作失败"))
            }
        )
    }

    private suspend fun mapLunToTarget(targetId: Int, lunId: Int) {
        _uiState.update { it.copy(isOperating = true) }
        repository.mapLunToTarget(targetId, lunId).fold(
            onSuccess = {
                _uiState.update { it.copy(isOperating = false, successMessage = "LUN 映射成功") }
                _events.emit(IscsiEvent.ShowSuccess("LUN 映射成功"))
                loadData()
            },
            onFailure = { error ->
                _uiState.update { it.copy(isOperating = false, error = error.message) }
                _events.emit(IscsiEvent.ShowError(error.message ?: "映射失败"))
            }
        )
    }

    private suspend fun unmapLunFromTarget(targetId: Int, lunId: Int) {
        _uiState.update { it.copy(isOperating = true) }
        repository.unmapLunFromTarget(targetId, lunId).fold(
            onSuccess = {
                _uiState.update { it.copy(isOperating = false, successMessage = "LUN 取消映射成功") }
                _events.emit(IscsiEvent.ShowSuccess("LUN 取消映射成功"))
                loadData()
            },
            onFailure = { error ->
                _uiState.update { it.copy(isOperating = false, error = error.message) }
                _events.emit(IscsiEvent.ShowError(error.message ?: "取消映射失败"))
            }
        )
    }

    // ==================== 数据转换 ====================

    private fun LunItemDto.toLunInfo(): LunInfo {
        return LunInfo(
            id = lunId ?: 0,
            name = name ?: "",
            location = location ?: "",
            size = size ?: 0,
            usedSize = usedSize ?: 0,
            status = status ?: "normal",
            isThin = isThinProvision ?: false
        )
    }

    private fun TargetItemDto.toTargetInfo(): TargetInfo {
        return TargetInfo(
            id = targetId ?: 0,
            name = name ?: "",
            iqn = iqn ?: "",
            status = status ?: "",
            enabled = enabled ?: true,
            connectedSessions = connectedSessions?.size ?: 0,
            mappedLunIds = mappedLunIds ?: emptyList()
        )
    }

    private fun StoragePoolItemDto.toStoragePoolInfo(): StoragePoolInfo {
        return StoragePoolInfo(
            id = poolId ?: 0,
            name = name ?: "",
            sizeTotal = sizeTotal ?: 0,
            sizeFree = sizeFree ?: 0,
            raidType = raidType ?: "",
            status = status ?: ""
        )
    }
}