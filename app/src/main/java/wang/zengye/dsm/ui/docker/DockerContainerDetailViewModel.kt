package wang.zengye.dsm.ui.docker

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
import wang.zengye.dsm.data.repository.DockerRepository
import wang.zengye.dsm.data.model.docker.DockerContainerDetailDto
import wang.zengye.dsm.data.model.docker.DockerContainerProcess
import wang.zengye.dsm.data.model.docker.DockerContainerLog
import wang.zengye.dsm.ui.base.BaseViewModel
import javax.inject.Inject

/**
 * 容器端口绑定
 */
data class PortBinding(
    val hostPort: String,
    val containerPort: String,
    val type: String
)

/**
 * 容器卷绑定
 */
data class VolumeBinding(
    val hostVolumeFile: String,
    val mountPoint: String,
    val type: String
)

/**
 * 容器链接
 */
data class ContainerLink(
    val linkContainer: String,
    val alias: String
)

/**
 * 容器网络信息
 */
data class ContainerNetworkInfo(
    val name: String,
    val driver: String
)

/**
 * 环境变量
 */
data class EnvironmentVariable(
    val key: String,
    val value: String
)

/**
 * 容器进程
 */
data class ContainerProcess(
    val pid: Int,
    val cpu: Double,
    val memory: Long,
    val command: String
)

/**
 * 容器日志
 */
data class ContainerLog(
    val stream: String,
    val created: String,
    val text: String
)

/**
 * 容器详情状态
 */
data class ContainerDetailUiState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    // 基本信息
    val status: String = "",
    val upTime: Long = 0,
    val command: String = "",
    val memoryLimit: Long = 0,
    val memoryPercent: Double = 0.0,
    val cpuPriority: Int = 0,
    val shortcutEnabled: Boolean = false,
    val shortcutUrl: String = "",
    // 端口、卷、链接等
    val ports: List<PortBinding> = emptyList(),
    val volumes: List<VolumeBinding> = emptyList(),
    val links: List<ContainerLink> = emptyList(),
    val networks: List<ContainerNetworkInfo> = emptyList(),
    val envVariables: List<EnvironmentVariable> = emptyList(),
    // 进程
    val processes: List<ContainerProcess> = emptyList(),
    val processesLoading: Boolean = false,
    // 日志
    val logDates: List<String> = emptyList(),
    val selectedLogDate: String = "",
    val logs: List<ContainerLog> = emptyList(),
    val logsLoading: Boolean = false
) : wang.zengye.dsm.ui.base.BaseState

