package wang.zengye.dsm.data.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * API 响应基类
 */
data class ApiResponse<T>(
    val success: Boolean = false,
    val error: ApiError? = null,
    val data: T? = null
)

data class ApiError(
    val code: Int = 0,
    val errors: List<String>? = null
)

/**
 * API 信息模型
 */
data class ApiInfo(
    val maxVersion: Int = 0,
    val minVersion: Int = 0,
    val path: String = ""
)

/**
 * 用户信息
 */
data class UserInfo(
    val name: String = "",
    val displayName: String = "",
    val email: String = "",
    val description: String = "",
    val expired: Boolean = false
)

/**
 * 服务器账户
 */
@JsonClass(generateAdapter = true)
data class ServerAccount(
    @Json(name = "name") val name: String = "",
    @Json(name = "host") val host: String = "",
    @Json(name = "account") val account: String = "",
    @Json(name = "password") val password: String = "",
    @Json(name = "isDefault") val isDefault: Boolean = false,
    @Json(name = "ssl") val ssl: Boolean = true,
    @Json(name = "port") val port: Int = 5000
)

/**
 * 系统信息
 */
data class SystemInfo(
    val model: String = "",
    val serial: String = "",
    val version: String = "",
    val versionString: String = "",
    val hostname: String = "",
    val uptime: Long = 0,
    val temperature: Int = 0,
    val temperatureWarn: Boolean = false
)

/**
 * 系统利用率
 */
data class SystemUtilization(
    val cpu: CpuUtilization? = null,
    val memory: MemoryUtilization? = null,
    val network: List<NetworkUtilization>? = null,
    val disk: List<DiskUtilization>? = null
)

data class CpuUtilization(
    val userLoad: Int = 0,
    val systemLoad: Int = 0,
    val idleLoad: Int = 0,
    val otherLoad: Int = 0,
    val utilization: Int = 0,
    val model: String = "",
    val cores: Int = 0
)

data class MemoryUtilization(
    val memorySize: Long = 0,
    val memoryUsage: Long = 0,
    val memoryAvailableSwap: Long = 0,
    val memoryCached: Long = 0,
    val utilization: Int = 0,
    val swap: SwapInfo? = null
)

data class SwapInfo(
    val total: Long = 0,
    val used: Long = 0
)

data class NetworkUtilization(
    val device: String = "",
    val rx: Long = 0,
    val tx: Long = 0,
    val name: String = ""
)

data class DiskUtilization(
    val device: String = "",
    val read: Long = 0,
    val write: Long = 0,
    val name: String = ""
)

/**
 * 存储信息
 */
data class StorageInfo(
    val volumes: List<VolumeInfo>? = null,
    val disks: List<DiskInfo>? = null,
    val storagePools: List<StoragePool>? = null
)

data class VolumeInfo(
    val volumeId: Int = 0,
    val volumeStatus: Int = 0,
    val name: String = "",
    val displayName: String = "",
    val sizeTotal: Long = 0,
    val sizeUsed: Long = 0,
    val sizeFree: Long = 0,
    val container: String = "",
    val filesystem: String = "",
    val volType: Int = 0,
    val volTypeString: String = "",
    val statusTooltipString: String = "",
    val degrade: Boolean = false,
    val readonly: Boolean = false
) {
    val usagePercent: Int
        get() = if (sizeTotal > 0) ((sizeUsed * 100) / sizeTotal).toInt() else 0
}

data class DiskInfo(
    val diskId: String = "",
    val name: String = "",
    val model: String = "",
    val vendor: String = "",
    val sizeTotal: Long = 0,
    val status: String = "",
    val temperature: Int = 0,
    val smartStatus: Int = 0,
    val healthStatus: Int = 0
)

data class StoragePool(
    val poolId: Int = 0,
    val name: String = "",
    val raidType: String = "",
    val sizeTotal: Long = 0,
    val sizeUsed: Long = 0,
    val status: Int = 0
)

/**
 * 共享文件夹
 */
data class SharedFolder(
    val name: String = "",
    val path: String = "",
    val desc: String = "",
    val volPath: String = "",
    val hidden: Boolean = false,
    val recyclebin: Boolean = false,
    val encryption: Boolean = false,
    val isAclMode: Boolean = false,
    val enableShareCow: Boolean = false,
    val enableShareCompress: Boolean = false,
    val shareQuota: Long = 0
)

// ==================== Moshi 模型 ====================

/**
 * 登录响应 (Moshi)
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
data class LoginDto(
    @com.squareup.moshi.Json(name = "success") val success: Boolean = false,
    @com.squareup.moshi.Json(name = "error") val error: ApiErrorDto? = null,
    @com.squareup.moshi.Json(name = "data") val data: LoginDataDto? = null,
    // sid 可能在顶层
    @com.squareup.moshi.Json(name = "sid") val sid: String? = null
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class LoginDataDto(
    @com.squareup.moshi.Json(name = "did") val did: String = "",
    @com.squareup.moshi.Json(name = "sid") val sid: String = "",
    @com.squareup.moshi.Json(name = "synotoken") val synoToken: String = ""
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class ApiErrorDto(
    @com.squareup.moshi.Json(name = "code") val code: Int = 0,
    @com.squareup.moshi.Json(name = "errors") val errors: List<String>? = null
)

/**
 * OTP二维码响应 (Moshi)
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
data class OtpQrCodeDto(
    @com.squareup.moshi.Json(name = "success") val success: Boolean = false,
    @com.squareup.moshi.Json(name = "error") val error: ApiErrorDto? = null,
    @com.squareup.moshi.Json(name = "data") val data: OtpQrCodeDataDto? = null
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class OtpQrCodeDataDto(
    @com.squareup.moshi.Json(name = "qr_code_url") val qrCodeUrl: String = "",
    @com.squareup.moshi.Json(name = "secret") val secret: String = ""
)

/**
 * OTP验证响应 (Moshi)
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
data class OtpAuthDto(
    @com.squareup.moshi.Json(name = "success") val success: Boolean = false,
    @com.squareup.moshi.Json(name = "error") val error: ApiErrorDto? = null
)
