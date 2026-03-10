package wang.zengye.dsm.data.model.dashboard

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SmartHealthDto(
    @Json(name = "disk") val disk: List<SmartHealthDisk>?
)

@JsonClass(generateAdapter = true)
data class SmartHealthDisk(
    @Json(name = "device") val device: String?,
    @Json(name = "model") val model: String?,
    @Json(name = "serial") val serial: String?,
    @Json(name = "temp") val temp: Int?,
    @Json(name = "health") val health: String?,
    @Json(name = "status") val status: String?,
    @Json(name = "size") val size: Long?,
    @Json(name = "test_progress") val testProgress: Int?,
    @Json(name = "last_test_type") val lastTestType: String?,
    @Json(name = "last_test_time") val lastTestTime: Long?
)
