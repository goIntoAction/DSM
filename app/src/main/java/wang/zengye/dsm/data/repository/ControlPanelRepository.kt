package wang.zengye.dsm.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import wang.zengye.dsm.data.api.ControlPanelApiRetrofit
import wang.zengye.dsm.data.api.DdnsProvidersResponse
import wang.zengye.dsm.data.api.GroupsDetailedResponse
import wang.zengye.dsm.data.model.OperationDto
import wang.zengye.dsm.data.model.control_panel.*
import wang.zengye.dsm.data.model.dashboard.HardwarePowerDto
import wang.zengye.dsm.data.model.dashboard.SystemInfoDetailDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 控制面板 Repository
 * 使用 Retrofit + Moshi 进行 API 调用
 */
@Singleton
class ControlPanelRepository @Inject constructor(
    private val controlPanelApi: ControlPanelApiRetrofit
) : BaseRepository() {

    // ==================== 用户管理 ====================

    suspend fun getUsers(): Result<UsersDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.getUsers()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getUserDetail(username: String): Result<UserDetailDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.getUserDetail(username = username)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun createUser(
        username: String,
        password: String,
        description: String = "",
        email: String = "",
        expired: Boolean = false
    ): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.createUser(
                    username = username,
                    password = password,
                    description = description,
                    email = email,
                    expired = expired
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateUser(
        username: String,
        description: String? = null,
        email: String? = null,
        expired: Boolean? = null,
        newPassword: String? = null
    ): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.updateUser(
                    username = username,
                    description = description,
                    email = email,
                    expired = expired,
                    newPassword = newPassword
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteUser(username: String): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.deleteUser(username = username)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 群组管理 ====================

    suspend fun getGroups(): Result<GroupsDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.getGroups()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getGroupsDetailed(): Result<GroupsDetailedResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.getGroupsDetailed()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun createGroup(
        name: String,
        description: String = "",
        members: List<String> = emptyList()
    ): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.createGroup(
                    name = name,
                    description = description,
                    members = if (members.isEmpty()) "[]" else members.joinToString(",", "[", "]")
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteGroup(name: String): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.deleteGroup(name = name)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 共享文件夹 ====================

    suspend fun getShares(): Result<ShareFoldersDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.getShares()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getShareDetail(name: String): Result<ShareDetailDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.getShareDetail(name = name)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteShare(names: List<String>): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.deleteShare(names = names.joinToString(",", "[", "]"))
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 电源管理 ====================

    suspend fun shutdown(): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.shutdown()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun reboot(): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.reboot()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 终端设置 ====================

    suspend fun getTerminalInfo(): Result<TerminalInfoDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.getTerminalInfo()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateTerminalSettings(
        enableSsh: Boolean,
        sshPort: Int = 22,
        allowRootLogin: Boolean = false
    ): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.updateTerminalSettings(
                    enableSsh = enableSsh,
                    sshPort = sshPort,
                    allowRootLogin = allowRootLogin
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 防火墙 ====================

    suspend fun getFirewall(): Result<FirewallDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.getFirewall()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteFirewallRule(ruleId: Int): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.deleteFirewallRule(ruleId = ruleId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 计划任务 ====================

    suspend fun getTaskScheduler(): Result<TaskSchedulerDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.getTaskScheduler()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteTask(taskId: Int): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.deleteTask(taskId = taskId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== DDNS ====================

    suspend fun getDdnsProviders(): Result<DdnsProvidersResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.getDdnsProviders()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getDdnsRecords(): Result<DdnsRecordsDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.getDdnsRecords()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun createDdnsRecord(
        provider: String,
        hostname: String,
        username: String,
        password: String
    ): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.createDdnsRecord(
                    provider = provider,
                    hostname = hostname,
                    username = username,
                    password = password
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteDdnsRecord(recordId: String): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.deleteDdnsRecord(recordId = recordId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateDdnsRecord(
        recordId: String,
        provider: String,
        hostname: String,
        username: String,
        password: String? = null
    ): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.updateDdnsRecord(
                    recordId = recordId,
                    provider = provider,
                    hostname = hostname,
                    username = username,
                    password = password
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 证书 ====================

    suspend fun getCertificates(): Result<CertificatesDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.getCertificates()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteCertificate(certId: String): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.deleteCertificate(certId = certId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 网络设置 ====================

    suspend fun getNetworkInfo(): Result<NetworkInfoDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.getNetworkInfo()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 文件服务 ====================

    suspend fun getFileServices(): Result<FileServicesDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.getFileServices()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 存储卷 ====================

    suspend fun getVolumes(): Result<VolumesDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.getVolumes()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 系统信息 ====================

    suspend fun getSystemInfoDetail(): Result<SystemInfoDetailDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.getSystemInfoDetail()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 外部设备 ====================

    suspend fun getExternalDevices(): Result<ExternalDevicesDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.getExternalDevices()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun ejectDevice(device: String): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.ejectDevice(device = device)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun formatDevice(deviceId: String): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.formatDevice(deviceId = deviceId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== FTP 设置 ====================

    suspend fun getFtpSettings(): Result<FtpSettingsDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.getFtpSettings()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun setFtpEnabled(enabled: Boolean): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.setFtpEnabled(enabled = enabled)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 防火墙操作 ====================

    suspend fun setFirewallEnabled(enabled: Boolean): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.setFirewallEnabled(enabled = enabled)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun setFirewallRule(ruleId: Int, enabled: Boolean): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.setFirewallRule(ruleId = ruleId, enabled = enabled)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 文件服务操作 ====================

    suspend fun setFileService(serviceType: String, enabled: Boolean): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.setFileService(serviceType = serviceType, enabled = enabled)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 共享文件夹创建 ====================

    suspend fun createShare(
        name: String,
        volPath: String,
        description: String = "",
        hidden: Boolean = false,
        hideUnreadable: Boolean = false,
        enableRecycleBin: Boolean = false,
        recycleBinAdminOnly: Boolean = false,
        encryption: Boolean = false,
        password: String = "",
        enableShareCow: Boolean = false,
        enableShareCompress: Boolean = false,
        shareQuota: Long = 0,
        oldName: String? = null
    ): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.createShare(
                    name = name,
                    volPath = volPath,
                    description = description,
                    hidden = hidden,
                    hideUnreadable = hideUnreadable,
                    enableRecycleBin = enableRecycleBin,
                    recycleBinAdminOnly = recycleBinAdminOnly,
                    encryption = encryption,
                    password = password,
                    enableShareCow = enableShareCow,
                    enableShareCompress = enableShareCompress,
                    shareQuota = shareQuota,
                    oldName = oldName
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 证书操作 ====================

    suspend fun setCertificateDefault(certId: String): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.setCertificateDefault(certId = certId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 媒体索引 ====================

    suspend fun getMediaIndexStatus(): Result<MediaIndexDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.getMediaIndexStatus()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun reindexMedia(): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.reindexMedia()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun setMediaIndex(
        thumbQuality: String = "medium",
        mobileEnabled: Boolean = true
    ): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.setMediaIndex(
                    thumbQuality = thumbQuality,
                    mobileEnabled = mobileEnabled
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 硬件电源 ====================

    suspend fun getHardwarePower(): Result<PowerSettingsDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.getHardwarePower()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Result.success(parseHardwarePowerDto(body))
                    } else {
                        Result.failure(Exception("Empty response body"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun parseHardwarePowerDto(response: HardwarePowerDto): PowerSettingsDto {
        var autoPowerOn = false
        var beepOnAlert = false
        val schedules = mutableListOf<PowerScheduleItem>()
        var upsInfo: UpsInfoData? = null

        response.data?.result?.forEach { result ->
            when (result.api) {
                "SYNO.Core.Hardware.BeepControl" -> {
                    beepOnAlert = result.data?.poweronBeep ?: false
                }
                "SYNO.Core.Hardware.PowerRecovery" -> {
                    autoPowerOn = (result.data?.rcPowerConfig ?: 0) == 1
                }
                "SYNO.Core.Hardware.PowerSchedule" -> {
                    result.data?.poweronTasks?.forEach { task ->
                        schedules.add(PowerScheduleItem(
                            id = task.id ?: 0,
                            type = "power_on",
                            hour = task.hour ?: 0,
                            minute = task.min ?: 0,
                            enabled = task.enabled ?: false,
                            days = parseWeekdays(task.weekdays)
                        ))
                    }
                    result.data?.poweroffTasks?.forEach { task ->
                        schedules.add(PowerScheduleItem(
                            id = task.id ?: 0,
                            type = "power_off",
                            hour = task.hour ?: 0,
                            minute = task.min ?: 0,
                            enabled = task.enabled ?: false,
                            days = parseWeekdays(task.weekdays)
                        ))
                    }
                }
                "SYNO.Core.ExternalDevice.UPS" -> {
                    upsInfo = UpsInfoData(
                        connected = result.data?.enable ?: false,
                        model = result.data?.upsName ?: "",
                        batteryCharge = result.data?.batteryCapacity ?: 0
                    )
                }
            }
        }

        return PowerSettingsDto(
            autoPowerOn = autoPowerOn,
            beepOnAlert = beepOnAlert,
            upsInfo = upsInfo,
            schedules = schedules.sortedBy { it.id }
        )
    }

    private fun parseWeekdays(weekdaysStr: String?): List<Int> {
        if (weekdaysStr.isNullOrEmpty()) return listOf(0, 1, 2, 3, 4, 5, 6)
        return weekdaysStr.split(",").mapNotNull { it.toIntOrNull() }
    }

    // ==================== 安全扫描 ====================

    suspend fun getSecurityScan(): Result<SecurityScanDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.getSecurityScan()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 电源操作 ====================

    suspend fun setAutoPowerOn(enabled: Boolean): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.setAutoPowerOn(config = if (enabled) 1 else 0)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun setBeepOnAlert(enabled: Boolean): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.setBeepControl(poweronBeep = enabled)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== SMB 操作 ====================

    suspend fun setSmbEnabled(enabled: Boolean): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.setSmbEnabled(enabled = enabled)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 任务计划操作 ====================

    suspend fun runTask(taskId: Int): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.runTask(taskId = taskId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun setTaskEnabled(taskId: Int, enabled: Boolean): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.setTaskEnabled(taskId = taskId, enabled = enabled)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 终端操作 ====================

    suspend fun disconnectTerminalSession(sessionId: String): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.disconnectTerminalSession(sessionId = sessionId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 用户操作 ====================

    suspend fun getUserGroups(username: String): Result<UserGroupsDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.getUserGroups(username = username)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun saveUser(
        username: String,
        description: String? = null,
        email: String? = null,
        password: String? = null,
        expired: Boolean? = null
    ): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.saveUser(
                    username = username,
                    description = description,
                    email = email,
                    password = password,
                    expired = expired
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 共享文件夹操作 ====================

    suspend fun cleanRecycleBin(shareId: String): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.cleanRecycleBin(shareId = shareId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 辅助方法 ====================

    // ==================== 通知管理 ====================

    suspend fun markNotificationReadId(id: Long): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.setNotificationEnabledId(id = id.toString())
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun clearAllNotifications(): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = controlPanelApi.clearAllNotifications()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}