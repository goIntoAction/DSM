package wang.zengye.dsm.ui.smart

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
 * SMART 测试信息
 */
data class SmartTestInfo(
    val diskId: String = "",
    val diskName: String = "",
    val status: String = "",
    val health: String = "",
    val temperature: Int = 0,
    val powerOnHours: Long = 0,
    val capacity: Long = 0
)

/**
 * SMART 测试日志
 */
data class SmartTestLog(
    val id: String = "",
    val diskId: String = "",
    val testType: String = "",
    val progress: Int = 0,
    val status: String = "",
    val result: String = "",
    val startTime: String = "",
    val endTime: String = ""
)

data class SmartTestUiState(
    override val isLoading: Boolean = true,
    override val error: String? = null,
    val disks: List<SmartTestInfo> = emptyList(),
    val testLogs: List<SmartTestLog> = emptyList(),
    val isTesting: Boolean = false,
    val testingDiskId: String? = null
) : wang.zengye.dsm.ui.base.BaseState

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
        sendIntent(SmartTestIntent.LoadData)
    }

    override suspend fun processIntent(intent: SmartTestIntent) {
        when (intent) {
            is SmartTestIntent.LoadData -> loadData()
            is SmartTestIntent.Refresh -> loadData()
            is SmartTestIntent.StartSmartTest -> startSmartTest(intent.diskId)
            is SmartTestIntent.SelectDiskById -> {
                // 磁盘已在列表中显示，无需特殊处理
                // 可以在未来添加滚动到指定磁盘的功能
            }
        }
    }

    private suspend fun loadData() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        // 获取存储信息（包含SMART状态）
        val storageResult = systemRepository.getStorageInfo()

        storageResult.onSuccess { response ->
            try {
                val data = response.data
                val disksList = data?.disks?.mapNotNull { disk ->
                    SmartTestInfo(
                        diskId = disk.id ?: "",
                        diskName = disk.longName ?: disk.name ?: "",
                        status = disk.status ?: "",
                        health = disk.overviewStatus ?: disk.health ?: "",
                        temperature = disk.temp ?: 0,
                        powerOnHours = disk.powerOnTime ?: 0L,
                        capacity = disk.getSizeLong()
                    )
                } ?: emptyList()

                _uiState.update { it.copy(disks = disksList) }
            } catch (e: Exception) {
                Log.e(TAG, "Parse storage error", e)
                _events.emit(SmartTestEvent.Error(e.message ?: "Parse error"))
            }
        }.onFailure { error ->
            Log.e(TAG, "Storage API error", error)
            _events.emit(SmartTestEvent.Error(error.message ?: "Storage API error"))
        }

        // 获取SMART测试日志
        val smartLogResult = systemRepository.getSmartTestLog()

        smartLogResult.onSuccess { data ->
            try {
                val logList = data.items?.mapNotNull { log ->
                    SmartTestLog(
                        id = log.id ?: "",
                        diskId = log.diskId ?: "",
                        testType = log.testType ?: "",
                        progress = log.progress ?: 0,
                        status = log.status ?: "",
                        result = log.result ?: "",
                        startTime = log.startTime ?: "",
                        endTime = log.endTime ?: ""
                    )
                } ?: emptyList()

                _uiState.update { it.copy(testLogs = logList) }
            } catch (e: Exception) {
                Log.e(TAG, "Parse SMART log error", e)
            }
        }.onFailure { error ->
            Log.e(TAG, "SmartTestLog API error", error)
        }

        _uiState.update { it.copy(isLoading = false) }
    }

    /**
     * 执行SMART测试
     */
    private suspend fun startSmartTest(diskId: String) {
        _uiState.update { it.copy(isTesting = true, testingDiskId = diskId) }

        val result = systemRepository.doSmartTest(diskId, "short")
        result.onSuccess {
            _uiState.update { it.copy(isTesting = false, testingDiskId = null) }
            _events.emit(SmartTestEvent.TestStarted)
            // 刷新数据
            loadData()
        }.onFailure { error ->
            Log.e(TAG, "SmartTest API error", error)
            _uiState.update { it.copy(isTesting = false, testingDiskId = null, error = error.message) }
            _events.emit(SmartTestEvent.Error(error.message ?: "Test failed"))
        }
    }
}
