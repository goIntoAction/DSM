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

data class UserGroup(
    val name: String = "",
    val description: String = "",
    val gid: Int = 0,
    val members: List<String> = emptyList(),
    val isBuiltIn: Boolean = false
)

data class GroupsUiState(
    override val isLoading: Boolean = false,
    val groups: List<UserGroup> = emptyList(),
    override val error: String? = null,
    val showAddDialog: Boolean = false
) : wang.zengye.dsm.ui.base.BaseState

// 迁移状态：[部分完成]
// 说明：getGroups 已使用 Moshi，deleteGroup 仍使用旧 ApiResult
@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val controlPanelRepository: ControlPanelRepository
) : BaseViewModel<GroupsUiState, GroupsIntent, GroupsEvent>() {

    private val _uiState = MutableStateFlow(GroupsUiState())
    override val state: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    /**
     * 暴露给 UI 的 state 属性别名，保持与 Screen 兼容
     */
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<GroupsEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    init {
        sendIntent(GroupsIntent.LoadGroups)
    }

    override suspend fun processIntent(intent: GroupsIntent) {
        when (intent) {
            is GroupsIntent.LoadGroups -> loadGroups()
            is GroupsIntent.DeleteGroup -> deleteGroup(intent.name)
            is GroupsIntent.ShowAddDialog -> showAddDialog()
            is GroupsIntent.HideAddDialog -> hideAddDialog()
        }
    }

    private suspend fun loadGroups() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        controlPanelRepository.getGroups()
            .onSuccess { response ->
                val groups = response.data?.groups?.map { group ->
                    UserGroup(
                        name = group.name ?: "",
                        description = group.description ?: "",
                        gid = group.gid ?: 0,
                        members = group.members ?: emptyList(),
                        isBuiltIn = group.isBuiltIn ?: false
                    )
                } ?: emptyList()

                _uiState.update {
                    it.copy(
                        groups = groups.sortedWith(compareByDescending<UserGroup> { it.isBuiltIn }.thenBy { it.name }),
                        isLoading = false
                    )
                }
            }
            .onFailure { exception ->
                _uiState.update {
                    it.copy(error = exception.message, isLoading = false)
                }
            }
    }

    private suspend fun deleteGroup(name: String) {
        controlPanelRepository.deleteGroup(name)
            .onSuccess {
                loadGroups()
                _events.emit(GroupsEvent.DeleteSuccess)
            }
            .onFailure { _events.emit(GroupsEvent.ShowError(it.message ?: "删除失败")) }
    }

    private fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    private fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }
}