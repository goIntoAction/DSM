package wang.zengye.dsm.ui.control_panel

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
import wang.zengye.dsm.data.repository.ControlPanelRepository
import wang.zengye.dsm.data.repository.SystemRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import javax.inject.Inject

data class NotificationItem(
    val id: Long = 0,
    val time: Long = 0,
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val level: String = "",
    val read: Boolean = false
)

data class NotificationsUiState(
    override val isLoading: Boolean = false,
    val notifications: List<NotificationItem> = emptyList(),
    override val error: String? = null
) : wang.zengye.dsm.ui.base.BaseState

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val systemRepository: SystemRepository,
    private val controlPanelRepository: ControlPanelRepository
) : BaseViewModel<NotificationsUiState, NotificationsIntent, NotificationsEvent>() {

    companion object {
        private const val TAG = "NotificationsViewModel"
    }

    private val _uiState = MutableStateFlow(NotificationsUiState())
    override val state: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<NotificationsEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    init {
        sendIntent(NotificationsIntent.LoadNotifications)
    }

    override suspend fun processIntent(intent: NotificationsIntent) {
        when (intent) {
            is NotificationsIntent.LoadNotifications -> loadNotifications()
            is NotificationsIntent.MarkAsRead -> markAsRead(intent.id)
            is NotificationsIntent.ClearAll -> clearAll()
        }
    }

    private suspend fun loadNotifications() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        systemRepository.getNotifications().onSuccess { data ->
            val notifications = (data.notifications ?: data.items ?: emptyList()).mapNotNull { item ->
                try {
                    val message = if (!item.msg.isNullOrEmpty()) {
                        item.msg.joinToString("\n")
                    } else {
                        item.message ?: item.body ?: ""
                    }
                    NotificationItem(
                        id = item.notifyId ?: item.id ?: 0,
                        time = item.time ?: item.timestamp ?: 0,
                        title = item.title ?: item.subject ?: "",
                        message = message,
                        type = item.className ?: item.type ?: "",
                        level = item.level ?: "info",
                        read = item.read ?: false
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing notification", e)
                    null
                }
            }.sortedByDescending { it.time }

            _uiState.update {
                it.copy(
                    notifications = notifications,
                    isLoading = false
                )
            }
        }.onFailure { error ->
            Log.e(TAG, "Notifications API error", error)
            _uiState.update { it.copy(error = error.message, isLoading = false) }
            _events.emit(NotificationsEvent.Error(error.message ?: "Failed to load notifications"))
        }
    }

    private suspend fun markAsRead(id: Long) {
        controlPanelRepository.markNotificationReadId(id)
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.map {
                    if (it.id == id) it.copy(read = true) else it
                }
            )
        }
    }

    private suspend fun clearAll() {
        controlPanelRepository.clearAllNotifications()
        _uiState.update { it.copy(notifications = emptyList()) }
    }
}
