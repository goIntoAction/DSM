package wang.zengye.dsm.ui.file

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import wang.zengye.dsm.data.repository.FileRepository
import wang.zengye.dsm.ui.base.BaseViewModel

/**
 * 共享链接数据
 */
data class ShareLinkItem(
    val id: String = "",
    val name: String = "",
    val path: String = "",
    val url: String = "",
    val dateExpired: Long = 0,
    val dateCreated: Long = 0,
    val isValid: Boolean = true
) {
    val isExpired: Boolean
        get() = dateExpired > 0 && dateExpired < System.currentTimeMillis() / 1000
}

/**
 * 共享链接UI状态
 */
data class ShareManagerUiState(
    override val isLoading: Boolean = false,
    val shares: List<ShareLinkItem> = emptyList(),
    override val error: String? = null,
    val showDeleteDialog: Boolean = false,
    val selectedItem: ShareLinkItem? = null
) : wang.zengye.dsm.ui.base.BaseState

/**
 * 共享链接管理ViewModel
 * 迁移状态：[已完成]
 * 说明：已使用 Moshi API，移除所有 JsonObject/Gson 操作
 */
@HiltViewModel
class ShareManagerViewModel @Inject constructor(
    private val fileRepository: FileRepository
) : BaseViewModel<ShareManagerUiState, ShareManagerIntent, ShareManagerEvent>() {

    private val _state = MutableStateFlow(ShareManagerUiState())
    override val state: StateFlow<ShareManagerUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ShareManagerEvent>(extraBufferCapacity = 10)
    override val events = _events.asSharedFlow()

    init {
        sendIntent(ShareManagerIntent.LoadShares)
    }

    override suspend fun processIntent(intent: ShareManagerIntent) {
        when (intent) {
            is ShareManagerIntent.LoadShares -> loadShares()
            is ShareManagerIntent.ShowDeleteDialog -> showDeleteDialog(intent.item)
            is ShareManagerIntent.HideDialogs -> hideDialogs()
            is ShareManagerIntent.DeleteShare -> deleteShare()
        }
    }

    /**
     * 加载共享链接列表
     */
    private suspend fun loadShares() {
        _state.update { it.copy(isLoading = true, error = null) }

        fileRepository.shareList()
            .onSuccess { response ->
                val shares = response.data?.links?.map { it.toShareLinkItem() } ?: emptyList()
                _state.update { it.copy(isLoading = false, shares = shares) }
            }
            .onFailure { exception ->
                _state.update { it.copy(isLoading = false, error = exception.message) }
                _events.emit(ShareManagerEvent.ShowError(exception.message ?: "加载失败"))
            }
    }

    /**
     * 将 Moshi 模型转换为 UI 模型
     */
    private fun wang.zengye.dsm.data.model.ShareLinkItemDto.toShareLinkItem(): ShareLinkItem {
        return ShareLinkItem(
            id = this.id,
            name = this.name,
            path = this.path,
            url = this.url,
            dateExpired = this.dateExpired,
            dateCreated = this.dateCreated,
            isValid = this.isValid
        )
    }

    /**
     * 显示删除确认对话框
     */
    private fun showDeleteDialog(item: ShareLinkItem) {
        _state.update { it.copy(
            showDeleteDialog = true,
            selectedItem = item
        ) }
    }

    /**
     * 隐藏对话框
     */
    private fun hideDialogs() {
        _state.update { it.copy(
            showDeleteDialog = false,
            selectedItem = null
        ) }
    }

    /**
     * 删除共享链接
     */
    private suspend fun deleteShare() {
        val item = _state.value.selectedItem ?: return

        fileRepository.shareDelete(item.id)
            .onSuccess {
                hideDialogs()
                _events.emit(ShareManagerEvent.DeleteSuccess)
                loadShares()
            }
            .onFailure { exception ->
                _state.update { it.copy(error = exception.message) }
                _events.emit(ShareManagerEvent.ShowError(exception.message ?: "删除失败"))
            }
    }
}
