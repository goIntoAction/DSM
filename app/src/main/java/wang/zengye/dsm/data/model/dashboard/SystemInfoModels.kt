package wang.zengye.dsm.data.model.dashboard

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SystemInfoDto(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "data") val data: SystemInfoData?
)

@JsonClass(generateAdapter = true)
data class SystemInfoData(
    @Json(name = "hostname") val hostname: String?,
    @Json(name = "server_name") val serverName: String?,
    @Json(name = "host_name") val hostName: String?,
    @Json(name = "model") val model: String?,
    @Json(name = "up_time") val upTime: String?,
    @Json(name = "firmware_ver") val firmwareVer: String?,
    @Json(name = "version") val version: String?,
    @Json(name = "sys_temp") val sysTemp: Int?,
    @Json(name = "temperature_warning") val temperatureWarning: Boolean?
)

@JsonClass(generateAdapter = true)
data class HostnameDto(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "data") val data: HostnameData?
)

@JsonClass(generateAdapter = true)
data class HostnameData(
    @Json(name = "hostname") val hostname: String?,
    @Json(name = "server_name") val serverName: String?,
    @Json(name = "host_name") val hostName: String?,
    @Json(name = "name") val name: String?
)

@JsonClass(generateAdapter = true)
data class PowerActionDto(
    @Json(name = "success") val success: Boolean = true
)

/**
 * 系统详情响应（Control Panel -> 系统信息）
 */
@JsonClass(generateAdapter = true)
data class SystemInfoDetailDto(
    @Json(name = "data") val data: SystemInfoDetailDataDto?
)

@JsonClass(generateAdapter = true)
data class SystemInfoDetailDataDto(
    @Json(name = "model") val model: String?,
    @Json(name = "serial") val serial: String?,
    @Json(name = "hostname") val hostname: String?,
    @Json(name = "firmware_ver") val firmwareVer: String?,
    @Json(name = "buildnumber") val buildnumber: String?,
    @Json(name = "kernel_version") val kernelVersion: String?,
    @Json(name = "cpu_family") val cpuFamily: String?,
    @Json(name = "cpu_cores") val cpuCores: String?,
    @Json(name = "ram_size") val ramSize: Long?,
    @Json(name = "up_time") val upTime: String?,
    @Json(name = "sys_temp") val sysTemp: Int?
)
