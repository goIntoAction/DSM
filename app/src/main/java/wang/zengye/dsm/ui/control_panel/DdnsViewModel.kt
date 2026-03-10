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

data class DdnsRecord(
    val id: Int = 0,
    val hostname: String = "",
    val provider: String = "",
    val username: String = "",
    val status: String = "",
    val lastUpdate: Long = 0,
    val externalIp: String = ""
)

data class DdnsUiState(
    override val isLoading: Boolean = false,
    val records: List<DdnsRecord> = emptyList(),
    override val error: String? = null,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val editingRecord: DdnsRecord? = null
) : wang.zengye.dsm.ui.base.BaseState

@HiltViewModel
class DdnsViewModel @Inject constructor(
    private val controlPanelRepository: ControlPanelRepository
) : BaseViewModel<DdnsUiState, DdnsIntent, DdnsEvent>() {

    private val _state = MutableStateFlow(DdnsUiState())
    override val state: StateFlow<DdnsUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<DdnsEvent>(extraBufferCapacity = 1)
    override val events = _events.asSharedFlow()

    init {
        sendIntent(DdnsIntent.LoadRecords)
    }

    override suspend fun processIntent(intent: DdnsIntent) {
        when (intent) {
            is DdnsIntent.LoadRecords -> loadRecords()
            is DdnsIntent.ShowAddDialog -> showAddDialog()
            is DdnsIntent.HideAddDialog -> hideAddDialog()
            is DdnsIntent.AddRecord -> addRecord(intent.provider, intent.hostname, intent.username, intent.password)
            is DdnsIntent.DeleteRecord -> deleteRecord(intent.id)
            is DdnsIntent.ShowEditDialog -> showEditDialog(intent.record)
            is DdnsIntent.HideEditDialog -> hideEditDialog()
            is DdnsIntent.UpdateRecord -> updateRecord(intent.id, intent.provider, intent.hostname, intent.username, intent.password)
        }
    }

    private suspend fun loadRecords() {
        _state.update { it.copy(isLoading = true, error = null) }

        controlPanelRepository.getDdnsRecords()
            .onSuccess { response ->
                val records = response.data?.records?.map { record ->
                    DdnsRecord(
                        id = record.id ?: 0,
                        hostname = record.hostname ?: "",
                        provider = record.provider ?: "",
                        username = record.username ?: "",
                        status = record.status ?: "",
                        lastUpdate = record.lastUpdate ?: 0,
                        externalIp = record.externalIp ?: ""
                    )
                } ?: emptyList()

                _state.update {
                    it.copy(
                        records = records,
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

    private fun showAddDialog() {
        _state.update { it.copy(showAddDialog = true) }
    }

    private fun hideAddDialog() {
        _state.update { it.copy(showAddDialog = false) }
    }

    private fun showEditDialog(record: DdnsRecord) {
        _state.update { it.copy(showEditDialog = true, editingRecord = record) }
    }

    private fun hideEditDialog() {
        _state.update { it.copy(showEditDialog = false, editingRecord = null) }
    }

    private suspend fun addRecord(provider: String, hostname: String, username: String, password: String) {
        controlPanelRepository.createDdnsRecord(provider, hostname, username, password)
            .onSuccess {
                _events.emit(DdnsEvent.AddSuccess)
                _state.update { it.copy(showAddDialog = false) }
                loadRecords()
            }
            .onFailure { _events.emit(DdnsEvent.ShowError(it.message ?: "添加失败")) }
    }

    private suspend fun deleteRecord(id: Int) {
        controlPanelRepository.deleteDdnsRecord(id.toString())
            .onSuccess {
                _events.emit(DdnsEvent.DeleteSuccess)
                loadRecords()
            }
            .onFailure { _events.emit(DdnsEvent.ShowError(it.message ?: "删除失败")) }
    }

    private suspend fun updateRecord(id: Int, provider: String, hostname: String, username: String, password: String?) {
        controlPanelRepository.updateDdnsRecord(id.toString(), provider, hostname, username, password)
            .onSuccess {
                _events.emit(DdnsEvent.UpdateSuccess)
                _state.update { it.copy(showEditDialog = false, editingRecord = null) }
                loadRecords()
            }
            .onFailure { _events.emit(DdnsEvent.ShowError(it.message ?: "更新失败")) }
    }
}