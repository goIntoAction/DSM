package wang.zengye.dsm.ui.storage

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import wang.zengye.dsm.data.repository.SystemRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import javax.inject.Inject

/**
 * 磁盘SMART信息
 */
data class SmartDiskInfo(
    val device: String = "",
    val model: String = "",
    val serial: String = "",
    val temperature: Int = 0,
    val healthStatus: String = "",
    val status: String = "",
    val size: Long = 0,
    val testInProgress: Boolean = false,
    val testProgress: Int = 0,
    val lastTestType: String = "",
    val lastTestTime: Long = 0
) {
    val isHealthy: Boolean get() = healthStatus == "normal"
    val hasWarning: Boolean get() = healthStatus == "warning"
    val isAbnormal: Boolean get() = healthStatus == "abnormal"
}

/**
 * SMART测试日志
 */
data class SmartTestLog(
    val id: Int = 0,
    val device: String = "",
    val testType: String = "",
    val status: String = "",
    val progress: Int = 0,
    val startTime: Long = 0,
    val endTime: Long? = null
)

/**
 * SMART检测UI状态
 */
data class SmartTestUiState(
    override val isLoading: Boolean = false,
    val disks: List<SmartDiskInfo> = emptyList(),
    val testLogs: List<SmartTestLog> = emptyList(),
    override val error: String? = null,
    val selectedDisk: SmartDiskInfo? = null,
    val isRunningTest: Boolean = false,
    val testDiskDevice: String? = null,
    val showTestDialog: Boolean = false
) : wang.zengye.dsm.ui.base.BaseState

/**
 * SMART检测ViewModel
 */
@HiltViewModel
class SmartTestViewModel @Inject constructor(
    private val systemRepository: SystemRepository
) : BaseViewModel<SmartTestUiState, SmartTestIntent, SmartTestEvent>() {

    companion object {
        private const val TAG = "SmartTestViewModel"
    }

    private val _uiState = MutableStateFlow(SmartTestUiState())
    override val state: StateFlow<SmartTestUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SmartTestEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    init {
        sendIntent(SmartTestIntent.LoadSmartInfo)
    }

    override suspend fun processIntent(intent: SmartTestIntent) {
        when (intent) {
            is SmartTestIntent.LoadSmartInfo -> loadSmartInfo()
            is SmartTestIntent.SelectDisk -> selectDisk(intent.disk)
            is SmartTestIntent.ShowTestDialog -> showTestDialog()
            is SmartTestIntent.HideTestDialog -> hideTestDialog()
            is SmartTestIntent.StartTest -> startTest(intent.device, intent.testType)
        }
    }

    /**
     * 加载SMART信息
     */
    private suspend fun loadSmartInfo() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        systemRepository.getSmartHealth().onSuccess { data ->
            val disks = data.disk?.map { disk ->
                SmartDiskInfo(
                    device = disk.device ?: "",
                    model = disk.model ?: "",
                    serial = disk.serial ?: "",
                    temperature = disk.temp ?: 0,
                    healthStatus = disk.health ?: "normal",
                    status = disk.status ?: "",
                    size = disk.size ?: 0,
                    testInProgress = (disk.testProgress ?: 0) > 0 && (disk.testProgress ?: 0) < 100,
                    testProgress = disk.testProgress ?: 0,
                    lastTestType = disk.lastTestType ?: "",
                    lastTestTime = disk.lastTestTime ?: 0
                )
            } ?: emptyList()

            _uiState.update { it.copy(isLoading = false, disks = disks) }
        }.onFailure { error ->
            Log.e(TAG, "SmartHealth API error", error)
            _uiState.update { it.copy(isLoading = false, error = error.message) }
            _events.emit(SmartTestEvent.Error(error.message ?: "Failed to load SMART info"))
        }
    }

    /**
     * 选择磁盘
     */
    private suspend fun selectDisk(disk: SmartDiskInfo?) {
        _uiState.update { it.copy(selectedDisk = disk) }
        disk?.let {
            loadTestLogs(it.device)
        }
    }

    /**
     * 加载测试日志
     */
    private suspend fun loadTestLogs(device: String) {
                    systemRepository.getSmartTestLog(device).onSuccess { data ->            val logs = data.items?.mapIndexed { index, log ->
                SmartTestLog(
                    id = log.id?.toIntOrNull() ?: index,
                    device = log.diskId ?: "",
                    testType = log.testType ?: "",
                    status = log.status ?: "",
                    progress = log.progress ?: 0,
                    startTime = log.startTime?.toLongOrNull() ?: 0,
                    endTime = log.endTime?.toLongOrNull()
                )
            } ?: emptyList()

            _uiState.update { it.copy(testLogs = logs) }
        }.onFailure { error ->
            Log.e(TAG, "SmartTestLog API error", error)
            _uiState.update { it.copy(testLogs = emptyList()) }
        }
    }

    /**
     * 显示测试对话框
     */
    private fun showTestDialog() {
        _uiState.update { it.copy(showTestDialog = true) }
    }

    /**
     * 隐藏测试对话框
     */
    private fun hideTestDialog() {
        _uiState.update { it.copy(showTestDialog = false) }
    }

    /**
     * 开始SMART测试
     */
    private suspend fun startTest(device: String, testType: String) {
        _uiState.update { it.copy(isRunningTest = true, testDiskDevice = device, showTestDialog = false) }

        systemRepository.doSmartTest(device, testType).onSuccess {
            _uiState.update { it.copy(isRunningTest = false, testDiskDevice = null) }
            _events.emit(SmartTestEvent.TestStarted)
            loadSmartInfo()
        }.onFailure { error ->
            Log.e(TAG, "doSmartTest API error", error)
            _uiState.update { it.copy(isRunningTest = false, testDiskDevice = null, error = error.message) }
            _events.emit(SmartTestEvent.Error(error.message ?: "Test failed"))
        }
    }
}
