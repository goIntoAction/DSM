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

data class TerminalSetting(
    val enabled: Boolean = false,
    val port: Int = 22,
    val allowRootLogin: Boolean = false,
    val sshKeyEnabled: Boolean = false,
    val maxConnections: Int = 10
)

data class TerminalSession(
    val id: String = "",
    val user: String = "",
    val ip: String = "",
    val startTime: Long = 0,
    val active: Boolean = false
)

data class TerminalUiState(
    override val isLoading: Boolean = false,
    val setting: TerminalSetting = TerminalSetting(),
    val sessions: List<TerminalSession> = emptyList(),
    override val error: String? = null,
    val showEditDialog: Boolean = false
) : wang.zengye.dsm.ui.base.BaseState

@HiltViewModel
class TerminalViewModel @Inject constructor(
    private val controlPanelRepository: ControlPanelRepository
) : BaseViewModel<TerminalUiState, TerminalIntent, TerminalEvent>() {

    companion object {
        private const val TAG = "TerminalViewModel"
    }

    private val _state = MutableStateFlow(TerminalUiState())
    override val state: StateFlow<TerminalUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<TerminalEvent>(extraBufferCapacity = 1)
    override val events = _events.asSharedFlow()

    init {
        sendIntent(TerminalIntent.LoadSettings)
    }

    override suspend fun processIntent(intent: TerminalIntent) {
        when (intent) {
            is TerminalIntent.LoadSettings -> loadSettings()
            is TerminalIntent.UpdateSettings -> updateSettings(intent.setting)
            is TerminalIntent.DisconnectSession -> disconnectSession(intent.sessionId)
            is TerminalIntent.ShowEditDialog -> _state.update { it.copy(showEditDialog = true) }
            is TerminalIntent.HideEditDialog -> _state.update { it.copy(showEditDialog = false) }
        }
    }

    private suspend fun loadSettings() {
        _state.update { it.copy(isLoading = true, error = null) }

        controlPanelRepository.getTerminalInfo()
            .onSuccess { response ->
                val data = response.data
                _state.update {
                    it.copy(
                        setting = TerminalSetting(
                            enabled = data?.enableSsh ?: false,
                            port = data?.sshPort ?: 22,
                            allowRootLogin = data?.allowRootLogin ?: false,
                            sshKeyEnabled = data?.sshKeyEnabled ?: false,
                            maxConnections = data?.maxConnections ?: 10
                        ),
                        sessions = data?.sessions?.map { session ->
                            TerminalSession(
                                id = session.id ?: "",
                                user = session.user ?: "",
                                ip = session.ip ?: "",
                                startTime = session.startTime ?: 0,
                                active = session.active ?: false
                            )
                        } ?: emptyList(),
                        isLoading = false
                    )
                }
            }
            .onFailure { exception ->
                _state.update { it.copy(error = exception.message, isLoading = false) }
            }
    }

    private suspend fun updateSettings(setting: TerminalSetting) {
        controlPanelRepository.updateTerminalSettings(setting.enabled, setting.port, setting.allowRootLogin)
            .onSuccess {
                _events.emit(TerminalEvent.UpdateSuccess)
                _state.update {
                    it.copy(
                        setting = setting,
                        showEditDialog = false
                    )
                }
            }
            .onFailure { _events.emit(TerminalEvent.ShowError(it.message ?: "更新失败")) }
    }

    private suspend fun disconnectSession(sessionId: String) {
        controlPanelRepository.disconnectTerminalSession(sessionId)
        _events.emit(TerminalEvent.DisconnectSuccess)
        loadSettings()
    }
}