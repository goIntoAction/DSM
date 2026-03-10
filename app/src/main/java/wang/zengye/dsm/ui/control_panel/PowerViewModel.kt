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

data class UpsInfo(
    val connected: Boolean = false,
    val model: String = "",
    val status: String = "",
    val batteryCharge: Int = 0,
    val load: Int = 0,
    val runtime: Int = 0
)

data class PowerSchedule(
    val id: Int = 0,
    val type: String = "",
    val hour: Int = 0,
    val minute: Int = 0,
    val enabled: Boolean = false,
    val days: List<Int> = emptyList()
)

data class PowerUiState(
    override val isLoading: Boolean = false,
    val autoPowerOn: Boolean = false,
    val beepOnAlert: Boolean = false,
    val schedules: List<PowerSchedule> = emptyList(),
    val upsInfo: UpsInfo? = null,
    override val error: String? = null
) : wang.zengye.dsm.ui.base.BaseState

@HiltViewModel
class PowerViewModel @Inject constructor(
    private val controlPanelRepository: ControlPanelRepository
) : BaseViewModel<PowerUiState, PowerIntent, PowerEvent>() {

    companion object {
        private const val TAG = "PowerViewModel"
    }

    private val _state = MutableStateFlow(PowerUiState())
    override val state: StateFlow<PowerUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PowerEvent>(extraBufferCapacity = 1)
    override val events = _events.asSharedFlow()

    init {
        sendIntent(PowerIntent.LoadPowerSettings)
    }

    override suspend fun processIntent(intent: PowerIntent) {
        when (intent) {
            is PowerIntent.LoadPowerSettings -> loadPowerSettings()
            is PowerIntent.SetAutoPowerOn -> setAutoPowerOn(intent.enabled)
            is PowerIntent.SetBeepOnAlert -> setBeepOnAlert(intent.enabled)
            is PowerIntent.Shutdown -> shutdown()
            is PowerIntent.Reboot -> reboot()
        }
    }

    private suspend fun loadPowerSettings() {
        _state.update { it.copy(isLoading = true, error = null) }

        controlPanelRepository.getHardwarePower()
            .onSuccess { response ->
                val ups = response.upsInfo?.let {
                    UpsInfo(
                        connected = it.connected,
                        model = it.model,
                        status = if (it.connected) "正常" else "未连接",
                        batteryCharge = it.batteryCharge,
                        load = 0,
                        runtime = 0
                    )
                }
                
                val schedules = response.schedules.map { item ->
                    PowerSchedule(
                        id = item.id,
                        type = item.type,
                        hour = item.hour,
                        minute = item.minute,
                        enabled = item.enabled,
                        days = item.days
                    )
                }

                _state.update {
                    it.copy(
                        autoPowerOn = response.autoPowerOn,
                        beepOnAlert = response.beepOnAlert,
                        schedules = schedules,
                        upsInfo = ups,
                        isLoading = false
                    )
                }
            }
            .onFailure { exception ->
                _state.update { it.copy(error = exception.message, isLoading = false) }
            }
    }

    private suspend fun setAutoPowerOn(enabled: Boolean) {
        controlPanelRepository.setAutoPowerOn(enabled)
        _state.update { it.copy(autoPowerOn = enabled) }
    }

    private suspend fun setBeepOnAlert(enabled: Boolean) {
        controlPanelRepository.setBeepOnAlert(enabled)
        _state.update { it.copy(beepOnAlert = enabled) }
    }

    private suspend fun shutdown() {
        controlPanelRepository.shutdown()
            .onSuccess { _events.emit(PowerEvent.ShutdownSuccess) }
            .onFailure { _events.emit(PowerEvent.ShowError(it.message ?: "关机失败")) }
    }

    private suspend fun reboot() {
        controlPanelRepository.reboot()
            .onSuccess { _events.emit(PowerEvent.RebootSuccess) }
            .onFailure { _events.emit(PowerEvent.ShowError(it.message ?: "重启失败")) }
    }
}