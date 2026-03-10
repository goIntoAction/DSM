package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.data.model.control_panel.*
import wang.zengye.dsm.ui.base.BaseIntent

/**
 * 文件服务 Intent
 */
sealed class FileServiceIntent : BaseIntent {
    data object LoadServices : FileServiceIntent()
    data object SaveAll : FileServiceIntent()
    
    // SMB
    data class UpdateSmbEnabled(val enabled: Boolean) : FileServiceIntent()
    data class UpdateSmbWorkgroup(val workgroup: String) : FileServiceIntent()
    data class UpdateSmbShadowCopy(val disabled: Boolean) : FileServiceIntent()
    data class UpdateSmbTransferLog(val enabled: Boolean) : FileServiceIntent()
    
    // AFP
    data class UpdateAfpEnabled(val enabled: Boolean) : FileServiceIntent()
    data class UpdateAfpTransferLog(val enabled: Boolean) : FileServiceIntent()
    
    // NFS
    data class UpdateNfsEnabled(val enabled: Boolean) : FileServiceIntent()
    data class UpdateNfsV4Enabled(val enabled: Boolean) : FileServiceIntent()
    data class UpdateNfsV4Domain(val domain: String) : FileServiceIntent()
    
    // FTP
    data class UpdateFtpEnabled(val enabled: Boolean) : FileServiceIntent()
    data class UpdateFtpsEnabled(val enabled: Boolean) : FileServiceIntent()
    data class UpdateFtpTimeout(val timeout: Int) : FileServiceIntent()
    data class UpdateFtpPort(val port: Int) : FileServiceIntent()
    data class UpdateFtpFxp(val enabled: Boolean) : FileServiceIntent()
    data class UpdateFtpFips(val enabled: Boolean) : FileServiceIntent()
    data class UpdateFtpAscii(val enabled: Boolean) : FileServiceIntent()
    data class UpdateFtpUtf8Mode(val mode: Int) : FileServiceIntent()
    
    // SFTP
    data class UpdateSftpEnabled(val enabled: Boolean) : FileServiceIntent()
    data class UpdateSftpPort(val port: Int) : FileServiceIntent()
}