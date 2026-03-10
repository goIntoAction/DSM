package wang.zengye.dsm.ui.control_panel

import android.util.Log
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wang.zengye.dsm.data.repository.ControlPanelRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import javax.inject.Inject

data class ExternalDevice(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val vendor: String = "",
    val model: String = "",
    val size: Long = 0,
    val status: String = "",
    val mountPoint: String = "",
    val isUsb: Boolean = true,
    val isEjectable: Boolean = true
)

data class ExternalDevicesUiState(
    override val isLoading: Boolean = false,
    val devices: List<ExternalDevice> = emptyList(),
    override val error: String? = null
) : wang.zengye.dsm.ui.base.BaseState

@HiltViewModel
class ExternalDevicesViewModel @Inject constructor(
    private val controlPanelRepository: ControlPanelRepository
) : BaseViewModel<ExternalDevicesUiState, ExternalDevicesIntent, ExternalDevicesEvent>() {

    companion object {
        private const val TAG = "ExternalDevicesViewModel"
    }

    private val _state = MutableStateFlow(ExternalDevicesUiState())
    override val state: StateFlow<ExternalDevicesUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ExternalDevicesEvent>(extraBufferCapacity = 1)
    override val events = _events.asSharedFlow()

    init {
        sendIntent(ExternalDevicesIntent.LoadDevices)
    }

    override suspend fun processIntent(intent: ExternalDevicesIntent) {
        when (intent) {
            is ExternalDevicesIntent.LoadDevices -> loadDevices()
            is ExternalDevicesIntent.EjectDevice -> ejectDevice(intent.deviceId)
            is ExternalDevicesIntent.FormatDevice -> formatDevice(intent.deviceId)
        }
    }

    private suspend fun loadDevices() {
        _state.update { it.copy(isLoading = true, error = null) }

        controlPanelRepository.getExternalDevices()
            .onSuccess { response ->
                val devices = mutableListOf<ExternalDevice>()
                response.data?.result?.forEach { resultItem ->
                    try {
                        val api = resultItem.api ?: ""
                        val data = resultItem.data
                        val isUsb = api.contains("USB")

                        data?.devices?.forEach { deviceData ->
                            devices.add(ExternalDevice(
                                id = deviceData.devId ?: "",
                                name = deviceData.devName ?: "",
                                type = deviceData.devType ?: "",
                                vendor = deviceData.vendor ?: "",
                                model = deviceData.model ?: "",
                                size = deviceData.size ?: 0,
                                status = deviceData.status ?: "",
                                mountPoint = deviceData.mountPoint ?: "",
                                isUsb = isUsb,
                                isEjectable = deviceData.ejectable ?: true
                            ))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing result item", e)
                    }
                }

                _state.update {
                    it.copy(
                        devices = devices,
                        isLoading = false
                    )
                }
            }
            .onFailure { exception ->
                _state.update { it.copy(error = exception.message, isLoading = false) }
            }
    }

    private suspend fun ejectDevice(deviceId: String) {
        controlPanelRepository.ejectDevice(deviceId)
        _events.emit(ExternalDevicesEvent.EjectSuccess)
        loadDevices()
    }

    private suspend fun formatDevice(deviceId: String) {
        controlPanelRepository.formatDevice(deviceId)
        loadDevices()
    }
}