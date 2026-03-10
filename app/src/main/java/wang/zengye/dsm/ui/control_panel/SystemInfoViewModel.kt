package wang.zengye.dsm.ui.control_panel

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
import wang.zengye.dsm.data.repository.ControlPanelRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import javax.inject.Inject

data class SystemInfoDetails(
    val model: String = "",
    val serial: String = "",
    val hostname: String = "",
    val version: String = "",
    val build: String = "",
    val kernel: String = "",
    val cpuModel: String = "",
    val cpuCores: Int = 0,
    val totalMemory: Long = 0,
    val uptime: String = "",
    val temperature: Int = 0,
    val fanSpeed: Int = 0
)

data class SystemInfoUiState(
    override val isLoading: Boolean = false,
    val info: SystemInfoDetails = SystemInfoDetails(),
    override val error: String? = null
) : wang.zengye.dsm.ui.base.BaseState

// 迁移状态：[已完成]
// 说明：已迁移到 MVI 架构，使用 Moshi API
@HiltViewModel
class SystemInfoViewModel @Inject constructor(
    private val controlPanelRepository: ControlPanelRepository
) : BaseViewModel<SystemInfoUiState, SystemInfoIntent, SystemInfoEvent>() {

    companion object {
        private const val TAG = "SystemInfoViewModel"
    }

    private val _uiState = MutableStateFlow(SystemInfoUiState())
    override val state: StateFlow<SystemInfoUiState> = _uiState.asStateFlow()

    /**
     * 暴露给 UI 的 state 属性别名，保持与 Screen 兼容
     */
    val uiState: StateFlow<SystemInfoUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SystemInfoEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    init {
        sendIntent(SystemInfoIntent.LoadSystemInfo)
    }

    override suspend fun processIntent(intent: SystemInfoIntent) {
        when (intent) {
            is SystemInfoIntent.LoadSystemInfo -> loadSystemInfo()
        }
    }

    private suspend fun loadSystemInfo() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        val result = controlPanelRepository.getSystemInfoDetail()
        result.fold(
            onSuccess = { response ->
                val data = response.data
                _uiState.update {
                    it.copy(
                        info = SystemInfoDetails(
                            model = data?.model ?: "",
                            serial = data?.serial ?: "",
                            hostname = data?.hostname ?: "",
                            version = data?.firmwareVer ?: "",
                            build = data?.buildnumber ?: "",
                            kernel = data?.kernelVersion ?: "",
                            cpuModel = data?.cpuFamily ?: "",
                            cpuCores = data?.cpuCores?.toIntOrNull() ?: 0,
                            totalMemory = data?.ramSize ?: 0,
                            uptime = data?.upTime ?: "",
                            temperature = data?.sysTemp ?: 0,
                            fanSpeed = 0
                        ),
                        isLoading = false
                    )
                }
            },
            onFailure = { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        )
    }
}