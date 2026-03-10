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

data class UserInfo(
    val name: String = "",
    val description: String = "",
    val email: String = "",
    val uid: Int = 0,
    val gid: Int = 0,
    val expired: Boolean = false
)

data class UsersUiState(
    override val isLoading: Boolean = false,
    val users: List<UserInfo> = emptyList(),
    override val error: String? = null,
    val operationSuccess: String? = null,
    val isOperating: Boolean = false
) : wang.zengye.dsm.ui.base.BaseState

// 迁移状态：[已完成]
// 说明：已完全迁移到 Moshi API
@HiltViewModel
class UsersViewModel @Inject constructor(
    private val controlPanelRepository: ControlPanelRepository
) : BaseViewModel<UsersUiState, UsersIntent, UsersEvent>() {

    private val _uiState = MutableStateFlow(UsersUiState())
    override val state: StateFlow<UsersUiState> = _uiState.asStateFlow()

    /**
     * 暴露给 UI 的 state 属性别名，保持与 Screen 兼容
     */
    val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<UsersEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    init {
        sendIntent(UsersIntent.LoadUsers)
    }

    override suspend fun processIntent(intent: UsersIntent) {
        when (intent) {
            is UsersIntent.LoadUsers -> loadUsers()
            is UsersIntent.CreateUser -> createUser(intent)
            is UsersIntent.UpdateUser -> updateUser(intent)
            is UsersIntent.DeleteUser -> deleteUser(intent)
        }
    }

    private suspend fun loadUsers() {
        _uiState.update { it.copy(isLoading = true, error = null, operationSuccess = null) }

        controlPanelRepository.getUsers()
            .onSuccess { response ->
                val users = response.data?.users?.map { user ->
                    UserInfo(
                        name = user.name ?: "",
                        description = user.additional?.description ?: "",
                        email = user.additional?.email ?: "",
                        uid = user.uid ?: 0,
                        gid = 0,
                        expired = user.additional?.expired?.toBoolean() ?: false
                    )
                } ?: emptyList()

                _uiState.update {
                    it.copy(
                        users = users.sortedBy { it.name.lowercase() },
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

    private suspend fun createUser(intent: UsersIntent.CreateUser) {
        _uiState.update { it.copy(isOperating = true) }

        controlPanelRepository.createUser(
            intent.username,
            intent.password,
            intent.description,
            intent.email
        )
            .onSuccess {
                _uiState.update {
                    it.copy(
                        isOperating = false,
                        operationSuccess = "users_create_success"
                    )
                }
                loadUsers()
                _events.emit(UsersEvent.CreateUserSuccess)
            }
            .onFailure { exception ->
                _uiState.update { it.copy(isOperating = false) }
                _events.emit(UsersEvent.ShowError(exception.message ?: "users_create_failed"))
            }
    }

    private suspend fun updateUser(intent: UsersIntent.UpdateUser) {
        _uiState.update { it.copy(isOperating = true) }

        controlPanelRepository.updateUser(
            intent.username,
            intent.description,
            intent.email,
            null,
            intent.newPassword
        )
            .onSuccess {
                _uiState.update {
                    it.copy(
                        isOperating = false,
                        operationSuccess = "users_update_success"
                    )
                }
                loadUsers()
                _events.emit(UsersEvent.UpdateUserSuccess)
            }
            .onFailure { exception ->
                _uiState.update { it.copy(isOperating = false) }
                _events.emit(UsersEvent.ShowError(exception.message ?: "users_update_failed"))
            }
    }

    private suspend fun deleteUser(intent: UsersIntent.DeleteUser) {
        _uiState.update { it.copy(isOperating = true) }

        controlPanelRepository.deleteUser(intent.username)
            .onSuccess {
                _uiState.update {
                    it.copy(
                        isOperating = false,
                        operationSuccess = "users_delete_success"
                    )
                }
                loadUsers()
                _events.emit(UsersEvent.DeleteUserSuccess)
            }
            .onFailure { exception ->
                _uiState.update { it.copy(isOperating = false) }
                _events.emit(UsersEvent.ShowError(exception.message ?: "users_delete_failed"))
            }
    }
}
