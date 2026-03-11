package wang.zengye.dsm.ui.terminal

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import wang.zengye.dsm.terminal.SshTerminalBridge
import wang.zengye.dsm.terminal.TerminalService
import wang.zengye.dsm.util.SettingsManager
import javax.inject.Inject

data class TerminalUiState(
    val connectionState: SshTerminalBridge.State = SshTerminalBridge.State.Idle,
    val showConnectionDialog: Boolean = false,
    val showDisconnectDialog: Boolean = false,
    val savedCredentials: SavedCredentials = SavedCredentials(),
    val isReconnecting: Boolean = false
)

data class SavedCredentials(
    val host: String = "",
    val username: String = "",
    val password: String = ""
)

@HiltViewModel
class TerminalViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    // 通过服务获取 bridge
    val bridge: SshTerminalBridge
        get() = TerminalService.instance?.getOrCreateBridge() ?: SshTerminalBridge()

    private val _uiState = MutableStateFlow(TerminalUiState())
    val uiState: StateFlow<TerminalUiState> = _uiState.asStateFlow()

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            // App 回到前台时，检查是否需要自动重连
            tryAutoReconnect()
        }
    }

    init {
        // 启动前台服务
        startTerminalService()

        // 监听服务的连接状态（服务可能需要一点时间启动）
        viewModelScope.launch {
            // 等待服务启动
            while (isActive && TerminalService.instance == null) {
                delay(50)
            }
            // 监听服务的连接状态
            TerminalService.instance?.connectionState?.collect { state ->
                _uiState.update { it.copy(connectionState = state) }
            }
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
    }

    private fun startTerminalService() {
        val intent = Intent(context, TerminalService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    override fun onCleared() {
        super.onCleared()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleObserver)
        // 注意：不在这里释放 bridge，因为服务持有它
    }

    private fun tryAutoReconnect() {
        TerminalService.instance?.tryAutoReconnect()
    }

    fun showConnectionDialog() {
        viewModelScope.launch {
            val rawHost = SettingsManager.host.first()
            val cleanHost = rawHost
                .removePrefix("https://")
                .removePrefix("http://")
                .substringBefore("/")
                .substringBefore(":")
            val username = SettingsManager.account.first()
            val password = SettingsManager.getPassword()
            _uiState.update {
                it.copy(
                    showConnectionDialog = true,
                    savedCredentials = SavedCredentials(cleanHost, username, password)
                )
            }
        }
    }

    fun hideConnectionDialog() {
        _uiState.update { it.copy(showConnectionDialog = false) }
    }

    fun showDisconnectDialog() {
        _uiState.update { it.copy(showDisconnectDialog = true) }
    }

    fun hideDisconnectDialog() {
        _uiState.update { it.copy(showDisconnectDialog = false) }
    }

    fun connect(host: String, port: Int, username: String, password: String) {
        _uiState.update { it.copy(showConnectionDialog = false) }
        TerminalService.instance?.connect(host, port, username, password)
        TerminalService.instance?.updateNotification(host, username)
    }

    fun disconnect() {
        TerminalService.instance?.disconnect()
        _uiState.update { it.copy(showDisconnectDialog = false) }
    }

    fun resize(cols: Int, rows: Int) {
        bridge.resize(cols, rows)
    }
}
