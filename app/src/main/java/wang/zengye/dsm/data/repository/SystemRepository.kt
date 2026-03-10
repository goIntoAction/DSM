package wang.zengye.dsm.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wang.zengye.dsm.data.api.SystemApiRetrofit
import wang.zengye.dsm.data.model.dashboard.ConnectedUsersDataDto
import wang.zengye.dsm.data.model.dashboard.DesktopInitdataDto
import wang.zengye.dsm.data.model.dashboard.HostnameDto
import wang.zengye.dsm.data.model.dashboard.NetworkInterfaceDto
import wang.zengye.dsm.data.model.dashboard.PowerActionDto
import wang.zengye.dsm.data.model.dashboard.SmartTestLogDto
import wang.zengye.dsm.data.model.dashboard.SmartTestActionDto
import wang.zengye.dsm.data.model.dashboard.ProcessListDto
import wang.zengye.dsm.data.model.dashboard.NotificationsDto
import wang.zengye.dsm.data.model.dashboard.SmartHealthDto
import wang.zengye.dsm.data.model.dashboard.LogsDto
import wang.zengye.dsm.data.model.dashboard.LogHistoryDto
import wang.zengye.dsm.data.model.dashboard.ProcessGroupDto
import wang.zengye.dsm.data.model.dashboard.StorageDto
import wang.zengye.dsm.data.model.dashboard.SystemInfoDto
import wang.zengye.dsm.data.model.dashboard.UtilizationDto
import wang.zengye.dsm.data.model.update.FirmwareVersionDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemRepository @Inject constructor(
    private val systemApiRetrofit: SystemApiRetrofit
) : BaseRepository() {

    /**
     * 获取系统信息
     */
    suspend fun getSystemInfo(): Result<SystemInfoDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = systemApiRetrofit.getSystemInfo()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取利用率
     */
    suspend fun getUtilization(): Result<UtilizationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = systemApiRetrofit.getUtilization()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取存储信息
     */
    suspend fun getStorageInfo(): Result<StorageDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = systemApiRetrofit.getStorageInfo()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取网络接口
     */
    suspend fun getNetworkInterfaces(): Result<NetworkInterfaceDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = systemApiRetrofit.getNetworkInterfaces()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取主机名
     */
    suspend fun getHostname(): Result<HostnameDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = systemApiRetrofit.getHostname()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取已连接用户
     */
    suspend fun getConnectedUsers(): Result<ConnectedUsersDataDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = systemApiRetrofit.getConnectedUsers()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取桌面初始化数据
     */
    suspend fun getDesktopInitdata(): Result<DesktopInitdataDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = systemApiRetrofit.getDesktopInitdata()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 关机
     */
    suspend fun shutdown(force: Boolean = false): Result<PowerActionDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = systemApiRetrofit.shutdown(force = force)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 重启
     */
    suspend fun reboot(force: Boolean = false): Result<PowerActionDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = systemApiRetrofit.reboot(force = force)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取 SMART 测试日志
     */
    suspend fun getSmartTestLog(device: String = ""): Result<SmartTestLogDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = systemApiRetrofit.getSmartTestLog(device = device)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 执行 SMART 测试
     */
    suspend fun doSmartTest(device: String, type: String): Result<SmartTestActionDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = systemApiRetrofit.doSmartTest(device = device, type = type)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取进程列表
     */
    suspend fun getProcessList(): Result<ProcessListDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = systemApiRetrofit.getProcessList()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取通知列表
     */
    suspend fun getNotifications(): Result<NotificationsDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = systemApiRetrofit.getNotifications()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取 SMART 健康信息
     */
    suspend fun getSmartHealth(): Result<SmartHealthDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = systemApiRetrofit.getSmartHealth()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取最新日志
     */
    suspend fun getLatestLogs(start: Int = 0, limit: Int = 50): Result<LogsDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = systemApiRetrofit.getLatestLogs(start = start, limit = limit)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取日志列表
     */
    suspend fun getLogs(start: Int = 0, limit: Int = 50, logType: String = "system"): Result<LogsDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = systemApiRetrofit.getLogs(start = start, limit = limit, logType = logType)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取日志历史
     */
    suspend fun getLogHistory(): Result<LogHistoryDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = systemApiRetrofit.getLogHistory()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取进程组
     */
    suspend fun getProcessGroup(node: String = "xnode-2572"): Result<ProcessGroupDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = systemApiRetrofit.getProcessGroup(node = node)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取固件版本信息
     */
    suspend fun getFirmwareVersion(): Result<FirmwareVersionDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = systemApiRetrofit.getFirmwareVersion()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 检查固件更新（与 getFirmwareVersion 相同）
     */
    suspend fun checkFirmwareUpdate(): Result<FirmwareVersionDto> = getFirmwareVersion()
}