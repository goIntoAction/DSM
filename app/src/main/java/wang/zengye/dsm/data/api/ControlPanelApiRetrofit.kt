package wang.zengye.dsm.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import wang.zengye.dsm.data.model.*
import wang.zengye.dsm.data.model.control_panel.*
import wang.zengye.dsm.data.model.dashboard.HardwarePowerDto
import wang.zengye.dsm.data.model.dashboard.SystemInfoDetailDto

/**
 * 使用 Retrofit + Moshi 的控制面板 API 接口
 */
interface ControlPanelApiRetrofit {

    // ==================== 用户管理 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getUsers(
        @Field("api") api: String = "SYNO.Core.User",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list",
        @Field("offset") offset: Int = 0,
        @Field("limit") limit: Int = -1,
        @Field("additional") additional: String = "[\"email\",\"description\",\"expired\"]"
    ): Response<UsersDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getUserDetail(
        @Field("api") api: String = "SYNO.Core.User",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get",
        @Field("name") username: String,
        @Field("additional") additional: String = "[\"description\",\"email\",\"expired\",\"passwd_expire\"]"
    ): Response<UserDetailDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun createUser(
        @Field("api") api: String = "SYNO.Core.User",
        @Field("version") version: String = "1",
        @Field("method") method: String = "create",
        @Field("name") username: String,
        @Field("passwd") password: String,
        @Field("description") description: String = "",
        @Field("email") email: String = "",
        @Field("expired") expired: Boolean = false
    ): Response<OperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun updateUser(
        @Field("api") api: String = "SYNO.Core.User",
        @Field("version") version: String = "1",
        @Field("method") method: String = "set",
        @Field("name") username: String,
        @Field("description") description: String? = null,
        @Field("email") email: String? = null,
        @Field("expired") expired: Boolean? = null,
        @Field("new_passwd") newPassword: String? = null
    ): Response<OperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun deleteUser(
        @Field("api") api: String = "SYNO.Core.User",
        @Field("version") version: String = "1",
        @Field("method") method: String = "delete",
        @Field("name") username: String
    ): Response<OperationDto>

    // ==================== 群组管理 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getGroups(
        @Field("api") api: String = "SYNO.Core.Group",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list",
        @Field("offset") offset: Int = 0,
        @Field("limit") limit: Int = -1,
        @Field("name_only") nameOnly: Boolean = false
    ): Response<GroupsDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getGroupsDetailed(
        @Field("api") api: String = "SYNO.Core.Group",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list",
        @Field("offset") offset: Int = 0,
        @Field("limit") limit: Int = -1,
        @Field("name_only") nameOnly: Boolean = false,
        @Field("additional") additional: String = "[\"description\",\"members\"]"
    ): Response<GroupsDetailedResponse>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun createGroup(
        @Field("api") api: String = "SYNO.Core.Group",
        @Field("version") version: String = "1",
        @Field("method") method: String = "create",
        @Field("name") name: String,
        @Field("description") description: String = "",
        @Field("member") members: String = "[]"
    ): Response<OperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun deleteGroup(
        @Field("api") api: String = "SYNO.Core.Group",
        @Field("version") version: String = "1",
        @Field("method") method: String = "delete",
        @Field("name") name: String
    ): Response<OperationDto>

    // ==================== 共享文件夹 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getShares(
        @Field("api") api: String = "SYNO.Core.Share",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list",
        @Field("shareType") shareType: String = "all",
        @Field("additional") additional: String = "[\"hidden\",\"recyclebin\",\"encryption\",\"is_acl_mode\"]"
    ): Response<ShareFoldersDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getShareDetail(
        @Field("api") api: String = "SYNO.Core.Share",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get",
        @Field("name") name: String
    ): Response<ShareDetailDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun deleteShare(
        @Field("api") api: String = "SYNO.Core.Share",
        @Field("version") version: String = "1",
        @Field("method") method: String = "delete",
        @Field("name") names: String
    ): Response<OperationDto>

    // ==================== 电源管理 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun shutdown(
        @Field("api") api: String = "SYNO.Core.System",
        @Field("version") version: String = "1",
        @Field("method") method: String = "shutdown"
    ): Response<OperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun reboot(
        @Field("api") api: String = "SYNO.Core.System",
        @Field("version") version: String = "1",
        @Field("method") method: String = "reboot"
    ): Response<OperationDto>

    // ==================== 终端设置 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getTerminalInfo(
        @Field("api") api: String = "SYNO.Core.Terminal",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get"
    ): Response<TerminalInfoDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun updateTerminalSettings(
        @Field("api") api: String = "SYNO.Core.Terminal",
        @Field("version") version: String = "1",
        @Field("method") method: String = "set",
        @Field("enable_ssh") enableSsh: Boolean,
        @Field("ssh_port") sshPort: Int = 22,
        @Field("allow_root_login") allowRootLogin: Boolean = false
    ): Response<OperationDto>

    // ==================== 防火墙 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getFirewall(
        @Field("api") api: String = "SYNO.Core.Security.Firewall",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list"
    ): Response<FirewallDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun deleteFirewallRule(
        @Field("api") api: String = "SYNO.Core.Security.Firewall.Rule",
        @Field("version") version: String = "1",
        @Field("method") method: String = "delete",
        @Field("id") ruleId: Int
    ): Response<OperationDto>

    // ==================== 计划任务 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getTaskScheduler(
        @Field("api") api: String = "SYNO.Core.TaskScheduler",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list"
    ): Response<TaskSchedulerDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun deleteTask(
        @Field("api") api: String = "SYNO.Core.TaskScheduler",
        @Field("version") version: String = "1",
        @Field("method") method: String = "delete",
        @Field("id") taskId: Int
    ): Response<OperationDto>

    // ==================== DDNS ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getDdnsProviders(
        @Field("api") api: String = "SYNO.Core.DDNS.Provider",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list"
    ): Response<DdnsProvidersResponse>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getDdnsRecords(
        @Field("api") api: String = "SYNO.Core.DDNS.Record",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list"
    ): Response<DdnsRecordsDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun createDdnsRecord(
        @Field("api") api: String = "SYNO.Core.DDNS.Record",
        @Field("version") version: String = "1",
        @Field("method") method: String = "create",
        @Field("provider") provider: String,
        @Field("hostname") hostname: String,
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<OperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun deleteDdnsRecord(
        @Field("api") api: String = "SYNO.Core.DDNS.Record",
        @Field("version") version: String = "1",
        @Field("method") method: String = "delete",
        @Field("id") recordId: String
    ): Response<OperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun updateDdnsRecord(
        @Field("api") api: String = "SYNO.Core.DDNS.Record",
        @Field("version") version: String = "1",
        @Field("method") method: String = "set",
        @Field("id") recordId: String,
        @Field("enable") enable: Boolean = true,
        @Field("provider") provider: String,
        @Field("hostname") hostname: String,
        @Field("username") username: String,
        @Field("password") password: String? = null
    ): Response<OperationDto>

    // ==================== 证书 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getCertificates(
        @Field("api") api: String = "SYNO.Core.Certificate",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list"
    ): Response<CertificatesDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun deleteCertificate(
        @Field("api") api: String = "SYNO.Core.Certificate",
        @Field("version") version: String = "1",
        @Field("method") method: String = "delete",
        @Field("id") certId: String
    ): Response<OperationDto>

    // ==================== 网络设置 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getNetworkInfo(
        @Field("api") api: String = "SYNO.Core.Network",
        @Field("version") version: String = "2",
        @Field("method") method: String = "get"
    ): Response<NetworkInfoDto>

    // ==================== 文件服务 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getFileServices(
        @Field("api") api: String = "SYNO.Core.FileServ",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get"
    ): Response<FileServicesDto>

    // ==================== 存储卷 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getVolumes(
        @Field("api") api: String = "SYNO.Storage.CGI.Storage",
        @Field("version") version: String = "1",
        @Field("method") method: String = "load_info"
    ): Response<VolumesDto>

    // ==================== 系统信息 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getSystemInfoDetail(
        @Field("api") api: String = "SYNO.Core.System",
        @Field("version") version: String = "2",
        @Field("method") method: String = "info",
        @Field("type") type: String = "all"
    ): Response<SystemInfoDetailDto>

    // ==================== 外部设备 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getExternalDevices(
        @Field("api") api: String = "SYNO.Core.ExternalDevice",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list"
    ): Response<ExternalDevicesDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun ejectDevice(
        @Field("api") api: String = "SYNO.Core.ExternalDevice",
        @Field("version") version: String = "1",
        @Field("method") method: String = "eject",
        @Field("device") device: String
    ): Response<OperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun formatDevice(
        @Field("api") api: String = "SYNO.Core.ExternalDevice",
        @Field("version") version: String = "1",
        @Field("method") method: String = "format",
        @Field("device") deviceId: String
    ): Response<OperationDto>

    // ==================== FTP 设置 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getFtpSettings(
        @Field("api") api: String = "SYNO.Core.FileServ.FTP",
        @Field("version") version: String = "3",
        @Field("method") method: String = "get"
    ): Response<FtpSettingsDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun setFtpEnabled(
        @Field("api") api: String = "SYNO.Core.FileServ.FTP",
        @Field("version") version: String = "3",
        @Field("method") method: String = "set",
        @Field("ftp_enabled") enabled: Boolean
    ): Response<OperationDto>

    // ==================== 防火墙操作 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun setFirewallEnabled(
        @Field("api") api: String = "SYNO.Core.Security.Firewall",
        @Field("version") version: String = "1",
        @Field("method") method: String = "set",
        @Field("enable") enabled: Boolean
    ): Response<OperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun setFirewallRule(
        @Field("api") api: String = "SYNO.Core.Security.Firewall.Rule",
        @Field("version") version: String = "1",
        @Field("method") method: String = "set",
        @Field("id") ruleId: Int,
        @Field("enable") enabled: Boolean
    ): Response<OperationDto>

    // ==================== 文件服务操作 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun setFileService(
        @Field("api") api: String = "SYNO.Core.FileServ",
        @Field("version") version: String = "1",
        @Field("method") method: String = "set",
        @Field("service") serviceType: String,
        @Field("enable") enabled: Boolean
    ): Response<OperationDto>

    // ==================== 共享文件夹操作 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun createShare(
        @Field("api") api: String = "SYNO.Core.Share",
        @Field("version") version: String = "1",
        @Field("method") method: String = "create",
        @Field("name") name: String,
        @Field("vol_path") volPath: String,
        @Field("description") description: String = "",
        @Field("hidden") hidden: Boolean = false,
        @Field("hide_unreadable") hideUnreadable: Boolean = false,
        @Field("enable_recycle_bin") enableRecycleBin: Boolean = false,
        @Field("recycle_bin_admin_only") recycleBinAdminOnly: Boolean = false,
        @Field("encryption") encryption: Boolean = false,
        @Field("password") password: String = "",
        @Field("enable_share_cow") enableShareCow: Boolean = false,
        @Field("enable_share_compress") enableShareCompress: Boolean = false,
        @Field("share_quota") shareQuota: Long = 0,
        @Field("old_name") oldName: String? = null
    ): Response<OperationDto>

    // ==================== 证书操作 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun setCertificateDefault(
        @Field("api") api: String = "SYNO.Core.Certificate",
        @Field("version") version: String = "1",
        @Field("method") method: String = "set",
        @Field("id") certId: String,
        @Field("default") isDefault: Boolean = true
    ): Response<OperationDto>

    // ==================== 媒体索引 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getMediaIndexStatus(
        @Field("api") api: String = "SYNO.Core.MediaIndexingService",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get"
    ): Response<MediaIndexDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun reindexMedia(
        @Field("api") api: String = "SYNO.Core.MediaIndexingService",
        @Field("version") version: String = "1",
        @Field("method") method: String = "reindex"
    ): Response<OperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun setMediaIndex(
        @Field("api") api: String = "SYNO.Core.MediaIndexingService",
        @Field("version") version: String = "1",
        @Field("method") method: String = "set",
        @Field("thumb_quality") thumbQuality: String = "medium",
        @Field("mobile_enabled") mobileEnabled: Boolean = true
    ): Response<OperationDto>

    // ==================== 硬件电源 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getHardwarePower(
        @Field("api") api: String = "SYNO.Core.Hardware.BeepControl",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get"
    ): Response<HardwarePowerDto>

    // ==================== 安全扫描 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getSecurityScan(
        @Field("api") api: String = "SYNO.Core.SecurityScan",
        @Field("version") version: String = "1",
        @Field("method") method: String = "check"
    ): Response<SecurityScanDto>

    // ==================== 电源操作 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun setAutoPowerOn(
        @Field("api") api: String = "SYNO.Core.Hardware.PowerRecovery",
        @Field("version") version: String = "1",
        @Field("method") method: String = "set",
        @Field("rc_power_config") config: Int
    ): Response<OperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun setBeepControl(
        @Field("api") api: String = "SYNO.Core.Hardware.BeepControl",
        @Field("version") version: String = "1",
        @Field("method") method: String = "set",
        @Field("poweron_beep") poweronBeep: Boolean,
        @Field("poweroff_beep") poweroffBeep: Boolean = false
    ): Response<OperationDto>

    // ==================== 文件服务配置 ====================

    // 获取 SMB 配置
    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getSmbConfig(
        @Field("api") api: String = "SYNO.Core.FileServ.SMB",
        @Field("version") version: String = "3",
        @Field("method") method: String = "get"
    ): Response<SmbConfigResponse>

    // 设置 SMB 配置
    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun setSmbConfig(
        @Field("api") api: String = "SYNO.Core.FileServ.SMB",
        @Field("version") version: String = "3",
        @Field("method") method: String = "set",
        @Field("enable_samba") enableSamba: Boolean,
        @Field("workgroup") workgroup: String,
        @Field("disable_shadow_copy") disableShadowCopy: Boolean,
        @Field("smb_transfer_log_enable") smbTransferLogEnable: Boolean
    ): Response<OperationDto>

    // 获取 AFP 配置
    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getAfpConfig(
        @Field("api") api: String = "SYNO.Core.FileServ.AFP",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get"
    ): Response<AfpConfigResponse>

    // 设置 AFP 配置
    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun setAfpConfig(
        @Field("api") api: String = "SYNO.Core.FileServ.AFP",
        @Field("version") version: String = "1",
        @Field("method") method: String = "set",
        @Field("enable_afp") enableAfp: Boolean
    ): Response<OperationDto>

    // 获取 NFS 配置
    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getNfsConfig(
        @Field("api") api: String = "SYNO.Core.FileServ.NFS",
        @Field("version") version: String = "2",
        @Field("method") method: String = "get"
    ): Response<NfsConfigResponse>

    // 设置 NFS 配置
    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun setNfsConfig(
        @Field("api") api: String = "SYNO.Core.FileServ.NFS",
        @Field("version") version: String = "2",
        @Field("method") method: String = "set",
        @Field("enable_nfs") enableNfs: Boolean,
        @Field("enable_nfs_v4") enableNfsV4: Boolean,
        @Field("enable_nfs_v4_1") enableNfsV41: Boolean,
        @Field("nfs_v4_domain") nfsV4Domain: String
    ): Response<OperationDto>

    // 获取 FTP 配置
    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getFtpConfig(
        @Field("api") api: String = "SYNO.Core.FileServ.FTP",
        @Field("version") version: String = "3",
        @Field("method") method: String = "get"
    ): Response<FtpConfigResponse>

    // 设置 FTP 配置
    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun setFtpConfig(
        @Field("api") api: String = "SYNO.Core.FileServ.FTP",
        @Field("version") version: String = "3",
        @Field("method") method: String = "set",
        @Field("enable_ftp") enableFtp: Boolean,
        @Field("enable_ftps") enableFtps: Boolean,
        @Field("timeout") timeout: Int,
        @Field("portnum") portnum: Int,
        @Field("enable_fxp") enableFxp: Boolean,
        @Field("enable_fips") enableFips: Boolean,
        @Field("enable_ascii") enableAscii: Boolean,
        @Field("utf8_mode") utf8Mode: Int
    ): Response<OperationDto>

    // 获取 SFTP 配置
    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getSftpConfig(
        @Field("api") api: String = "SYNO.Core.FileServ.FTP.SFTP",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get"
    ): Response<SftpConfigResponse>

    // 设置 SFTP 配置
    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun setSftpConfig(
        @Field("api") api: String = "SYNO.Core.FileServ.FTP.SFTP",
        @Field("version") version: String = "1",
        @Field("method") method: String = "set",
        @Field("enable") enable: Boolean,
        @Field("sftp_portnum") portnum: Int
    ): Response<OperationDto>

    // 获取 Syslog 客户端配置
    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getSyslogClientConfig(
        @Field("api") api: String = "SYNO.Core.SyslogClient.FileTransfer",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get"
    ): Response<SyslogClientConfigResponse>

    // 设置 Syslog 客户端配置
    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun setSyslogClientConfig(
        @Field("api") api: String = "SYNO.Core.SyslogClient.FileTransfer",
        @Field("version") version: String = "1",
        @Field("method") method: String = "set",
        @Field("cifs") cifs: Boolean,
        @Field("afp") afp: Boolean,
        @Field("ftp") ftp: Boolean
    ): Response<OperationDto>

    // 旧版兼容
    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun setSmbEnabled(
        @Field("api") api: String = "SYNO.Core.FileServ.SMB",
        @Field("version") version: String = "3",
        @Field("method") method: String = "set",
        @Field("enable") enabled: Boolean
    ): Response<OperationDto>

    // ==================== 任务计划操作 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun runTask(
        @Field("api") api: String = "SYNO.Core.TaskScheduler",
        @Field("version") version: String = "1",
        @Field("method") method: String = "run",
        @Field("id") taskId: Int
    ): Response<OperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun setTaskEnabled(
        @Field("api") api: String = "SYNO.Core.TaskScheduler",
        @Field("version") version: String = "1",
        @Field("method") method: String = "set",
        @Field("id") taskId: Int,
        @Field("enabled") enabled: Boolean
    ): Response<OperationDto>

    // ==================== 终端操作 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun disconnectTerminalSession(
        @Field("api") api: String = "SYNO.Core.Terminal",
        @Field("version") version: String = "1",
        @Field("method") method: String = "disconnect",
        @Field("session") sessionId: String
    ): Response<OperationDto>

    // ==================== 用户操作 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getUserGroups(
        @Field("api") api: String = "SYNO.Core.User.Group",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get",
        @Field("name") username: String
    ): Response<UserGroupsDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun saveUser(
        @Field("api") api: String = "SYNO.Core.User",
        @Field("version") version: String = "1",
        @Field("method") method: String = "set",
        @Field("name") username: String,
        @Field("description") description: String? = null,
        @Field("email") email: String? = null,
        @Field("passwd") password: String? = null,
        @Field("expired") expired: Boolean? = null
    ): Response<OperationDto>

    // ==================== 共享文件夹操作 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun cleanRecycleBin(
        @Field("api") api: String = "SYNO.Core.Share.RecycleBin",
        @Field("version") version: String = "1",
        @Field("method") method: String = "clear",
        @Field("share") shareId: String
    ): Response<OperationDto>

    // ==================== 通知管理 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun setNotificationEnabledId(
        @Field("api") api: String = "SYNO.Core.DSMNotify",
        @Field("version") version: String = "1",
        @Field("method") method: String = "notify",
        @Field("action") action: String = "apply",
        @Field("id") id: String
    ): Response<OperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun clearAllNotifications(
        @Field("api") api: String = "SYNO.Core.DSMNotify",
        @Field("version") version: String = "1",
        @Field("method") method: String = "notify",
        @Field("action") action: String = "apply",
        @Field("clean") clean: String = "all"
    ): Response<OperationDto>
}

