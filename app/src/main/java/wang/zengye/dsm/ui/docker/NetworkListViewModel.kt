package wang.zengye.dsm.ui.docker

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import wang.zengye.dsm.data.repository.DockerRepository
import wang.zengye.dsm.data.model.docker.DockerNetworkItem
import wang.zengye.dsm.ui.base.BaseViewModel
import javax.inject.Inject

/**
 * Docker网络信息
 */
data class DockerNetwork(
    val id: String = "",
    val name: String = "",
    val driver: String = "",
    val scope: String = "",
    val subnet: String = "",
    val gateway: String = "",
    val created: String = "",
    val ipv6Enabled: Boolean = false,
    val internal: Boolean = false,
    val attachable: Boolean = false,
    val containers: List<NetworkContainer> = emptyList()
)

/**
 * 网络中的容器
 */
data class NetworkContainer(
    val id: String = "",
    val name: String = "",
    val endpoint: String = "",
    val macAddress: String = ""
)

/**
 * Docker网络列表UI状态
 */
data class NetworkListUiState(
    override val isLoading: Boolean = false,
    val networks: List<DockerNetwork> = emptyList(),
    override val error: String? = null,
    val showCreateDialog: Boolean = false,
    val isCreating: Boolean = false,
    val createError: String? = null
) : wang.zengye.dsm.ui.base.BaseState

/**
 * Docker网络列表ViewModel
 */
@HiltViewModel
class NetworkListViewModel @Inject constructor(
    private val dockerRepository: DockerRepository
) : BaseViewModel<NetworkListUiState, NetworkListIntent, NetworkListEvent>() {

    private val _state = MutableStateFlow(NetworkListUiState())
    override val state: StateFlow<NetworkListUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<NetworkListEvent>(extraBufferCapacity = 10)
    override val events = _events.asSharedFlow()

    init {
        sendIntent(NetworkListIntent.LoadNetworks)
    }

    override suspend fun processIntent(intent: NetworkListIntent) {
        when (intent) {
            is NetworkListIntent.LoadNetworks -> loadNetworks()
            is NetworkListIntent.ShowCreateDialog -> showCreateDialog()
            is NetworkListIntent.HideCreateDialog -> hideCreateDialog()
            is NetworkListIntent.CreateNetwork -> createNetwork(intent.name, intent.driver, intent.subnet, intent.gateway)
            is NetworkListIntent.DeleteNetwork -> deleteNetwork(intent.networkId)
        }
    }

    /**
     * 加载网络列表
     */
    private suspend fun loadNetworks() {
        _state.update { it.copy(isLoading = true, error = null) }

        dockerRepository.getNetworks()
            .onSuccess { response ->
                val networks = parseNetworks(response)
                _state.update { it.copy(isLoading = false, networks = networks) }
            }
            .onFailure { exception ->
                _state.update { it.copy(isLoading = false, error = exception.message) }
                _events.emit(NetworkListEvent.ShowError(exception.message ?: "加载失败"))
            }
    }

    private fun parseNetworks(response: wang.zengye.dsm.data.model.docker.DockerNetworkListDto): List<DockerNetwork> {
        val data = response.data ?: return emptyList()
        val items = data.networks ?: return emptyList()

        return items.map { item -> parseNetworkItem(item) }
    }

    private fun parseNetworkItem(item: DockerNetworkItem): DockerNetwork {
        val ipConfig = item.ipam?.config
        val containersMap = item.containers ?: emptyMap()

        return DockerNetwork(
            id = item.id ?: "",
            name = item.name ?: "",
            driver = item.driver ?: "",
            scope = item.scope ?: "",
            subnet = ipConfig?.subnet ?: "",
            gateway = ipConfig?.gateway ?: "",
            created = item.created ?: "",
            ipv6Enabled = item.enableIpv6 ?: false,
            internal = item.internal ?: false,
            attachable = item.attachable ?: false,
            containers = parseNetworkContainers(containersMap)
        )
    }

    private fun parseNetworkContainers(containers: Map<String, wang.zengye.dsm.data.model.docker.DockerNetworkContainer>): List<NetworkContainer> {
        return containers.map { (id, container) ->
            NetworkContainer(
                id = id,
                name = container.name ?: "",
                endpoint = container.endpointId ?: "",
                macAddress = container.macAddress ?: ""
            )
        }
    }

    /**
     * 显示创建网络对话框
     */
    private fun showCreateDialog() {
        _state.update { it.copy(showCreateDialog = true, createError = null) }
    }

    /**
     * 隐藏创建网络对话框
     */
    private fun hideCreateDialog() {
        _state.update { it.copy(showCreateDialog = false, createError = null) }
    }

    /**
     * 创建网络
     */
    private suspend fun createNetwork(
        name: String,
        driver: String = "bridge",
        subnet: String = "",
        gateway: String = ""
    ) {
        _state.update { it.copy(isCreating = true, createError = null) }

        dockerRepository.createNetwork(name, driver, subnet, gateway)
            .onSuccess {
                _state.update { it.copy(isCreating = false, showCreateDialog = false) }
                _events.emit(NetworkListEvent.CreateSuccess)
                loadNetworks()
            }
            .onFailure { exception ->
                _state.update { it.copy(isCreating = false, createError = exception.message) }
                _events.emit(NetworkListEvent.ShowError(exception.message ?: "创建失败"))
            }
    }

    /**
     * 删除网络
     */
    private suspend fun deleteNetwork(networkId: String) {
        dockerRepository.deleteNetwork(networkId)
            .onSuccess {
                _events.emit(NetworkListEvent.DeleteSuccess)
                loadNetworks()
            }
            .onFailure { exception ->
                _state.update { it.copy(error = exception.message) }
                _events.emit(NetworkListEvent.ShowError(exception.message ?: "删除失败"))
            }
    }
}
