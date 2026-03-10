package wang.zengye.dsm.data.model.control_panel

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 共享文件夹列表响应
 */
@JsonClass(generateAdapter = true)
data class ShareFoldersDto(
    @Json(name = "data") val data: ShareFoldersDataDto?
)

@JsonClass(generateAdapter = true)
data class ShareFoldersDataDto(
    @Json(name = "shares") val shares: List<ShareDataDto>?
)

@JsonClass(generateAdapter = true)
data class ShareDataDto(
    @Json(name = "name") val name: String?,
    @Json(name = "path") val path: String?,
    @Json(name = "desc") val desc: String?,
    @Json(name = "vol_path") val volPath: String?,
    @Json(name = "additional") val additional: ShareAdditional?
)

@JsonClass(generateAdapter = true)
data class ShareAdditional(
    @Json(name = "hidden") val hidden: Boolean?,
    @Json(name = "recyclebin") val recyclebin: Boolean?,
    @Json(name = "encryption") val encryption: Boolean?,
    @Json(name = "is_acl_mode") val isAclMode: Boolean?,
    @Json(name = "hide_unreadable") val hideUnreadable: Boolean?,
    @Json(name = "recycle_bin_admin_only") val recycleBinAdminOnly: Boolean?,
    @Json(name = "enable_share_cow") val enableShareCow: Boolean?,
    @Json(name = "enable_share_compress") val enableShareCompress: Boolean?,
    @Json(name = "share_quota") val shareQuota: Long?,
    @Json(name = "share_quota_used") val shareQuotaUsed: Long?
)

/**
 * 共享文件夹详情响应
 */
@JsonClass(generateAdapter = true)
data class ShareDetailDto(
    @Json(name = "data") val data: ShareDetailDataDto?
)

@JsonClass(generateAdapter = true)
data class ShareDetailDataDto(
    @Json(name = "shareinfo") val shareInfo: ShareInfoDataDto?
)

@JsonClass(generateAdapter = true)
data class ShareInfoDataDto(
    @Json(name = "name") val name: String?,
    @Json(name = "path") val path: String?,
    @Json(name = "vol_path") val volPath: String?,
    @Json(name = "desc") val desc: String?,
    @Json(name = "quota_value") val quotaValue: Long?,
    @Json(name = "share_quota_used") val shareQuotaUsed: Long?,
    @Json(name = "hidden") val hidden: Boolean?,
    @Json(name = "hide_unreadable") val hideUnreadable: Boolean?,
    @Json(name = "enable_recycle_bin") val enableRecycleBin: Boolean?,
    @Json(name = "recycle_bin_admin_only") val recycleBinAdminOnly: Boolean?,
    @Json(name = "encryption") val encryption: Int?,
    @Json(name = "enable_share_cow") val enableShareCow: Boolean?,
    @Json(name = "enable_share_compress") val enableShareCompress: Boolean?
)

/**
 * 存储卷信息响应
 */
@JsonClass(generateAdapter = true)
data class VolumesDto(
    @Json(name = "data") val data: VolumesDataDto?
)

@JsonClass(generateAdapter = true)
data class VolumesDataDto(
    @Json(name = "volumes") val volumes: List<VolumeDataDto>?
)

@JsonClass(generateAdapter = true)
data class VolumeDataDto(
    @Json(name = "volume_id") val volumeId: Int?,
    @Json(name = "volume_path") val volumePath: String?,
    @Json(name = "display_name") val displayName: String?,
    @Json(name = "fs_type") val fsType: String?,
    @Json(name = "size_free_byte") val sizeFreeByte: Long?,
    @Json(name = "description") val description: String?
)

/**
 * 用户列表响应
 */
@JsonClass(generateAdapter = true)
data class UsersDto(
    @Json(name = "data") val data: UsersDataDto?
)

@JsonClass(generateAdapter = true)
data class UsersDataDto(
    @Json(name = "users") val users: List<UserDataDto>?
)

/**
 * 群组列表响应
 */
@JsonClass(generateAdapter = true)
data class GroupsDto(
    @Json(name = "data") val data: GroupsDataDto?
)

@JsonClass(generateAdapter = true)
data class GroupsDataDto(
    @Json(name = "groups") val groups: List<GroupDataDto>?
)

@JsonClass(generateAdapter = true)
data class GroupDataDto(
    @Json(name = "name") val name: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "gid") val gid: Int?,
    @Json(name = "members") val members: List<String>?,
    @Json(name = "is_built_in") val isBuiltIn: Boolean?
)

/**
 * FTP 设置响应
 */
@JsonClass(generateAdapter = true)
data class FtpSettingsDto(
    @Json(name = "data") val data: FtpSettingsDataDto?
)

