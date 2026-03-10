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

data class ScheduledTask(
    val id: Int = 0,
    val name: String = "",
    val type: String = "",
    val schedule: String = "",
    val enabled: Boolean = false,
    val lastRun: Long = 0,
    val nextRun: Long = 0,
    val status: String = ""
)

data class TaskSchedulerUiState(
    override val isLoading: Boolean = false,
    val tasks: List<ScheduledTask> = emptyList(),
    override val error: String? = null
) : wang.zengye.dsm.ui.base.BaseState

@HiltViewModel
class TaskSchedulerViewModel @Inject constructor(
    private val controlPanelRepository: ControlPanelRepository
) : BaseViewModel<TaskSchedulerUiState, TaskSchedulerIntent, TaskSchedulerEvent>() {

    private val _state = MutableStateFlow(TaskSchedulerUiState())
    override val state: StateFlow<TaskSchedulerUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<TaskSchedulerEvent>(extraBufferCapacity = 1)
    override val events = _events.asSharedFlow()

    init {
        sendIntent(TaskSchedulerIntent.LoadTasks)
    }

    override suspend fun processIntent(intent: TaskSchedulerIntent) {
        when (intent) {
            is TaskSchedulerIntent.LoadTasks -> loadTasks()
            is TaskSchedulerIntent.RunTask -> runTask(intent.taskId)
            is TaskSchedulerIntent.ToggleTask -> toggleTask(intent.taskId, intent.enabled)
            is TaskSchedulerIntent.DeleteTask -> deleteTask(intent.taskId)
        }
    }

    private suspend fun loadTasks() {
        _state.update { it.copy(isLoading = true, error = null) }

        controlPanelRepository.getTaskScheduler()
            .onSuccess { response ->
                val tasks = response.data?.tasks?.map { task ->
                    ScheduledTask(
                        id = task.id ?: 0,
                        name = task.name ?: "",
                        type = task.type ?: "",
                        schedule = task.schedule ?: "",
                        enabled = task.enable ?: false,
                        lastRun = task.lastTriggerTime ?: 0,
                        nextRun = task.nextTriggerTime ?: 0,
                        status = task.status ?: ""
                    )
                } ?: emptyList()

                _state.update {
                    it.copy(
                        tasks = tasks.sortedBy { it.nextRun },
                        isLoading = false
                    )
                }
            }
            .onFailure { exception ->
                _state.update { it.copy(error = exception.message, isLoading = false) }
            }
    }

    private suspend fun runTask(taskId: Int) {
        controlPanelRepository.runTask(taskId)
        _events.emit(TaskSchedulerEvent.RunSuccess)
        loadTasks()
    }

    private suspend fun toggleTask(taskId: Int, enabled: Boolean) {
        controlPanelRepository.setTaskEnabled(taskId, enabled)
        _state.update { state ->
            state.copy(
                tasks = state.tasks.map {
                    if (it.id == taskId) it.copy(enabled = enabled) else it
                }
            )
        }
    }

    private suspend fun deleteTask(taskId: Int) {
        controlPanelRepository.deleteTask(taskId)
            .onSuccess {
                _events.emit(TaskSchedulerEvent.DeleteSuccess)
                loadTasks()
            }
            .onFailure { _events.emit(TaskSchedulerEvent.ShowError(it.message ?: "删除失败")) }
    }
}