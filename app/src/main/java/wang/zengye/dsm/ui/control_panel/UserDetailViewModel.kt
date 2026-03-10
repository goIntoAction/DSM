package wang.zengye.dsm.ui.control_panel

import android.util.Log
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

/**
 * 用户群组信息
 */
data class UserGroupInfo(
    val name: String,
    val description: String,
    val isMember: Boolean
)

/**
 * 用户权限信息
 */
data class UserPermissionInfo(
    val name: String,
    val description: String,
    val isReadonly: Boolean,
    val isWritable: Boolean,
    val isDeny: Boolean,
    val isCustom: Boolean
)

/**
 * 用户详情状态
 */
data class UserDetailUiState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    // 基本信息
    val username: String = "",
    val description: String = "",
    val email: String = "",
    val uid: Int = 0,
    val expired: String = "normal",
    val cannotChangePassword: Boolean = false,
    val passwordNeverExpire: Boolean = false,
    // 密码
    val newPassword: String = "",
    val confirmPassword: String = "",
    // 群组
    val groups: List<UserGroupInfo> = emptyList(),
    val groupsLoading: Boolean = false,
    // 权限
    val permissions: List<UserPermissionInfo> = emptyList(),
    val permissionsLoading: Boolean = false,
    // 保存中
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val saveError: String? = null
) : wang.zengye.dsm.ui.base.BaseState

// 迁移状态：[已完成]
// 说明：已完全迁移到 Moshi API
@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val repository: ControlPanelRepository
) : BaseViewModel<UserDetailUiState, UserDetailIntent, UserDetailEvent>() {

    companion object {
        private const val TAG = "UserDetailViewModel"
    }

    private val _uiState = MutableStateFlow(UserDetailUiState())
    override val state: StateFlow<UserDetailUiState> = _uiState.asStateFlow()

    /**
     * 暴露给 UI 的 state 属性别名，保持与 Screen 兼容
     */
    val uiState: StateFlow<UserDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<UserDetailEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    override suspend fun processIntent(intent: UserDetailIntent) {
        when (intent) {
            is UserDetailIntent.LoadUserDetail -> loadUserDetail(intent.username)
            is UserDetailIntent.LoadUserGroups -> loadUserGroups(intent.username)
            is UserDetailIntent.UpdateDescription -> updateDescription(intent.description)
            is UserDetailIntent.UpdateEmail -> updateEmail(intent.email)
            is UserDetailIntent.UpdateNewPassword -> updateNewPassword(intent.password)
            is UserDetailIntent.UpdateConfirmPassword -> updateConfirmPassword(intent.password)
            is UserDetailIntent.ToggleCannotChangePassword -> toggleCannotChangePassword()
            is UserDetailIntent.TogglePasswordNeverExpire -> togglePasswordNeverExpire()
            is UserDetailIntent.ToggleGroup -> toggleGroup(intent.groupName)
            is UserDetailIntent.Save -> save(intent.username)
        }
    }

    private suspend fun loadUserDetail(username: String) {
        _uiState.update { it.copy(isLoading = true, error = null, username = username) }

        repository.getUserDetail(username)
            .onSuccess { response ->
                val userData = response.data?.users?.firstOrNull()
                val additional = userData?.additional

                _uiState.update {
                    it.copy(
                        username = userData?.name ?: username,
                        description = additional?.description ?: "",
                        email = additional?.email ?: "",
                        uid = userData?.uid ?: 0,
                        expired = additional?.expired ?: "normal",
                        cannotChangePassword = additional?.cannotChgPasswd ?: false,
                        passwordNeverExpire = additional?.passwdNeverExpire ?: false,
                        isLoading = false
                    )
                }
                // 加载群组
                sendIntent(UserDetailIntent.LoadUserGroups(username))
            }
            .onFailure { exception ->
                Log.e(TAG, "Load user detail failed", exception)
                _uiState.update { it.copy(error = exception.message, isLoading = false) }
            }
    }

    private suspend fun loadUserGroups(username: String) {
        _uiState.update { it.copy(groupsLoading = true) }

        repository.getUserGroups(username)
            .onSuccess { response ->
                val groups = response.data?.groups?.map { group ->
                    UserGroupInfo(
                        name = group.name ?: "",
                        description = group.description ?: "",
                        isMember = group.isMember ?: false
                    )
                } ?: emptyList()

                _uiState.update {
                    it.copy(groups = groups, groupsLoading = false)
                }
            }
            .onFailure { exception ->
                Log.e(TAG, "Load user groups failed", exception)
                _uiState.update { it.copy(groupsLoading = false) }
            }
    }

    private fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    private fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    private fun updateNewPassword(password: String) {
        _uiState.update { it.copy(newPassword = password) }
    }

    private fun updateConfirmPassword(password: String) {
        _uiState.update { it.copy(confirmPassword = password) }
    }

    private fun toggleCannotChangePassword() {
        _uiState.update { it.copy(cannotChangePassword = !it.cannotChangePassword) }
    }

    private fun togglePasswordNeverExpire() {
        _uiState.update { it.copy(passwordNeverExpire = !it.passwordNeverExpire) }
    }

    private fun toggleGroup(groupName: String) {
        _uiState.update { state ->
            val updatedGroups = state.groups.map { group ->
                if (group.name == groupName) {
                    group.copy(isMember = !group.isMember)
                } else {
                    group
                }
            }
            state.copy(groups = updatedGroups)
        }
    }

    private suspend fun save(username: String) {
        _uiState.update { it.copy(isSaving = true, saveError = null) }

        val state = _uiState.value
        val newPassword = state.newPassword.ifEmpty { null }

        repository.saveUser(
            username = username,
            description = state.description,
            email = state.email,
            password = newPassword
        )
            .onSuccess {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true,
                        newPassword = "",
                        confirmPassword = ""
                    )
                }
                _events.emit(UserDetailEvent.SaveSuccess)
            }
            .onFailure { exception ->
                val message = exception.message ?: "Save failed"
                _uiState.update { it.copy(isSaving = false, saveError = message) }
                _events.emit(UserDetailEvent.ShowError(message))
            }
    }
}
