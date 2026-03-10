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
import wang.zengye.dsm.data.model.FavoriteItemDto
import wang.zengye.dsm.data.repository.FileRepository
import wang.zengye.dsm.ui.base.BaseViewModel

/**
 * 收藏项数据
 */
data class FavoriteItem(
    val name: String = "",
    val path: String = "",
    val status: String = "valid" // valid, broken
)

/**
 * 收藏夹UI状态
 */
data class FavoriteUiState(
    override val isLoading: Boolean = false,
    val favorites: List<FavoriteItem> = emptyList(),
    override val error: String? = null,
    val showRenameDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val selectedItem: FavoriteItem? = null,
    val newName: String = ""
) : wang.zengye.dsm.ui.base.BaseState

/**
 * 收藏夹ViewModel
 * 迁移状态：[已完成]
 * 说明：已使用 Moshi API，移除所有 JsonObject/Gson 操作
 */
@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val fileRepository: FileRepository
) : BaseViewModel<FavoriteUiState, FavoriteIntent, FavoriteEvent>() {

    private val _state = MutableStateFlow(FavoriteUiState())
    override val state: StateFlow<FavoriteUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<FavoriteEvent>(extraBufferCapacity = 10)
    override val events = _events.asSharedFlow()

    init {
        sendIntent(FavoriteIntent.LoadFavorites)
    }

    override suspend fun processIntent(intent: FavoriteIntent) {
        when (intent) {
            is FavoriteIntent.LoadFavorites -> loadFavorites()
            is FavoriteIntent.ShowRenameDialog -> showRenameDialog(intent.item)
            is FavoriteIntent.ShowDeleteDialog -> showDeleteDialog(intent.item)
            is FavoriteIntent.HideDialogs -> hideDialogs()
            is FavoriteIntent.UpdateNewName -> updateNewName(intent.name)
            is FavoriteIntent.RenameFavorite -> renameFavorite()
            is FavoriteIntent.DeleteFavorite -> deleteFavorite()
        }
    }

    /**
     * 加载收藏列表
     */
    private suspend fun loadFavorites() {
        _state.update { it.copy(isLoading = true, error = null) }

        fileRepository.favoriteList()
            .onSuccess { response ->
                val favorites = response.data?.favorites?.map { it.toFavoriteItem() } ?: emptyList()
                _state.update { it.copy(isLoading = false, favorites = favorites) }
            }
            .onFailure { exception ->
                _state.update { it.copy(isLoading = false, error = exception.message) }
                _events.emit(FavoriteEvent.ShowError(exception.message ?: "加载失败"))
            }
    }

    /**
     * 将 Moshi 模型转换为 UI 模型
     */
    private fun FavoriteItemDto.toFavoriteItem(): FavoriteItem {
        return FavoriteItem(
            name = this.name,
            path = this.path,
            status = this.status
        )
    }

    /**
     * 显示重命名对话框
     */
    private fun showRenameDialog(item: FavoriteItem) {
        _state.update { it.copy(
            showRenameDialog = true,
            selectedItem = item,
            newName = item.name
        ) }
    }

    /**
     * 显示删除确认对话框
     */
    private fun showDeleteDialog(item: FavoriteItem) {
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
            showRenameDialog = false,
            showDeleteDialog = false,
            selectedItem = null,
            newName = ""
        ) }
    }

    /**
     * 更新新名称
     */
    private fun updateNewName(name: String) {
        _state.update { it.copy(newName = name) }
    }

    /**
     * 重命名收藏
     */
    private suspend fun renameFavorite() {
        val item = _state.value.selectedItem ?: return
        val newName = _state.value.newName.trim()

        if (newName.isEmpty()) return

        fileRepository.favoriteEdit(item.path, newName)
            .onSuccess {
                hideDialogs()
                _events.emit(FavoriteEvent.RenameSuccess)
                loadFavorites()
            }
            .onFailure { exception ->
                _state.update { it.copy(error = exception.message) }
                _events.emit(FavoriteEvent.ShowError(exception.message ?: "重命名失败"))
            }
    }

    /**
     * 删除收藏
     */
    private suspend fun deleteFavorite() {
        val item = _state.value.selectedItem ?: return

        fileRepository.favoriteDelete(item.path)
            .onSuccess {
                hideDialogs()
                _events.emit(FavoriteEvent.DeleteSuccess)
                loadFavorites()
            }
            .onFailure { exception ->
                _state.update { it.copy(error = exception.message) }
                _events.emit(FavoriteEvent.ShowError(exception.message ?: "删除失败"))
            }
    }
}
