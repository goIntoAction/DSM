package wang.zengye.dsm.ui.download

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
import wang.zengye.dsm.R
import wang.zengye.dsm.data.repository.DownloadRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import wang.zengye.dsm.util.appString
import javax.inject.Inject

/**
 * BT文件数据
 */
data class BtFileItem(
    val index: Int = 0,
    val fileName: String = "",
    val size: Long = 0,
    val sizeDownloaded: Long = 0,
    val priority: String = "normal", // high, normal, low
    val isSelected: Boolean = true
)

/**
 * BT文件选择UI状态
 */
data class BtFileSelectUiState(
    override val isLoading: Boolean = false,
    val files: List<BtFileItem> = emptyList(),
    override val error: String? = null,
    val isSaving: Boolean = false,
    val selectedCount: Int = 0,
    val totalSize: Long = 0,
    val selectedSize: Long = 0
) : wang.zengye.dsm.ui.base.BaseState

sealed class BtFileSelectIntent : wang.zengye.dsm.ui.base.BaseIntent {
    data class LoadFiles(val taskId: String) : BtFileSelectIntent()
    data object ToggleSelectAll : BtFileSelectIntent()
    data class ToggleFileSelection(val index: Int) : BtFileSelectIntent()
    data class SaveSelection(val taskId: String) : BtFileSelectIntent()
}

sealed class BtFileSelectEvent : wang.zengye.dsm.ui.base.BaseEvent {
    data class Error(val message: String) : BtFileSelectEvent()
    data object SaveSuccess : BtFileSelectEvent()
}

// 迁移状态：[已完成]
// 说明：已使用 Moshi API，移除所有 Gson 操作
@HiltViewModel
class BtFileSelectViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository
) : BaseViewModel<BtFileSelectUiState, BtFileSelectIntent, BtFileSelectEvent>() {

    private val _uiState = MutableStateFlow(BtFileSelectUiState())
    override val state: StateFlow<BtFileSelectUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<BtFileSelectEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    override suspend fun processIntent(intent: BtFileSelectIntent) {
        when (intent) {
            is BtFileSelectIntent.LoadFiles -> loadFiles(intent.taskId)
            is BtFileSelectIntent.ToggleSelectAll -> toggleSelectAll()
            is BtFileSelectIntent.ToggleFileSelection -> toggleFileSelection(intent.index)
            is BtFileSelectIntent.SaveSelection -> saveSelection(intent.taskId)
        }
    }

    /**
     * 加载BT文件列表
     */
    private suspend fun loadFiles(taskId: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        downloadRepository.getBtFileList(taskId).fold(
            onSuccess = { response ->
                val files = response.files?.mapIndexed { index, fileResp ->
                    BtFileItem(
                        index = fileResp.priority?.let { index } ?: index,
                        fileName = fileResp.name ?: "",
                        size = fileResp.size ?: 0,
                        sizeDownloaded = 0,
                        priority = "normal",
                        isSelected = fileResp.selected ?: true
                    )
                } ?: emptyList()

                val selectedCount = files.count { it.isSelected }
                val totalSize = files.sumOf { it.size }
                val selectedSize = files.filter { it.isSelected }.sumOf { it.size }

                _uiState.update { it.copy(
                    isLoading = false,
                    files = files,
                    selectedCount = selectedCount,
                    totalSize = totalSize,
                    selectedSize = selectedSize
                ) }
            },
            onFailure = { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        )
    }

    /**
     * 切换文件选择状态
     */
    private fun toggleFileSelection(index: Int) {
        _uiState.update { state ->
            val newFiles = state.files.map { file ->
                if (file.index == index) {
                    file.copy(isSelected = !file.isSelected)
                } else {
                    file
                }
            }
            val selectedCount = newFiles.count { it.isSelected }
            val selectedSize = newFiles.filter { it.isSelected }.sumOf { it.size }

            state.copy(
                files = newFiles,
                selectedCount = selectedCount,
                selectedSize = selectedSize
            )
        }
    }

    /**
     * 全选/取消全选
     */
    private fun toggleSelectAll() {
        _uiState.update { state ->
            val allSelected = state.files.all { it.isSelected }
            val newFiles = state.files.map { it.copy(isSelected = !allSelected) }
            val selectedCount = newFiles.count { it.isSelected }
            val selectedSize = newFiles.filter { it.isSelected }.sumOf { it.size }

            state.copy(
                files = newFiles,
                selectedCount = selectedCount,
                selectedSize = selectedSize
            )
        }
    }

    /**
     * 保存文件选择
     */
    private suspend fun saveSelection(taskId: String) {
        val selectedIndexes = _uiState.value.files
            .filter { it.isSelected }
            .map { it.index }

        if (selectedIndexes.isEmpty()) {
            _uiState.update { it.copy(error = appString(R.string.download_select_at_least_one)) }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        downloadRepository.setBtFileSelection(taskId, selectedIndexes, true).fold(
            onSuccess = {
                _uiState.update { it.copy(isSaving = false) }
                _events.emit(BtFileSelectEvent.SaveSuccess)
            },
            onFailure = { error ->
                _uiState.update { it.copy(isSaving = false, error = error.message) }
            }
        )
    }
}
