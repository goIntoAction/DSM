package wang.zengye.dsm.data.model.dashboard

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 硬件电源批量响应
 */
@JsonClass(generateAdapter = true)
data class HardwarePowerDto(
    @Json(name = "data") val data: HardwarePowerDataDto?
)

@JsonClass(generateAdapter = true)
data class HardwarePowerDataDto(
    @Json(name = "result") val result: List<HardwarePowerResult>?
)

@JsonClass(generateAdapter = true)
data class HardwarePowerResult(
    @Json(name = "api") val api: String?,
    @Json(name = "data") val data: HardwarePowerApiDataDto?
)

@JsonClass(generateAdapter = true)
data class HardwarePowerApiDataDto(
    @Json(name = "poweron_beep") val poweronBeep: Boolean?,
    @Json(name = "rc_power_config") val rcPowerConfig: Int?,
    @Json(name = "poweron_tasks") val poweronTasks: List<PowerScheduleTask>?,
    @Json(name = "poweroff_tasks") val poweroffTasks: List<PowerScheduleTask>?,
    @Json(name = "enable") val enable: Boolean?,
    @Json(name = "ups_name") val upsName: String?,
    @Json(name = "battery_capacity") val batteryCapacity: Int?
)

@JsonClass(generateAdapter = true)
data class PowerScheduleTask(
    @Json(name = "id") val id: Int?,
    @Json(name = "hour") val hour: Int?,
    @Json(name = "min") val min: Int?,
    @Json(name = "enabled") val enabled: Boolean?,
    @Json(name = "weekdays") val weekdays: String?
)
