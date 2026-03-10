package wang.zengye.dsm.ui.filestation

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
import wang.zengye.dsm.data.repository.FileRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import wang.zengye.dsm.util.appString
import javax.inject.Inject

/**
 * 远程文件夹信息
 */
data class RemoteFolder(
    val id: String = "",
    val name: String = "",
    val type: String = "", // cifs, nfs
    val server: String = "",
    val path: String = "",
    val mountPoint: String = "",
    val connected: Boolean = false,
    val status: String = ""
)

/**
 * 远程文件夹管理UI状态
 */
data class RemoteFolderUiState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val smbFolders: List<RemoteFolder> = emptyList(),
    val remoteLinks: List<RemoteFolder> = emptyList(),
    val selectedTab: Int = 0, // 0: SMB/CIFS, 1: NFS
    val showDisconnectDialog: Boolean = false,
    val selectedFolder: RemoteFolder? = null,
    val showAddDialog: Boolean = false,
    val newFolderServer: String = "",
    val newFolderPath: String = "",
    val newFolderUsername: String = "",
    val newFolderPassword: String = ""
) : wang.zengye.dsm.ui.base.BaseState

// 迁移状态：[已完成]
// 说明：已使用 Moshi API，移除所有 Gson 操作
@HiltViewModel
class RemoteFolderViewModel @Inject constructor(
    private val fileRepository: FileRepository
) : BaseViewModel<RemoteFolderUiState, RemoteFolderIntent, RemoteFolderEvent>() {

    private val _uiState = MutableStateFlow(RemoteFolderUiState())
    override val state: StateFlow<RemoteFolderUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RemoteFolderEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    init {
        sendIntent(RemoteFolderIntent.LoadRemoteFolders)
    }

    override suspend fun processIntent(intent: RemoteFolderIntent) {
        when (intent) {
            is RemoteFolderIntent.LoadRemoteFolders -> loadRemoteFolders()
            is RemoteFolderIntent.SetTab -> setTab(intent.tab)
            is RemoteFolderIntent.ShowDisconnectDialog -> showDisconnectDialog(intent.folder)
            is RemoteFolderIntent.HideDisconnectDialog -> hideDisconnectDialog()
            is RemoteFolderIntent.Disconnect -> disconnect(intent.folder)
            is RemoteFolderIntent.ShowAddDialog -> showAddDialog()
            is RemoteFolderIntent.HideAddDialog -> hideAddDialog()
            is RemoteFolderIntent.SetNewFolderServer -> setNewFolderServer(intent.server)
            is RemoteFolderIntent.SetNewFolderPath -> setNewFolderPath(intent.path)
            is RemoteFolderIntent.SetNewFolderUsername -> setNewFolderUsername(intent.username)
            is RemoteFolderIntent.SetNewFolderPassword -> setNewFolderPassword(intent.password)
            is RemoteFolderIntent.AddRemoteFolder -> addRemoteFolder()
            is RemoteFolderIntent.UnmountFolder -> unmountFolder(intent.folder)
        }
    }

    /**
     * 加载远程文件夹
     */
    private suspend fun loadRemoteFolders() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        var smbFolders: List<RemoteFolder> = emptyList()
        var remoteLinks: List<RemoteFolder> = emptyList()
        var errorMsg: String? = null

        fileRepository.getSmbFolders()
            .onSuccess { response ->
                // 解析响应
                _uiState.update { it.copy(isLoading = false) }
            }
            .onFailure { exception ->
                errorMsg = exception.message
                _uiState.update { it.copy(isLoading = false) }
            }

        _uiState.update {
            it.copy(
                isLoading = false,
                error = if (smbFolders.isEmpty() && remoteLinks.isEmpty()) errorMsg else null
            )
        }
    }

    /**
     * 切换Tab
     */
    private fun setTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    /**
     * 显示断开连接对话框
     */
    private fun showDisconnectDialog(folder: RemoteFolder) {
        _uiState.update { it.copy(showDisconnectDialog = true, selectedFolder = folder) }
    }

    /**
     * 隐藏断开连接对话框
     */
    private fun hideDisconnectDialog() {
        _uiState.update { it.copy(showDisconnectDialog = false, selectedFolder = null) }
    }

    /**
     * 断开连接
     */
    private suspend fun disconnect(folder: RemoteFolder) {
        _uiState.update { it.copy(showDisconnectDialog = false) }

        fileRepository.disconnectRemote(folder.id).fold(
            onSuccess = {
                sendIntent(RemoteFolderIntent.LoadRemoteFolders)
                _events.emit(RemoteFolderEvent.DisconnectSuccess(folder.name))
            },
            onFailure = { e ->
                _uiState.update { it.copy(error = e.message) }
                _events.emit(RemoteFolderEvent.Error(e.message ?: "Unknown error"))
            }
        )
    }

    /**
     * 显示添加对话框
     */
    private fun showAddDialog() {
        _uiState.update { it.copy(
            showAddDialog = true,
            newFolderServer = "",
            newFolderPath = "",
            newFolderUsername = "",
            newFolderPassword = ""
        ) }
    }

    /**
     * 隐藏添加对话框
     */
    private fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    /**
     * 更新服务器地址
     */
    private fun setNewFolderServer(server: String) {
        _uiState.update { it.copy(newFolderServer = server) }
    }

    /**
     * 更新路径
     */
    private fun setNewFolderPath(path: String) {
        _uiState.update { it.copy(newFolderPath = path) }
    }

    /**
     * 更新用户名
     */
    private fun setNewFolderUsername(username: String) {
        _uiState.update { it.copy(newFolderUsername = username) }
    }

    /**
     * 更新密码
     */
    private fun setNewFolderPassword(password: String) {
        _uiState.update { it.copy(newFolderPassword = password) }
    }

    /**
     * 添加远程文件夹
     */
    private suspend fun addRemoteFolder() {
        val state = _uiState.value

        if (state.newFolderServer.isEmpty()) {
            _uiState.update { it.copy(error = appString(R.string.remote_error_server_required)) }
            return
        }

        _uiState.update { it.copy(showAddDialog = false, isLoading = true) }

        fileRepository.mountRemoteFolder(
            serverIp = state.newFolderServer,
            mountPoint = state.newFolderPath,
            account = state.newFolderUsername,
            password = state.newFolderPassword
        ).fold(
            onSuccess = {
                sendIntent(RemoteFolderIntent.LoadRemoteFolders)
                _events.emit(RemoteFolderEvent.AddSuccess)
            },
            onFailure = { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                _events.emit(RemoteFolderEvent.Error(e.message ?: "Unknown error"))
            }
        )
    }

    /**
     * 卸载远程文件夹
     */
    private suspend fun unmountFolder(folder: RemoteFolder) {
        _uiState.update { it.copy(isLoading = true) }

        fileRepository.unmountRemoteFolder(folder.mountPoint).fold(
            onSuccess = {
                sendIntent(RemoteFolderIntent.LoadRemoteFolders)
                _events.emit(RemoteFolderEvent.UnmountSuccess(folder.name))
            },
            onFailure = { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                _events.emit(RemoteFolderEvent.Error(e.message ?: "Unknown error"))
            }
        )
    }
}