@HiltViewModel
class DockerContainerDetailViewModel @Inject constructor(
    private val repository: DockerRepository
) : BaseViewModel<ContainerDetailUiState, DockerContainerDetailIntent, DockerContainerDetailEvent>() {

    companion object {
        private const val TAG = "ContainerDetailVM"
    }

    private val _state = MutableStateFlow(ContainerDetailUiState())
    override val state: StateFlow<ContainerDetailUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<DockerContainerDetailEvent>(extraBufferCapacity = 10)
    override val events = _events.asSharedFlow()

    private var containerName: String = ""

    override suspend fun processIntent(intent: DockerContainerDetailIntent) {
        when (intent) {
            is DockerContainerDetailIntent.LoadDetail -> loadDetail(intent.containerName)
            is DockerContainerDetailIntent.LoadLogs -> loadLogs(intent.date)
            is DockerContainerDetailIntent.Refresh -> refresh()
        }
    }

    private suspend fun loadDetail(name: String) {
        containerName = name
        _state.update { it.copy(isLoading = true, error = null) }

        repository.getContainerDetail(name)
            .onSuccess { response ->
                parseDetail(response)
            }
            .onFailure { exception ->
                _state.update { it.copy(error = exception.message, isLoading = false) }
                _events.emit(DockerContainerDetailEvent.ShowError(exception.message ?: "加载失败"))
            }
    }

    private suspend fun parseDetail(response: DockerContainerDetailDto) {
        try {
            val data = response.data ?: run {
                _state.update { it.copy(error = "No data", isLoading = false) }
                return
            }

            // 基本信息
            val details = data.details
            val profile = data.profile

            // 详情解析
            val status = details?.status ?: ""
            val upTime = details?.upTime ?: 0
            val command = details?.exeCmd ?: ""
            val memoryPercent = details?.memoryPercent ?: 0.0

            // 配置解析
            val memoryLimit = profile?.memoryLimit ?: 0
            val cpuPriority = profile?.cpuPriority ?: 0

            // 快捷方式
            val shortcut = profile?.shortcut
            val shortcutEnabled = shortcut?.enableShortcut ?: false
            val shortcutUrl = if (shortcut?.enableWebPage == true) {
                shortcut.webPageUrl ?: ""
            } else ""

            // 端口绑定
            val ports = profile?.portBindings?.map { binding ->
                PortBinding(
                    hostPort = binding.hostPort ?: "",
                    containerPort = binding.containerPort ?: "",
                    type = binding.type ?: ""
                )
            } ?: emptyList()

            // 卷绑定
            val volumes = profile?.volumeBindings?.map { binding ->
                VolumeBinding(
                    hostVolumeFile = binding.hostVolumeFile ?: "",
                    mountPoint = binding.mountPoint ?: "",
                    type = binding.type ?: ""
                )
            } ?: emptyList()

            // 链接
            val links = profile?.links?.map { link ->
                ContainerLink(
                    linkContainer = link.linkContainer ?: "",
                    alias = link.alias ?: ""
                )
            } ?: emptyList()

            // 网络
            val networks = profile?.network?.map { net ->
                ContainerNetworkInfo(
                    name = net.name ?: "",
                    driver = net.driver ?: ""
                )
            } ?: emptyList()

            // 环境变量
            val envVariables = profile?.envVariables?.map { env ->
                EnvironmentVariable(
                    key = env.key ?: "",
                    value = env.value ?: ""
                )
            } ?: emptyList()

            _state.update {
                it.copy(
                    isLoading = false,
                    status = status,
                    upTime = upTime,
                    command = command,
                    memoryLimit = memoryLimit,
                    memoryPercent = memoryPercent,
                    cpuPriority = cpuPriority,
                    shortcutEnabled = shortcutEnabled,
                    shortcutUrl = shortcutUrl,
                    ports = ports,
                    volumes = volumes,
                    links = links,
                    networks = networks,
                    envVariables = envVariables
                )
            }

            // 解析进程
            val processes = data.processes?.map { process ->
                ContainerProcess(
                    pid = process.pid ?: 0,
                    cpu = process.cpu ?: 0.0,
                    memory = process.memory ?: 0,
                    command = process.command ?: ""
                )
            } ?: emptyList()
            _state.update { it.copy(processes = processes, processesLoading = false) }

            // 加载日志日期
            loadLogDates()

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing container detail", e)
            _state.update { it.copy(error = e.message, isLoading = false) }
            _events.emit(DockerContainerDetailEvent.ShowError(e.message ?: "解析失败"))
        }
    }

    private suspend fun loadLogDates() {
        repository.getContainerLogList(containerName)
            .onSuccess { response ->
                try {
                    val dates = response.data?.dates ?: emptyList()
                    val selectedDate = dates.firstOrNull() ?: ""
                    _state.update { it.copy(logDates = dates, selectedLogDate = selectedDate) }
                    if (selectedDate.isNotEmpty()) {
                        loadLogs(selectedDate)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing log dates", e)
                }
            }
            .onFailure { exception ->
                Log.e(TAG, "Error loading log dates", exception)
            }
    }

    private suspend fun loadLogs(date: String) {
        _state.update { it.copy(logsLoading = true, selectedLogDate = date) }

        repository.getContainerLog(containerName, date)
            .onSuccess { response ->
                try {
                    val logs = response.data?.logs?.map { log ->
                        ContainerLog(
                            stream = log.stream ?: "",
                            created = log.created ?: "",
                            text = log.text ?: ""
                        )
                    } ?: emptyList()
                    _state.update { it.copy(logs = logs, logsLoading = false) }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing logs", e)
                    _state.update { it.copy(logsLoading = false) }
                    _events.emit(DockerContainerDetailEvent.ShowError(e.message ?: "日志加载失败"))
                }
            }
            .onFailure { exception ->
                Log.e(TAG, "Error loading logs", exception)
                _state.update { it.copy(logsLoading = false) }
                _events.emit(DockerContainerDetailEvent.ShowError(exception.message ?: "日志加载失败"))
            }
    }

    private suspend fun refresh() {
        if (containerName.isNotEmpty()) {
            loadDetail(containerName)
        }
    }
}