// ==================== 额外的响应模型 ====================

@JsonClass(generateAdapter = true)
data class GroupsDetailedResponse(
    @Json(name = "data") val data: GroupsDetailedDataDto?
)

@JsonClass(generateAdapter = true)
data class GroupsDetailedDataDto(
    @Json(name = "groups") val groups: List<GroupDataDto>?
)

@JsonClass(generateAdapter = true)
data class DdnsProvidersResponse(
    @Json(name = "data") val data: DdnsProvidersDataDto?
)

@JsonClass(generateAdapter = true)
data class DdnsProvidersDataDto(
    @Json(name = "providers") val providers: List<DdnsProvider>?
)

@JsonClass(generateAdapter = true)
data class DdnsProvider(
    @Json(name = "name") val name: String?,
    @Json(name = "title") val title: String?
)

// ==================== 文件服务配置响应 ====================

@JsonClass(generateAdapter = true)
data class SmbConfigResponse(
    @Json(name = "data") val data: SmbConfigData?
)

@JsonClass(generateAdapter = true)
data class SmbConfigData(
    @Json(name = "enable_samba") val enableSamba: Boolean?,
    @Json(name = "workgroup") val workgroup: String?,
    @Json(name = "disable_shadow_copy") val disableShadowCopy: Boolean?,
    @Json(name = "smb_transfer_log_enable") val smbTransferLogEnable: Boolean?
)

