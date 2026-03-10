package wang.zengye.dsm.data.model.dashboard

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProcessListDto(
    @Json(name = "process") val process: List<ProcessItem>?
)

@JsonClass(generateAdapter = true)
data class ProcessItem(
    @Json(name = "pid") val pid: Int?,
    @Json(name = "name") val name: String?,
    @Json(name = "user") val user: String?,
    @Json(name = "cpu") val cpu: Double?,
    @Json(name = "memory") val memory: Long?,
    @Json(name = "memory_percent") val memoryPercent: Double?,
    @Json(name = "state") val state: String?,
    @Json(name = "command") val command: String?
)
