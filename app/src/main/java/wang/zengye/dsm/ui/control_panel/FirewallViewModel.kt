package wang.zengye.dsm.ui.control_panel

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

data class FirewallRule(
    val id: Int = 0,
    val name: String = "",
    val action: String = "",
    val protocol: String = "",
    val srcIp: String = "",
    val srcPort: String = "",
    val dstPort: String = "",
    val enabled: Boolean = false,
    val order: Int = 0
)

data class FirewallUiState(
    override val isLoading: Boolean = false,
    val enabled: Boolean = false,
    val rules: List<FirewallRule> = emptyList(),
    override val error: String? = null,
    val showAddDialog: Boolean = false
) : wang.zengye.dsm.ui.base.BaseState

@HiltViewModel
class FirewallViewModel @Inject constructor(
    private val controlPanelRepository: ControlPanelRepository
) : BaseViewModel<FirewallUiState, FirewallIntent, FirewallEvent>() {

    private val _state = MutableStateFlow(FirewallUiState())
    override val state: StateFlow<FirewallUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<FirewallEvent>(extraBufferCapacity = 1)
    override val events = _events.asSharedFlow()

    init {
        sendIntent(FirewallIntent.LoadFirewall)
    }

    override suspend fun processIntent(intent: FirewallIntent) {
        when (intent) {
            is FirewallIntent.LoadFirewall -> loadFirewall()
            is FirewallIntent.ToggleFirewall -> toggleFirewall(intent.enabled)
            is FirewallIntent.ToggleRule -> toggleRule(intent.ruleId, intent.enabled)
            is FirewallIntent.DeleteRule -> deleteRule(intent.ruleId)
            is FirewallIntent.ShowAddDialog -> showAddDialog()
            is FirewallIntent.HideAddDialog -> hideAddDialog()
        }
    }

    private suspend fun loadFirewall() {
        _state.update { it.copy(isLoading = true, error = null) }

        controlPanelRepository.getFirewall()
            .onSuccess { response ->
                val data = response.data
                val rules = data?.rules?.map { rule ->
                    FirewallRule(
                        id = rule.id ?: 0,
                        name = rule.name ?: "",
                        action = rule.action ?: "",
                        protocol = rule.protocol ?: "",
                        srcIp = rule.srcIp ?: "",
                        srcPort = rule.srcPort ?: "",
                        dstPort = rule.dstPort ?: "",
                        enabled = rule.enabled ?: false,
                        order = rule.order ?: 0
                    )
                } ?: emptyList()

                _state.update {
                    it.copy(
                        enabled = data?.enabled ?: false,
                        rules = rules.sortedBy { it.order },
                        isLoading = false
                    )
                }
            }
            .onFailure { exception ->
                _state.update { it.copy(error = exception.message, isLoading = false) }
            }
    }

    private suspend fun toggleFirewall(enabled: Boolean) {
        controlPanelRepository.setFirewallEnabled(enabled)
        _state.update { it.copy(enabled = enabled) }
    }

    private suspend fun toggleRule(ruleId: Int, enabled: Boolean) {
        controlPanelRepository.setFirewallRule(ruleId, enabled)
        _state.update { state ->
            state.copy(
                rules = state.rules.map {
                    if (it.id == ruleId) it.copy(enabled = enabled) else it
                }
            )
        }
    }

    private suspend fun deleteRule(ruleId: Int) {
        controlPanelRepository.deleteFirewallRule(ruleId)
            .onSuccess {
                _events.emit(FirewallEvent.DeleteSuccess)
                loadFirewall()
            }
            .onFailure { _events.emit(FirewallEvent.ShowError(it.message ?: "删除失败")) }
    }

    private fun showAddDialog() {
        _state.update { it.copy(showAddDialog = true) }
    }

    private fun hideAddDialog() {
        _state.update { it.copy(showAddDialog = false) }
    }
}