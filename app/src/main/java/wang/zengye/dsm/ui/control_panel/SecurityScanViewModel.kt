package wang.zengye.dsm.ui.control_panel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wang.zengye.dsm.R
import dagger.hilt.android.lifecycle.HiltViewModel
import wang.zengye.dsm.data.repository.ControlPanelRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import wang.zengye.dsm.util.appString
import javax.inject.Inject

data class SecurityCheckItem(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val status: String = "",
    val risk: String = "",
    val description: String = "",
    val solution: String = ""
) {
    val isSafe: Boolean
        get() = status == "safe" || status == "pass"

    val riskLevel: Int
        get() = when (risk.lowercase()) {
            "critical" -> 4
            "high" -> 3
            "medium" -> 2
            "low" -> 1
            else -> 0
        }
}

data class SecurityScanUiState(
    override val isLoading: Boolean = false,
    val scanProgress: Int = 0,
    val isScanning: Boolean = false,
    val categories: List<SecurityCategory> = emptyList(),
    override val error: String? = null
) : wang.zengye.dsm.ui.base.BaseState

data class SecurityCategory(
    val name: String,
    val items: List<SecurityCheckItem>
) {
    val safeCount: Int
        get() = items.count { it.isSafe }

    val totalCount: Int
        get() = items.size

    val hasIssues: Boolean
        get() = safeCount < totalCount
}

@HiltViewModel
class SecurityScanViewModel @Inject constructor(
    private val controlPanelRepository: ControlPanelRepository
) : BaseViewModel<SecurityScanUiState, SecurityScanIntent, SecurityScanEvent>() {

    private val _state = MutableStateFlow(SecurityScanUiState())
    override val state: StateFlow<SecurityScanUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SecurityScanEvent>(extraBufferCapacity = 1)
    override val events = _events.asSharedFlow()

    init {
        sendIntent(SecurityScanIntent.LoadScan)
    }

    override suspend fun processIntent(intent: SecurityScanIntent) {
        when (intent) {
            is SecurityScanIntent.LoadScan -> loadScan()
        }
    }

    private suspend fun loadScan() {
        _state.update { it.copy(isLoading = true, error = null) }

        controlPanelRepository.getSecurityScan()
            .onSuccess { response ->
                val categoriesMap = mutableMapOf<String, MutableList<SecurityCheckItem>>()

                response.data?.items?.forEach { item ->
                    val category = item.category?.ifEmpty { appString(R.string.security_scan_category_other) }
                        ?: appString(R.string.security_scan_category_other)

                    if (!categoriesMap.containsKey(category)) {
                        categoriesMap[category] = mutableListOf()
                    }

                    val checkItem = SecurityCheckItem(
                        id = item.id ?: "",
                        name = item.name ?: "",
                        category = category,
                        status = item.status ?: "",
                        risk = item.risk ?: "",
                        description = item.desc ?: "",
                        solution = item.solution ?: ""
                    )
                    categoriesMap[category]?.add(checkItem)
                }

                val categories = categoriesMap.map { (name, items) ->
                    SecurityCategory(name = name, items = items)
                }.sortedByDescending { it.hasIssues }

                _state.update {
                    it.copy(
                        categories = categories,
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
}