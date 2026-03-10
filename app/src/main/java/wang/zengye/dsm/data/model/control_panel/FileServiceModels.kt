package wang.zengye.dsm.data.model.control_panel

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 文件服务响应 (旧版，保留兼容)
 */
@JsonClass(generateAdapter = true)
data class FileServicesDto(
    @Json(name = "data") val data: FileServicesDataDto?
)

@JsonClass(generateAdapter = true)
data class FileServicesDataDto(
    @Json(name = "result") val result: List<FileServiceResult>?
)

@JsonClass(generateAdapter = true)
data class FileServiceResult(
    @Json(name = "api") val api: String?,
    @Json(name = "data") val data: FileServiceApiDataDto?
)

@JsonClass(generateAdapter = true)
data class FileServiceApiDataDto(
    @Json(name = "enable_samba") val enableSamba: Boolean?,
    @Json(name = "smb_port") val smbPort: Int?,
    @Json(name = "max_connections") val maxConnections: Int?,
    @Json(name = "enable_ftp") val enableFtp: Boolean?,
    @Json(name = "ftp_port") val ftpPort: Int?,
    @Json(name = "enable") val enable: Boolean?,
    @Json(name = "port") val port: Int?
)

/**
 * 文件服务配置 - 完整模型
 * 对应 API: SYNO.Core.FileServ.SMB, AFP, NFS, FTP, FTP.SFTP
 */

// ==================== SMB 配置 ====================
@JsonClass(generateAdapter = true)
data class SmbConfig(
    @Json(name = "enable_samba") val enableSamba: Boolean = false,
    @Json(name = "workgroup") val workgroup: String = "WORKGROUP",
    @Json(name = "disable_shadow_copy") val disableShadowCopy: Boolean = false,
    @Json(name = "smb_transfer_log_enable") val smbTransferLogEnable: Boolean = false
)

// ==================== AFP 配置 ====================
@JsonClass(generateAdapter = true)
data class AfpConfig(
    @Json(name = "enable_afp") val enableAfp: Boolean = false,
    @Json(name = "afp_transfer_log_enable") val afpTransferLogEnable: Boolean = false
)

// ==================== NFS 配置 ====================
@JsonClass(generateAdapter = true)
data class NfsConfig(
    @Json(name = "enable_nfs") val enableNfs: Boolean = false,
    @Json(name = "enable_nfs_v4") val enableNfsV4: Boolean = false,
    @Json(name = "enable_nfs_v4_1") val enableNfsV41: Boolean = false,
    @Json(name = "nfs_v4_domain") val nfsV4Domain: String = ""
)

// ==================== FTP 配置 ====================
@JsonClass(generateAdapter = true)
data class FtpConfig(
    @Json(name = "enable_ftp") val enableFtp: Boolean = false,
    @Json(name = "enable_ftps") val enableFtps: Boolean = false,
    @Json(name = "timeout") val timeout: Int = 300,
    @Json(name = "portnum") val portnum: Int = 21,
    @Json(name = "custom_port_range") val customPortRange: Boolean = false,
    @Json(name = "pasv_port_min") val pasvPortMin: Int = 0,
    @Json(name = "pasv_port_max") val pasvPortMax: Int = 0,
    @Json(name = "use_ext_ip") val useExtIp: Boolean = false,
    @Json(name = "ext_ip") val extIp: String = "",
    @Json(name = "enable_fxp") val enableFxp: Boolean = false,
    @Json(name = "enable_fips") val enableFips: Boolean = false,
    @Json(name = "enable_ascii") val enableAscii: Boolean = false,
    @Json(name = "utf8_mode") val utf8Mode: Int = 1 // 0=禁用, 1=自动, 2=强制
)

// ==================== SFTP 配置 ====================
@JsonClass(generateAdapter = true)
data class SftpConfig(
    @Json(name = "enable") val enable: Boolean = false,
    @Json(name = "portnum") val portnum: Int = 22
)

// ==================== Syslog 客户端配置 ====================
@JsonClass(generateAdapter = true)
data class SyslogClientConfig(
    @Json(name = "cifs") val cifs: Boolean = false,
    @Json(name = "afp") val afp: Boolean = false,
    @Json(name = "ftp") val ftp: Boolean = false
)