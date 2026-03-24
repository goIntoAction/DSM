package wang.zengye.dsm.ui.docker

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
import wang.zengye.dsm.data.repository.DockerRepository
import wang.zengye.dsm.data.model.DockerContainer
import wang.zengye.dsm.data.model.ContainerNetwork
import wang.zengye.dsm.data.model.docker.DockerContainerItem
import wang.zengye.dsm.ui.base.BaseViewModel
import javax.inject.Inject

data class DockerUiState(
    override val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val containers: List<DockerContainer> = emptyList(),
    override val error: String? = null,
    val operatingContainer: String? = null,
    val operationError: String? = null,
    val operationSuccess: String? = null
) : wang.zengye.dsm.ui.base.BaseState

// 迁移状态：[已完成]
// 说明：已迁移到 MVI 架构，使用 Moshi API
@HiltViewModel
class DockerViewModel @Inject constructor(
    private val dockerRepository: DockerRepository
) : BaseViewModel<DockerUiState, DockerIntent, DockerEvent>() {

    private val _uiState = MutableStateFlow(DockerUiState())
    override val state: StateFlow<DockerUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DockerEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    init {
        sendIntent(DockerIntent.LoadContainers)
    }

    override suspend fun processIntent(intent: DockerIntent) {
        when (intent) {
            is DockerIntent.LoadContainers -> loadContainers()
            is DockerIntent.Refresh -> refresh()
            is DockerIntent.StartContainer -> startContainer(intent.name)
            is DockerIntent.StopContainer -> stopContainer(intent.name)
            is DockerIntent.RestartContainer -> restartContainer(intent.name)
            is DockerIntent.ClearOperationMessage -> clearOperationMessage()
        }
    }

    private suspend fun loadContainers() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        fetchContainers()
    }

    private suspend fun refresh() {
        _uiState.update { it.copy(isRefreshing = true, error = null) }
        fetchContainers()
    }

    private suspend fun fetchContainers() {
        dockerRepository.getContainerList()
            .onSuccess { response ->
                val containers = parseContainers(response)
                _uiState.update {
                    it.copy(
                        containers = containers,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            }
            .onFailure { exception ->
                _uiState.update {
                    it.copy(
                        error = exception.message,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            }
    }

    private fun parseContainers(response: wang.zengye.dsm.data.model.docker.DockerContainerListDto): List<DockerContainer> {
        val containers = mutableListOf<DockerContainer>()

        // 单次请求格式：data.containers 直接包含容器列表
        response.data?.containers?.forEach { item ->
            val container = parseContainerItem(item)
            containers.add(container)
        }

        // 批量请求格式：result 列表中每个元素的 data.containers
        response.result?.forEach { result ->
            if (result.success != true) return@forEach
            if (result.api != "SYNO.Docker.Container") return@forEach

            result.data?.containers?.forEach { item ->
                val container = parseContainerItem(item)
                containers.add(container)
            }
        }

        return containers
    }

    private fun parseContainerItem(item: DockerContainerItem): DockerContainer {
        val bridge = item.networkSettings?.bridge
        return DockerContainer(
            name = item.name ?: "",
            status = item.status ?: "",
            image = item.image ?: "",
            created = item.created ?: 0,
            networkSettings = bridge?.let {
                ContainerNetwork(
                    ipAddress = it.ipAddress ?: "",
                    gateway = it.gateway ?: "",
                    macAddress = it.macAddress ?: ""
                )
            }
        )
    }

    private suspend fun startContainer(name: String) {
        _uiState.update { it.copy(operatingContainer = name, operationError = null, operationSuccess = null) }

        dockerRepository.startContainer(name)
            .onSuccess {
                _uiState.update { it.copy(operationSuccess = "容器 $name 启动成功") }
                fetchContainers()
            }
            .onFailure { exception ->
                _uiState.update { it.copy(operationError = "启动失败: ${exception.message}") }
            }

        _uiState.update { it.copy(operatingContainer = null) }
    }

    private suspend fun stopContainer(name: String) {
        _uiState.update { it.copy(operatingContainer = name, operationError = null, operationSuccess = null) }

        dockerRepository.stopContainer(name)
            .onSuccess {
                _uiState.update { it.copy(operationSuccess = "容器 $name 停止成功") }
                fetchContainers()
            }
            .onFailure { exception ->
                _uiState.update { it.copy(operationError = "停止失败: ${exception.message}") }
            }

        _uiState.update { it.copy(operatingContainer = null) }
    }

    private suspend fun restartContainer(name: String) {
        _uiState.update { it.copy(operatingContainer = name, operationError = null, operationSuccess = null) }

        dockerRepository.restartContainer(name)
            .onSuccess {
                _uiState.update { it.copy(operationSuccess = "容器 $name 重启成功") }
                fetchContainers()
            }
            .onFailure { exception ->
                _uiState.update { it.copy(operationError = "重启失败: ${exception.message}") }
            }

        _uiState.update { it.copy(operatingContainer = null) }
    }

    private fun clearOperationMessage() {
        _uiState.update { it.copy(operationError = null, operationSuccess = null) }
    }
}