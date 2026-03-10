package wang.zengye.dsm.ui.download

import android.content.Context
import android.net.Uri
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
import java.io.File
import javax.inject.Inject

data class AddTaskUiState(
    override val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val saveLocation: String = "",
    val urls: String = "",
    val torrentPath: String = "",
    override val error: String? = null,
    val success: Boolean = false,
    val createdTaskId: String? = null
) : wang.zengye.dsm.ui.base.BaseState

sealed class AddTaskIntent : wang.zengye.dsm.ui.base.BaseIntent {
    data object LoadDefaultLocation : AddTaskIntent()
    data class SetSaveLocation(val location: String) : AddTaskIntent()
    data class SetUrls(val urls: String) : AddTaskIntent()
    data class SetTorrentPath(val path: String) : AddTaskIntent()
    data class CreateTaskFromUrl(val context: Context, val uri: Uri? = null) : AddTaskIntent()
    data class CreateTaskFromFile(val context: Context, val uri: Uri) : AddTaskIntent()
}

sealed class AddTaskEvent : wang.zengye.dsm.ui.base.BaseEvent {
    data class Error(val message: String) : AddTaskEvent()
    data class CreateSuccess(val taskId: String) : AddTaskEvent()
}

// 迁移状态：[已完成]
// 说明：已使用 Moshi API，移除所有 Gson 操作
@HiltViewModel
class AddTaskViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository
) : BaseViewModel<AddTaskUiState, AddTaskIntent, AddTaskEvent>() {

    private val _uiState = MutableStateFlow(AddTaskUiState())
    override val state: StateFlow<AddTaskUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddTaskEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    init {
        sendIntent(AddTaskIntent.LoadDefaultLocation)
    }

    override suspend fun processIntent(intent: AddTaskIntent) {
        when (intent) {
            is AddTaskIntent.LoadDefaultLocation -> loadDefaultLocation()
            is AddTaskIntent.SetSaveLocation -> setSaveLocation(intent.location)
            is AddTaskIntent.SetUrls -> setUrls(intent.urls)
            is AddTaskIntent.SetTorrentPath -> setTorrentPath(intent.path)
            is AddTaskIntent.CreateTaskFromUrl -> createTaskFromUrl(intent.context, intent.uri)
            is AddTaskIntent.CreateTaskFromFile -> createTaskFromFile(intent.context, intent.uri)
        }
    }

    private suspend fun loadDefaultLocation() {
        _uiState.update { it.copy(isLoading = true) }

        downloadRepository.getDownloadLocation().fold(
            onSuccess = { response ->
                _uiState.update { it.copy(isLoading = false, saveLocation = response.destination ?: "/") }
            },
            onFailure = {
                _uiState.update { it.copy(isLoading = false, saveLocation = "/") }
            }
        )
    }

    private fun setSaveLocation(location: String) {
        _uiState.update { it.copy(saveLocation = location) }
    }

    private fun setUrls(urls: String) {
        _uiState.update { it.copy(urls = urls) }
    }

    private fun setTorrentPath(path: String) {
        _uiState.update { it.copy(torrentPath = path) }
    }

    private suspend fun createTaskFromUrl(context: Context, uri: Uri? = null) {
        _uiState.update { it.copy(isCreating = true, error = null) }

        val urlList = _uiState.value.urls.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        if (urlList.isEmpty()) {
            _uiState.update { it.copy(isCreating = false, error = appString(R.string.download_error_url_required)) }
            return
        }

        downloadRepository.createTaskFromUrl(urlList, _uiState.value.saveLocation).fold(
            onSuccess = {
                _uiState.update { it.copy(isCreating = false, success = true) }
            },
            onFailure = { e ->
                _uiState.update { it.copy(isCreating = false, error = appString(R.string.download_create_failed_reason, e.message ?: "")) }
            }
        )
    }

    private suspend fun createTaskFromFile(context: Context, uri: Uri) {
        _uiState.update { it.copy(isCreating = true, error = null) }

        try {
            // 将 Uri 内容复制到临时文件
            val fileName = getFileName(context, uri)
            val tempFile = File(context.cacheDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            downloadRepository.createTaskFromFile(tempFile, _uiState.value.saveLocation).fold(
                onSuccess = {
                    _uiState.update { it.copy(isCreating = false, success = true) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isCreating = false, error = appString(R.string.download_create_failed_reason, e.message ?: "")) }
                }
            )

            // 删除临时文件
            tempFile.delete()
        } catch (e: Exception) {
            _uiState.update { it.copy(isCreating = false, error = appString(R.string.download_read_file_failed, e.message ?: "")) }
        }
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var name = "unknown.torrent"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }
}