@JsonClass(generateAdapter = true)
data class FtpSettingsDataDto(
    @Json(name = "enable_ftp") val enableFtp: Boolean?,
    @Json(name = "ftp_port") val ftpPort: Int?,
    @Json(name = "ftp_ssl_port") val ftpSslPort: Int?,
    @Json(name = "max_connections") val maxConnections: Int?,
    @Json(name = "current_connections") val currentConnections: Int?,
    @Json(name = "anonymous_login") val anonymousLogin: Boolean?,
    @Json(name = "ssl_enabled") val sslEnabled: Boolean?,
    @Json(name = "utf8_enabled") val utf8Enabled: Boolean?,
    @Json(name = "passive_mode") val passiveMode: Boolean?,
    @Json(name = "passive_port_range") val passivePortRange: String?
)

/**
 * 防火墙响应
 */
@JsonClass(generateAdapter = true)
data class FirewallDto(
    @Json(name = "data") val data: FirewallDataDto?
)

@JsonClass(generateAdapter = true)
data class FirewallDataDto(
    @Json(name = "enabled") val enabled: Boolean?,
    @Json(name = "rules") val rules: List<FirewallRuleDataDto>?
)

@JsonClass(generateAdapter = true)
data class FirewallRuleDataDto(
    @Json(name = "id") val id: Int?,
    @Json(name = "name") val name: String?,
    @Json(name = "action") val action: String?,
    @Json(name = "protocol") val protocol: String?,
    @Json(name = "src_ip") val srcIp: String?,
    @Json(name = "src_port") val srcPort: String?,
    @Json(name = "dst_port") val dstPort: String?,
    @Json(name = "enabled") val enabled: Boolean?,
    @Json(name = "order") val order: Int?
)

/**
 * 任务计划响应
 */
@JsonClass(generateAdapter = true)
data class TaskSchedulerDto(
    @Json(name = "data") val data: TaskSchedulerDataDto?
)

@JsonClass(generateAdapter = true)
data class TaskSchedulerDataDto(
    @Json(name = "tasks") val tasks: List<ScheduledTaskDataDto>?
)

@JsonClass(generateAdapter = true)
data class ScheduledTaskDataDto(
    @Json(name = "id") val id: Int?,
    @Json(name = "name") val name: String?,
    @Json(name = "type") val type: String?,
    @Json(name = "schedule") val schedule: String?,
    @Json(name = "enable") val enable: Boolean?,
    @Json(name = "last_trigger_time") val lastTriggerTime: Long?,
    @Json(name = "next_trigger_time") val nextTriggerTime: Long?,
    @Json(name = "status") val status: String?
)

/**
 * DDNS 响应
 */
@JsonClass(generateAdapter = true)
data class DdnsRecordsDto(
    @Json(name = "data") val data: DdnsRecordsDataDto?
)

@JsonClass(generateAdapter = true)
data class DdnsRecordsDataDto(
    @Json(name = "records") val records: List<DdnsRecordDataDto>?
)

@JsonClass(generateAdapter = true)
data class DdnsRecordDataDto(
    @Json(name = "id") val id: Int?,
    @Json(name = "hostname") val hostname: String?,
    @Json(name = "provider") val provider: String?,
    @Json(name = "username") val username: String?,
    @Json(name = "status") val status: String?,
    @Json(name = "last_update") val lastUpdate: Long?,
    @Json(name = "external_ip") val externalIp: String?
)

/**
 * 媒体索引响应
 */
@JsonClass(generateAdapter = true)
data class MediaIndexDto(
    @Json(name = "data") val data: MediaIndexDataDto?
)

@JsonClass(generateAdapter = true)
data class MediaIndexDataDto(
    @Json(name = "indexing") val indexing: Boolean?,
    @Json(name = "progress") val progress: Int?,
    @Json(name = "total") val total: Int?,
    @Json(name = "indexed") val indexed: Int?,
    @Json(name = "last_index") val lastIndex: Long?,
    @Json(name = "folders") val folders: List<MediaIndexFolderDataDto>?,
    @Json(name = "settings") val settings: MediaIndexSettingsDataDto?
)

@JsonClass(generateAdapter = true)
data class MediaIndexFolderDataDto(
    @Json(name = "path") val path: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "enabled") val enabled: Boolean?,
    @Json(name = "file_count") val fileCount: Int?
)

@JsonClass(generateAdapter = true)
data class MediaIndexSettingsDataDto(
    @Json(name = "auto_index") val autoIndex: Boolean?,
    @Json(name = "index_video") val indexVideo: Boolean?,
    @Json(name = "index_photo") val indexPhoto: Boolean?,
    @Json(name = "index_music") val indexMusic: Boolean?
)

/**
 * 网络信息响应
 */