@JsonClass(generateAdapter = true)
data class AfpConfigResponse(
    @Json(name = "data") val data: AfpConfigData?
)

@JsonClass(generateAdapter = true)
data class AfpConfigData(
    @Json(name = "enable_afp") val enableAfp: Boolean?
)

@JsonClass(generateAdapter = true)
data class NfsConfigResponse(
    @Json(name = "data") val data: NfsConfigData?
)

@JsonClass(generateAdapter = true)
data class NfsConfigData(
    @Json(name = "enable_nfs") val enableNfs: Boolean?,
    @Json(name = "enable_nfs_v4") val enableNfsV4: Boolean?,
    @Json(name = "enable_nfs_v4_1") val enableNfsV41: Boolean?,
    @Json(name = "nfs_v4_domain") val nfsV4Domain: String?
)

@JsonClass(generateAdapter = true)
data class FtpConfigResponse(
    @Json(name = "data") val data: FtpConfigData?
)

@JsonClass(generateAdapter = true)
data class FtpConfigData(
    @Json(name = "enable_ftp") val enableFtp: Boolean?,
    @Json(name = "enable_ftps") val enableFtps: Boolean?,
    @Json(name = "timeout") val timeout: Int?,
    @Json(name = "portnum") val portnum: Int?,
    @Json(name = "enable_fxp") val enableFxp: Boolean?,
    @Json(name = "enable_fips") val enableFips: Boolean?,
    @Json(name = "enable_ascii") val enableAscii: Boolean?,
    @Json(name = "utf8_mode") val utf8Mode: Int?
)

@JsonClass(generateAdapter = true)
data class SftpConfigResponse(
    @Json(name = "data") val data: SftpConfigData?
)

@JsonClass(generateAdapter = true)
data class SftpConfigData(
    @Json(name = "enable") val enable: Boolean?,
    @Json(name = "portnum") val portnum: Int?
)

@JsonClass(generateAdapter = true)
data class SyslogClientConfigResponse(
    @Json(name = "data") val data: SyslogClientConfigData?
)

@JsonClass(generateAdapter = true)
data class SyslogClientConfigData(
    @Json(name = "cifs") val cifs: Boolean?,
    @Json(name = "afp") val afp: Boolean?,
    @Json(name = "ftp") val ftp: Boolean?
)
