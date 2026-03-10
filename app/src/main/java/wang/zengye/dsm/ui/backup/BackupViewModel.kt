package wang.zengye.dsm.ui.backup

import android.net.Uri
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.base.BaseViewModel
import wang.zengye.dsm.util.appString
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * 备份任务
 */
data class BackupTask(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val sourcePath: String = "",
    val targetPath: String = "",
    val status: BackupStatus = BackupStatus.IDLE,
    val progress: Int = 0,
    val totalFiles: Int = 0,
    val completedFiles: Int = 0,
    val lastRunTime: Date? = null,
    val errorMessage: String? = null
)

enum class BackupStatus {
    IDLE,
    RUNNING,
    COMPLETED,
    FAILED,
    PAUSED
}

data class BackupUiState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val tasks: List<BackupTask> = emptyList(),
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val uploadFileName: String = "",
    val backupFolder: String = "/photos",
    val lastBackupTime: Date? = null,
    val selectedFiles: List<Uri> = emptyList()
) : wang.zengye.dsm.ui.base.BaseState

sealed class BackupIntent : wang.zengye.dsm.ui.base.BaseIntent {
    data object LoadBackupTasks : BackupIntent()
    data class SelectBackupFolder(val folder: String) : BackupIntent()
    data class AddSelectedFiles(val uris: List<Uri>) : BackupIntent()
    data object ClearSelectedFiles : BackupIntent()
    data object StartBackup : BackupIntent()
    data object PauseBackup : BackupIntent()
    data class ResumeBackup(val taskId: String) : BackupIntent()
    data class DeleteTask(val taskId: String) : BackupIntent()
    data object ClearError : BackupIntent()
}

sealed class BackupEvent : wang.zengye.dsm.ui.base.BaseEvent {
    data class Error(val message: String) : BackupEvent()
}

@HiltViewModel
class BackupViewModel @Inject constructor() : BaseViewModel<BackupUiState, BackupIntent, BackupEvent>() {

    private val _uiState = MutableStateFlow(BackupUiState())
    override val state: StateFlow<BackupUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<BackupEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    init {
        sendIntent(BackupIntent.LoadBackupTasks)
    }

    override suspend fun processIntent(intent: BackupIntent) {
        when (intent) {
            is BackupIntent.LoadBackupTasks -> loadBackupTasks()
            is BackupIntent.SelectBackupFolder -> selectBackupFolder(intent.folder)
            is BackupIntent.AddSelectedFiles -> addSelectedFiles(intent.uris)
            is BackupIntent.ClearSelectedFiles -> clearSelectedFiles()
            is BackupIntent.StartBackup -> startBackup()
            is BackupIntent.PauseBackup -> pauseBackup()
            is BackupIntent.ResumeBackup -> resumeBackup(intent.taskId)
            is BackupIntent.DeleteTask -> deleteTask(intent.taskId)
            is BackupIntent.ClearError -> clearError()
        }
    }

    private suspend fun loadBackupTasks() {
        _uiState.update { it.copy(isLoading = true) }
        // 模拟加载备份任务
        // 实际应从API获取
        _uiState.update {
            it.copy(
                isLoading = false,
                tasks = listOf(
                    BackupTask(
                        id = "1",
                        name = appString(R.string.backup_photo_backup),
                        sourcePath = appString(R.string.backup_phone_album),
                        targetPath = "/photos/backup",
                        status = BackupStatus.IDLE,
                        lastRunTime = Date()
                    ),
                    BackupTask(
                        id = "2",
                        name = appString(R.string.backup_document_backup),
                        sourcePath = "Documents",
                        targetPath = "/documents/backup",
                        status = BackupStatus.IDLE,
                        lastRunTime = null
                    )
                )
            )
        }
    }

    private fun selectBackupFolder(folder: String) {
        _uiState.update { it.copy(backupFolder = folder) }
    }

    private fun addSelectedFiles(uris: List<Uri>) {
        _uiState.update {
            it.copy(selectedFiles = it.selectedFiles + uris)
        }
    }

    private fun clearSelectedFiles() {
        _uiState.update { it.copy(selectedFiles = emptyList()) }
    }

    private suspend fun startBackup() {
        _uiState.update { it.copy(isUploading = true, uploadProgress = 0f) }

        val files = _uiState.value.selectedFiles
        if (files.isEmpty()) {
            _uiState.update { it.copy(error = appString(R.string.backup_select_files_error), isUploading = false) }
            return
        }

        try {
            // 模拟上传进度
            for (i in 1..10) {
                delay(500)
                _uiState.update {
                    it.copy(
                        uploadProgress = i / 10f,
                        uploadFileName = appString(R.string.backup_file_progress, i, files.size)
                    )
                }
            }

            _uiState.update {
                it.copy(
                    isUploading = false,
                    lastBackupTime = Date(),
                    selectedFiles = emptyList(),
                    uploadProgress = 0f
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isUploading = false,
                    error = e.message
                )
            }
        }
    }

    private fun pauseBackup() {
        _uiState.update {
            it.copy(
                tasks = it.tasks.map { task ->
                    if (task.status == BackupStatus.RUNNING) {
                        task.copy(status = BackupStatus.PAUSED)
                    } else task
                }
            )
        }
    }

    private fun resumeBackup(taskId: String) {
        _uiState.update {
            it.copy(
                tasks = it.tasks.map { task ->
                    if (task.id == taskId && task.status == BackupStatus.PAUSED) {
                        task.copy(status = BackupStatus.RUNNING)
                    } else task
                }
            )
        }
    }

    private fun deleteTask(taskId: String) {
        _uiState.update {
            it.copy(tasks = it.tasks.filter { task -> task.id != taskId })
        }
    }

    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