@JsonClass(generateAdapter = true)
data class NetworkInfoDto(
    @Json(name = "data") val data: NetworkInfoDataDto?
)

@JsonClass(generateAdapter = true)
data class NetworkInfoDataDto(
    @Json(name = "ifaces") val ifaces: List<NetworkInterfaceDataDto>?,
    @Json(name = "gateway") val gateway: String?,
    @Json(name = "dns") val dns: List<String>?,
    @Json(name = "hostname") val hostname: String?
)

@JsonClass(generateAdapter = true)
data class NetworkInterfaceDataDto(
    @Json(name = "id") val id: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "type") val type: String?,
    @Json(name = "ip") val ip: String?,
    @Json(name = "mask") val mask: String?,
    @Json(name = "mac") val mac: String?,
    @Json(name = "status") val status: String?,
    @Json(name = "speed") val speed: String?,
    @Json(name = "up") val up: Boolean?
)

/**
 * 安全扫描响应
 */
@JsonClass(generateAdapter = true)
data class SecurityScanDto(
    @Json(name = "data") val data: SecurityScanDataDto?
)

@JsonClass(generateAdapter = true)
data class SecurityScanDataDto(
    @Json(name = "items") val items: List<SecurityCheckItemDataDto>?
)

@JsonClass(generateAdapter = true)
data class SecurityCheckItemDataDto(
    @Json(name = "id") val id: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "category") val category: String?,
    @Json(name = "status") val status: String?,
    @Json(name = "risk") val risk: String?,
    @Json(name = "desc") val desc: String?,
    @Json(name = "solution") val solution: String?
)

// ==================== 电源管理 ====================

/**
 * 批量 API 响应
 */
@JsonClass(generateAdapter = true)
data class BatchDto(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "data") val data: BatchDataDto? = null
)

@JsonClass(generateAdapter = true)
data class BatchDataDto(
    @Json(name = "result") val result: List<BatchResultItem>? = null
)

@JsonClass(generateAdapter = true)
data class BatchResultItem(
    @Json(name = "api") val api: String = "",
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "data") val data: Any? = null
)

/**
 * 蜂鸣控制响应
 */
@JsonClass(generateAdapter = true)
data class BeepControlDataDto(
    @Json(name = "poweron_beep") val poweronBeep: Boolean = false,
    @Json(name = "poweroff_beep") val poweroffBeep: Boolean = false,
    @Json(name = "fan_fail") val fanFail: Boolean = false,
    @Json(name = "volume_crash") val volumeCrash: Boolean = false
)

/**
 * 断电恢复响应
 */
@JsonClass(generateAdapter = true)
data class PowerRecoveryDataDto(
    @Json(name = "rc_power_config") val rcPowerConfig: Int = 0
)

/**
 * UPS 信息响应
 */
@JsonClass(generateAdapter = true)
data class UpsDataDto(
    @Json(name = "enable") val enable: Boolean = false,
    @Json(name = "ups_name") val upsName: String = "",
    @Json(name = "battery_capacity") val batteryCapacity: Int = 0
)

/**
 * 电源计划任务
 */
@JsonClass(generateAdapter = true)
data class PowerScheduleDataDto(
    @Json(name = "poweron_tasks") val poweronTasks: List<PowerTask>? = null,
    @Json(name = "poweroff_tasks") val poweroffTasks: List<PowerTask>? = null
)

@JsonClass(generateAdapter = true)
data class PowerTask(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "hour") val hour: Int = 0,
    @Json(name = "min") val minute: Int = 0,
    @Json(name = "enabled") val enabled: Boolean = false,
    @Json(name = "weekdays") val weekdays: String = ""
)

/**
 * 电源设置汇总响应（用于 ViewModel）
 */
data class PowerSettingsDto(
    val autoPowerOn: Boolean = false,
    val beepOnAlert: Boolean = false,
    val upsInfo: UpsInfoData? = null,
    val schedules: List<PowerScheduleItem> = emptyList()
)

data class UpsInfoData(
    val connected: Boolean = false,
    val model: String = "",
    val batteryCharge: Int = 0
)

data class PowerScheduleItem(
    val id: Int = 0,
    val type: String = "",
    val hour: Int = 0,
    val minute: Int = 0,
    val enabled: Boolean = false,
    val days: List<Int> = emptyList()
)

/**
 * 通用操作响应 (用于 shutdown, reboot, delete, update 等操作)
 */
@JsonClass(generateAdapter = true)
data class ControlPanelOperationDto(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "error") val error: ControlPanelError? = null
)

@JsonClass(generateAdapter = true)
data class ControlPanelError(
    @Json(name = "code") val code: Int = 0,
    @Json(name = "errors") val errors: List<String>? = null
)
