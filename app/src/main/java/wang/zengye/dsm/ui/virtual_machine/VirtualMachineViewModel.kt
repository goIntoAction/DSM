package wang.zengye.dsm.ui.virtual_machine

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
import wang.zengye.dsm.data.model.virtual_machine.VmItemDto
import wang.zengye.dsm.data.repository.VirtualMachineRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import javax.inject.Inject

/**
 * 虚拟机数据
 */
data class VmItem(
    val guestId: String = "",
    val name: String = "",
    val status: String = "", // running, stopped, suspended, etc.
    val vcpu: Int = 0,
    val memory: Long = 0,
    val diskSize: Long = 0,
    val network: String = "",
    val autoStart: Boolean = false
) {
    val isRunning: Boolean get() = status == "running"
    val isStopped: Boolean get() = status == "stopped"
    val isSuspended: Boolean get() = status == "suspended"
}

/**
 * 虚拟机管理UI状态
 */
data class VirtualMachineUiState(
    override val isLoading: Boolean = false,
    val vms: List<VmItem> = emptyList(),
    override val error: String? = null,
    val operatingVmId: String? = null,
    val operationType: String = ""
) : wang.zengye.dsm.ui.base.BaseState

/**
 * 虚拟机管理 ViewModel
 * 使用 Retrofit + Moshi API
 */
@HiltViewModel
class VirtualMachineViewModel @Inject constructor(
    private val repository: VirtualMachineRepository
) : BaseViewModel<VirtualMachineUiState, VirtualMachineIntent, VirtualMachineEvent>() {

    private val _uiState = MutableStateFlow(VirtualMachineUiState())
    override val state: StateFlow<VirtualMachineUiState> = _uiState.asStateFlow()

    /**
     * 暴露给 UI 的 state 属性别名，保持与 Screen 兼容
     */
    val uiState: StateFlow<VirtualMachineUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<VirtualMachineEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    init {
        sendIntent(VirtualMachineIntent.LoadVms)
    }

    override suspend fun processIntent(intent: VirtualMachineIntent) {
        when (intent) {
            is VirtualMachineIntent.LoadVms -> loadVms()
            is VirtualMachineIntent.StartVm -> startVm(intent.vm)
            is VirtualMachineIntent.StopVm -> stopVm(intent.vm, intent.force)
            is VirtualMachineIntent.RestartVm -> restartVm(intent.vm)
        }
    }

    /**
     * 加载虚拟机列表
     */
    private suspend fun loadVms() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        repository.getVmList().fold(
            onSuccess = { response ->
                val vms = response.data?.guests?.map { it.toVmItem() } ?: emptyList()
                _uiState.update { it.copy(isLoading = false, vms = vms) }
            },
            onFailure = { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
                _events.emit(VirtualMachineEvent.ShowError(error.message ?: "加载失败"))
            }
        )
    }

    private fun VmItemDto.toVmItem(): VmItem {
        return VmItem(
            guestId = guestId ?: "",
            name = name ?: "",
            status = status ?: "",
            vcpu = vcpuNum ?: 0,
            memory = memory ?: 0,
            diskSize = disks?.sumOf { it.size ?: 0L } ?: 0,
            network = networks?.firstOrNull()?.network ?: "",
            autoStart = autostart ?: false
        )
    }

    /**
     * 启动虚拟机
     */
    private suspend fun startVm(vm: VmItem) {
        _uiState.update { it.copy(operatingVmId = vm.guestId, operationType = "start") }

        repository.startVm(vm.guestId).fold(
            onSuccess = {
                _uiState.update { it.copy(operatingVmId = null) }
                sendIntent(VirtualMachineIntent.LoadVms)
            },
            onFailure = { error ->
                _uiState.update { it.copy(operatingVmId = null, error = error.message) }
                _events.emit(VirtualMachineEvent.ShowError(error.message ?: "启动失败"))
            }
        )
    }

    /**
     * 停止虚拟机
     */
    private suspend fun stopVm(vm: VmItem, force: Boolean = false) {
        _uiState.update { it.copy(operatingVmId = vm.guestId, operationType = "stop") }

        val result = if (force) {
            repository.forceStopVm(vm.guestId)
        } else {
            repository.shutdownVm(vm.guestId)
        }

        result.fold(
            onSuccess = {
                _uiState.update { it.copy(operatingVmId = null) }
                sendIntent(VirtualMachineIntent.LoadVms)
            },
            onFailure = { error ->
                _uiState.update { it.copy(operatingVmId = null, error = error.message) }
                _events.emit(VirtualMachineEvent.ShowError(error.message ?: "停止失败"))
            }
        )
    }

    /**
     * 重启虚拟机
     */
    private suspend fun restartVm(vm: VmItem) {
        _uiState.update { it.copy(operatingVmId = vm.guestId, operationType = "restart") }

        repository.resetVm(vm.guestId).fold(
            onSuccess = {
                _uiState.update { it.copy(operatingVmId = null) }
                sendIntent(VirtualMachineIntent.LoadVms)
            },
            onFailure = { error ->
                _uiState.update { it.copy(operatingVmId = null, error = error.message) }
                _events.emit(VirtualMachineEvent.ShowError(error.message ?: "重启失败"))
            }
        )
    }
}