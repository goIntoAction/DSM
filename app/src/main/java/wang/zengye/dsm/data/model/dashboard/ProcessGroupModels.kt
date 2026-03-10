package wang.zengye.dsm.data.model.dashboard

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProcessGroupDto(
    @Json(name = "data") val data: ProcessGroupDataDto?
)

@JsonClass(generateAdapter = true)
data class ProcessGroupDataDto(
    @Json(name = "services") val services: List<ProcessService>?
)

@JsonClass(generateAdapter = true)
data class ProcessService(
    @Json(name = "name") val name: String?,
    @Json(name = "display_name") val displayName: String?,
    @Json(name = "status") val status: String?,
    @Json(name = "enabled") val enabled: Boolean?
)
