package wang.zengye.dsm.ui.control_panel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import wang.zengye.dsm.data.repository.ControlPanelRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import javax.inject.Inject

data class NetworkInterface(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val ip: String = "",
    val mask: String = "",
    val mac: String = "",
    val status: String = "",
    val speed: String = "",
    val isUp: Boolean = false
)

data class NetworkUiState(
    override val isLoading: Boolean = false,
    val interfaces: List<NetworkInterface> = emptyList(),
    val gateway: String = "",
    val dns: List<String> = emptyList(),
    val hostname: String = "",
    override val error: String? = null
) : wang.zengye.dsm.ui.base.BaseState

@HiltViewModel
class NetworkViewModel @Inject constructor(
    private val controlPanelRepository: ControlPanelRepository
) : BaseViewModel<NetworkUiState, NetworkIntent, NetworkEvent>() {

    private val _state = MutableStateFlow(NetworkUiState())
    override val state: StateFlow<NetworkUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<NetworkEvent>(extraBufferCapacity = 1)
    override val events = _events.asSharedFlow()

    init {
        sendIntent(NetworkIntent.LoadNetwork)
    }

    override suspend fun processIntent(intent: NetworkIntent) {
        when (intent) {
            is NetworkIntent.LoadNetwork -> loadNetwork()
        }
    }

    private suspend fun loadNetwork() {
        _state.update { it.copy(isLoading = true, error = null) }

        controlPanelRepository.getNetworkInfo()
            .onSuccess { response ->
                val data = response.data
                val interfaces = data?.ifaces?.map { iface ->
                    NetworkInterface(
                        id = iface.id ?: "",
                        name = iface.name ?: "",
                        type = iface.type ?: "",
                        ip = iface.ip ?: "",
                        mask = iface.mask ?: "",
                        mac = iface.mac ?: "",
                        status = iface.status ?: "",
                        speed = iface.speed ?: "",
                        isUp = iface.up ?: false
                    )
                } ?: emptyList()

                _state.update {
                    it.copy(
                        interfaces = interfaces,
                        gateway = data?.gateway ?: "",
                        hostname = data?.hostname ?: "",
                        dns = data?.dns ?: emptyList(),
                        isLoading = false
                    )
                }
            }
            .onFailure { exception ->
                _state.update {
                    it.copy(error = exception.message, isLoading = false)
                }
            }
    }
}