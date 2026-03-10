package wang.zengye.dsm.ui.update

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
import wang.zengye.dsm.data.repository.SystemRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import javax.inject.Inject

data class UpdateUiState(
    override val isLoading: Boolean = false,
    val isChecking: Boolean = false,
    val currentVersion: String = "",
    val buildNumber: String = "",
    val model: String = "",
    val serial: String = "",
    // 更新信息
    val updateAvailable: Boolean = false,
    val newVersion: String = "",
    val releaseDate: String = "",
    val changelog: String = "",
    val isImportant: Boolean = false,
    val lastCheckTime: String = "",
    override val error: String? = null
) : wang.zengye.dsm.ui.base.BaseState

// 迁移状态：[已完成]
// 说明：已完全迁移到 Moshi API
@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val repository: SystemRepository
) : BaseViewModel<UpdateUiState, UpdateIntent, UpdateEvent>() {

    private val _uiState = MutableStateFlow(UpdateUiState())
    override val state: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<UpdateEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    init {
        sendIntent(UpdateIntent.LoadCurrentVersion)
    }

    override suspend fun processIntent(intent: UpdateIntent) {
        when (intent) {
            is UpdateIntent.LoadCurrentVersion -> loadCurrentVersion()
            is UpdateIntent.CheckUpdate -> checkUpdate()
        }
    }

    private suspend fun loadCurrentVersion() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        repository.getFirmwareVersion()
            .onSuccess { firmware ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentVersion = firmware.firmwareVer ?: "",
                        buildNumber = firmware.buildNumber ?: "",
                        model = firmware.model ?: "",
                        serial = firmware.serial ?: ""
                    )
                }
            }
            .onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
    }

    private suspend fun checkUpdate() {
        _uiState.update { it.copy(isChecking = true, error = null) }

        repository.checkFirmwareUpdate()
            .onSuccess { firmware ->
                _uiState.update {
                    it.copy(
                        isChecking = false,
                        updateAvailable = firmware.updateAvailable ?: false,
                        newVersion = firmware.version ?: "",
                        releaseDate = firmware.releaseDate ?: "",
                        changelog = firmware.releaseNote ?: "",
                        isImportant = firmware.rebootNeeded ?: false,
                        lastCheckTime = currentTime()
                    )
                }
            }
            .onFailure { error ->
                _uiState.update { it.copy(isChecking = false, error = error.message) }
            }
    }

    private fun currentTime(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}