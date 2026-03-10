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

data class ShareInfo(
    val name: String = "",
    val path: String = "",
    val desc: String = "",
    val volPath: String = "",
    val isHidden: Boolean = false,
    val hasRecycleBin: Boolean = false,
    val isEncrypted: Boolean = false,
    val isAclMode: Boolean = false,
    val hideUnreadable: Boolean = false,
    val recycleBinAdminOnly: Boolean = false,
    val enableShareCow: Boolean = false,
    val enableShareCompress: Boolean = false,
    val quotaValue: Long = 0,
    val quotaUsed: Long = 0
)

data class SharesUiState(
    override val isLoading: Boolean = false,
    val shares: List<ShareInfo> = emptyList(),
    override val error: String? = null,
    val isOperating: Boolean = false
) : wang.zengye.dsm.ui.base.BaseState

// 迁移状态：[已完成]
// 说明：已完全迁移到 Moshi API
@HiltViewModel
class SharedFoldersViewModel @Inject constructor(
    private val controlPanelRepository: ControlPanelRepository
) : BaseViewModel<SharesUiState, SharedFoldersIntent, SharedFoldersEvent>() {

    private val _state = MutableStateFlow(SharesUiState())
    override val state: StateFlow<SharesUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SharedFoldersEvent>(extraBufferCapacity = 1)
    override val events = _events.asSharedFlow()

    init {
        sendIntent(SharedFoldersIntent.LoadShares)
    }

    override suspend fun processIntent(intent: SharedFoldersIntent) {
        when (intent) {
            is SharedFoldersIntent.LoadShares -> loadShares()
            is SharedFoldersIntent.DeleteShare -> deleteShare(intent.name)
            is SharedFoldersIntent.CleanRecycleBin -> cleanRecycleBin(intent.name)
        }
    }

    private suspend fun loadShares() {
        _state.update { it.copy(isLoading = true, error = null) }

        controlPanelRepository.getShares()
            .onSuccess { response ->
                val shares = response.data?.shares?.map { share ->
                    ShareInfo(
                        name = share.name ?: "",
                        path = share.path ?: "",
                        desc = share.desc ?: "",
                        volPath = share.volPath ?: "",
                        isHidden = share.additional?.hidden ?: false,
                        hasRecycleBin = share.additional?.recyclebin ?: false,
                        isEncrypted = share.additional?.encryption ?: false,
                        isAclMode = share.additional?.isAclMode ?: false,
                        hideUnreadable = share.additional?.hideUnreadable ?: false,
                        recycleBinAdminOnly = share.additional?.recycleBinAdminOnly ?: false,
                        enableShareCow = share.additional?.enableShareCow ?: false,
                        enableShareCompress = share.additional?.enableShareCompress ?: false,
                        quotaValue = share.additional?.shareQuota ?: 0,
                        quotaUsed = share.additional?.shareQuotaUsed ?: 0
                    )
                } ?: emptyList()

                _state.update {
                    it.copy(
                        shares = shares.sortedBy { it.name.lowercase() },
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

    private suspend fun deleteShare(name: String) {
        _state.update { it.copy(isOperating = true) }

        controlPanelRepository.deleteShare(listOf(name))
            .onSuccess {
                _state.update { it.copy(isOperating = false) }
                _events.emit(SharedFoldersEvent.DeleteSuccess)
                loadShares()
            }
            .onFailure { exception ->
                _state.update { it.copy(isOperating = false) }
                _events.emit(SharedFoldersEvent.ShowError(exception.message ?: "删除失败"))
            }
    }

    private suspend fun cleanRecycleBin(name: String) {
        _state.update { it.copy(isOperating = true) }

        controlPanelRepository.cleanRecycleBin(name)
            .onSuccess {
                _state.update { it.copy(isOperating = false) }
                _events.emit(SharedFoldersEvent.CleanRecycleBinSuccess)
            }
            .onFailure { exception ->
                _state.update { it.copy(isOperating = false) }
                _events.emit(SharedFoldersEvent.ShowError(exception.message ?: "清空回收站失败"))
            }
    }
}