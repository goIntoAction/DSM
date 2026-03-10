package wang.zengye.dsm.ui.control_panel

import android.util.Log
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wang.zengye.dsm.data.api.ControlPanelApiRetrofit
import wang.zengye.dsm.data.model.control_panel.*
import wang.zengye.dsm.ui.base.BaseState
import wang.zengye.dsm.ui.base.BaseViewModel
import javax.inject.Inject

/**
 * 文件服务 UI 状态
 */
data class FileServiceState(
    override val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    override val error: String? = null,
    val smb: SmbConfig = SmbConfig(),
    val afp: AfpConfig = AfpConfig(),
    val nfs: NfsConfig = NfsConfig(),
    val ftp: FtpConfig = FtpConfig(),
    val sftp: SftpConfig = SftpConfig(),
    val syslogClient: SyslogClientConfig = SyslogClientConfig()
) : BaseState

@HiltViewModel
class FileServiceViewModel @Inject constructor(
    private val api: ControlPanelApiRetrofit
) : BaseViewModel<FileServiceState, FileServiceIntent, FileServiceEvent>() {

    companion object {
        private const val TAG = "FileServiceViewModel"
    }

    private val _state = MutableStateFlow(FileServiceState())
    override val state: StateFlow<FileServiceState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<FileServiceEvent>(extraBufferCapacity = 1)
    override val events = _events.asSharedFlow()

    init {
        sendIntent(FileServiceIntent.LoadServices)
    }

    override suspend fun processIntent(intent: FileServiceIntent) {
        when (intent) {
            is FileServiceIntent.LoadServices -> loadServices()
            is FileServiceIntent.SaveAll -> saveAll()
            
            // SMB
            is FileServiceIntent.UpdateSmbEnabled -> updateSmbEnabled(intent.enabled)
            is FileServiceIntent.UpdateSmbWorkgroup -> updateSmbWorkgroup(intent.workgroup)
            is FileServiceIntent.UpdateSmbShadowCopy -> updateSmbShadowCopy(intent.disabled)
            is FileServiceIntent.UpdateSmbTransferLog -> updateSmbTransferLog(intent.enabled)
            
            // AFP
            is FileServiceIntent.UpdateAfpEnabled -> updateAfpEnabled(intent.enabled)
            is FileServiceIntent.UpdateAfpTransferLog -> updateAfpTransferLog(intent.enabled)
            
            // NFS
            is FileServiceIntent.UpdateNfsEnabled -> updateNfsEnabled(intent.enabled)
            is FileServiceIntent.UpdateNfsV4Enabled -> updateNfsV4Enabled(intent.enabled)
            is FileServiceIntent.UpdateNfsV4Domain -> updateNfsV4Domain(intent.domain)
            
            // FTP
            is FileServiceIntent.UpdateFtpEnabled -> updateFtpEnabled(intent.enabled)
            is FileServiceIntent.UpdateFtpsEnabled -> updateFtpsEnabled(intent.enabled)
            is FileServiceIntent.UpdateFtpTimeout -> updateFtpTimeout(intent.timeout)
            is FileServiceIntent.UpdateFtpPort -> updateFtpPort(intent.port)
            is FileServiceIntent.UpdateFtpFxp -> updateFtpFxp(intent.enabled)
            is FileServiceIntent.UpdateFtpFips -> updateFtpFips(intent.enabled)
            is FileServiceIntent.UpdateFtpAscii -> updateFtpAscii(intent.enabled)
            is FileServiceIntent.UpdateFtpUtf8Mode -> updateFtpUtf8Mode(intent.mode)
            
            // SFTP
            is FileServiceIntent.UpdateSftpEnabled -> updateSftpEnabled(intent.enabled)
            is FileServiceIntent.UpdateSftpPort -> updateSftpPort(intent.port)
        }
    }

    private suspend fun loadServices() {
        _state.update { it.copy(isLoading = true, error = null) }

        try {
            // 并行获取所有配置
            val smbDeferred = viewModelScope.async { api.getSmbConfig() }
            val afpDeferred = viewModelScope.async { api.getAfpConfig() }
            val nfsDeferred = viewModelScope.async { api.getNfsConfig() }
            val ftpDeferred = viewModelScope.async { api.getFtpConfig() }
            val sftpDeferred = viewModelScope.async { api.getSftpConfig() }
            val syslogDeferred = viewModelScope.async { api.getSyslogClientConfig() }

            val smbResponse = smbDeferred.await()
            val afpResponse = afpDeferred.await()
            val nfsResponse = nfsDeferred.await()
            val ftpResponse = ftpDeferred.await()
            val sftpResponse = sftpDeferred.await()
            val syslogResponse = syslogDeferred.await()

            val smb = smbResponse.body()?.data?.let { data ->
                SmbConfig(
                    enableSamba = data.enableSamba ?: false,
                    workgroup = data.workgroup ?: "WORKGROUP",
                    disableShadowCopy = data.disableShadowCopy ?: false,
                    smbTransferLogEnable = data.smbTransferLogEnable ?: false
                )
            } ?: SmbConfig()

            val afp = afpResponse.body()?.data?.let { data ->
                AfpConfig(enableAfp = data.enableAfp ?: false)
            } ?: AfpConfig()

            val nfs = nfsResponse.body()?.data?.let { data ->
                NfsConfig(
                    enableNfs = data.enableNfs ?: false,
                    enableNfsV4 = data.enableNfsV4 ?: false,
                    enableNfsV41 = data.enableNfsV41 ?: false,
                    nfsV4Domain = data.nfsV4Domain ?: ""
                )
            } ?: NfsConfig()

            val ftp = ftpResponse.body()?.data?.let { data ->
                FtpConfig(
                    enableFtp = data.enableFtp ?: false,
                    enableFtps = data.enableFtps ?: false,
                    timeout = data.timeout ?: 300,
                    portnum = data.portnum ?: 21,
                    enableFxp = data.enableFxp ?: false,
                    enableFips = data.enableFips ?: false,
                    enableAscii = data.enableAscii ?: false,
                    utf8Mode = data.utf8Mode ?: 1
                )
            } ?: FtpConfig()

            val sftp = sftpResponse.body()?.data?.let { data ->
                SftpConfig(
                    enable = data.enable ?: false,
                    portnum = data.portnum ?: 22
                )
            } ?: SftpConfig()

            val syslogClient = syslogResponse.body()?.data?.let { data ->
                SyslogClientConfig(
                    cifs = data.cifs ?: false,
                    afp = data.afp ?: false,
                    ftp = data.ftp ?: false
                )
            } ?: SyslogClientConfig()

            _state.update {
                it.copy(
                    smb = smb,
                    afp = afp,
                    nfs = nfs,
                    ftp = ftp,
                    sftp = sftp,
                    syslogClient = syslogClient,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading file services", e)
            _state.update { it.copy(error = e.message, isLoading = false) }
            _events.emit(FileServiceEvent.ShowError(e.message ?: "加载失败"))
        }
    }

    private suspend fun saveAll() {
        _state.update { it.copy(isSaving = true) }

        try {
            val state = _state.value
            
            // 并行保存所有配置
            val smbDeferred = viewModelScope.async {
                api.setSmbConfig(
                    enableSamba = state.smb.enableSamba,
                    workgroup = state.smb.workgroup,
                    disableShadowCopy = state.smb.disableShadowCopy,
                    smbTransferLogEnable = state.syslogClient.cifs
                )
            }
            val afpDeferred = viewModelScope.async {
                api.setAfpConfig(enableAfp = state.afp.enableAfp)
            }
            val nfsDeferred = viewModelScope.async {
                api.setNfsConfig(
                    enableNfs = state.nfs.enableNfs,
                    enableNfsV4 = state.nfs.enableNfsV4,
                    enableNfsV41 = state.nfs.enableNfsV41,
                    nfsV4Domain = state.nfs.nfsV4Domain
                )
            }
            val ftpDeferred = viewModelScope.async {
                api.setFtpConfig(
                    enableFtp = state.ftp.enableFtp,
                    enableFtps = state.ftp.enableFtps,
                    timeout = state.ftp.timeout,
                    portnum = state.ftp.portnum,
                    enableFxp = state.ftp.enableFxp,
                    enableFips = state.ftp.enableFips,
                    enableAscii = state.ftp.enableAscii,
                    utf8Mode = state.ftp.utf8Mode
                )
            }
            val sftpDeferred = viewModelScope.async {
                api.setSftpConfig(
                    enable = state.sftp.enable,
                    portnum = state.sftp.portnum
                )
            }
            val syslogDeferred = viewModelScope.async {
                api.setSyslogClientConfig(
                    cifs = state.syslogClient.cifs,
                    afp = state.syslogClient.afp,
                    ftp = state.syslogClient.ftp
                )
            }

            val results = listOf(
                smbDeferred.await(),
                afpDeferred.await(),
                nfsDeferred.await(),
                ftpDeferred.await(),
                sftpDeferred.await(),
                syslogDeferred.await()
            )

            // 检查是否有失败
            val hasError = results.any { 
                it.body()?.success != true 
            }

            if (hasError) {
                _events.emit(FileServiceEvent.ShowError("部分设置保存失败"))
            } else {
                _events.emit(FileServiceEvent.SaveSuccess)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving file services", e)
            _events.emit(FileServiceEvent.ShowError(e.message ?: "保存失败"))
        } finally {
            _state.update { it.copy(isSaving = false) }
        }
    }

    // SMB 更新方法
    private fun updateSmbEnabled(enabled: Boolean) {
        _state.update { it.copy(smb = it.smb.copy(enableSamba = enabled)) }
    }

    private fun updateSmbWorkgroup(workgroup: String) {
        _state.update { it.copy(smb = it.smb.copy(workgroup = workgroup)) }
    }

    private fun updateSmbShadowCopy(disabled: Boolean) {
        _state.update { it.copy(smb = it.smb.copy(disableShadowCopy = disabled)) }
    }

    private fun updateSmbTransferLog(enabled: Boolean) {
        _state.update { it.copy(syslogClient = it.syslogClient.copy(cifs = enabled)) }
    }

    // AFP 更新方法
    private fun updateAfpEnabled(enabled: Boolean) {
        _state.update { it.copy(afp = it.afp.copy(enableAfp = enabled)) }
    }

    private fun updateAfpTransferLog(enabled: Boolean) {
        _state.update { it.copy(syslogClient = it.syslogClient.copy(afp = enabled)) }
    }

    // NFS 更新方法
    private fun updateNfsEnabled(enabled: Boolean) {
        _state.update { it.copy(nfs = it.nfs.copy(enableNfs = enabled)) }
    }

    private fun updateNfsV4Enabled(enabled: Boolean) {
        _state.update { it.copy(nfs = it.nfs.copy(enableNfsV4 = enabled, enableNfsV41 = enabled)) }
    }

    private fun updateNfsV4Domain(domain: String) {
        _state.update { it.copy(nfs = it.nfs.copy(nfsV4Domain = domain)) }
    }

    // FTP 更新方法
    private fun updateFtpEnabled(enabled: Boolean) {
        _state.update { it.copy(ftp = it.ftp.copy(enableFtp = enabled)) }
    }

    private fun updateFtpsEnabled(enabled: Boolean) {
        _state.update { it.copy(ftp = it.ftp.copy(enableFtps = enabled)) }
    }

    private fun updateFtpTimeout(timeout: Int) {
        _state.update { it.copy(ftp = it.ftp.copy(timeout = timeout)) }
    }

    private fun updateFtpPort(port: Int) {
        _state.update { it.copy(ftp = it.ftp.copy(portnum = port)) }
    }

    private fun updateFtpFxp(enabled: Boolean) {
        _state.update { it.copy(ftp = it.ftp.copy(enableFxp = enabled)) }
    }

    private fun updateFtpFips(enabled: Boolean) {
        _state.update { it.copy(ftp = it.ftp.copy(enableFips = enabled)) }
    }

    private fun updateFtpAscii(enabled: Boolean) {
        _state.update { it.copy(ftp = it.ftp.copy(enableAscii = enabled)) }
    }

    private fun updateFtpUtf8Mode(mode: Int) {
        _state.update { it.copy(ftp = it.ftp.copy(utf8Mode = mode)) }
    }

    // SFTP 更新方法
    private fun updateSftpEnabled(enabled: Boolean) {
        _state.update { it.copy(sftp = it.sftp.copy(enable = enabled)) }
    }

    private fun updateSftpPort(port: Int) {
        _state.update { it.copy(sftp = it.sftp.copy(portnum = port)) }
    }
}
