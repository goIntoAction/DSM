package wang.zengye.dsm.data.model.dashboard

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SmartTestLogDto(
    @Json(name = "items") val items: List<SmartTestLogItem>?
)

@JsonClass(generateAdapter = true)
data class SmartTestLogItem(
    @Json(name = "id") val id: String?,
    @Json(name = "disk_id") val diskId: String?,
    @Json(name = "test_type") val testType: String?,
    @Json(name = "progress") val progress: Int?,
    @Json(name = "status") val status: String?,
    @Json(name = "result") val result: String?,
    @Json(name = "start_time") val startTime: String?,
    @Json(name = "end_time") val endTime: String?
)

@JsonClass(generateAdapter = true)
data class SmartTestActionDto(
    @Json(name = "success") val success: Boolean = true
)
