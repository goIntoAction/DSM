package wang.zengye.dsm.data.model.dashboard

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 日志列表响应
 */
@JsonClass(generateAdapter = true)
data class LogsDto(
    @Json(name = "data") val data: LogsDataDto?
)

@JsonClass(generateAdapter = true)
data class LogsDataDto(
    @Json(name = "items") val items: List<LogItem>?
)

@JsonClass(generateAdapter = true)
data class LogItem(
    @Json(name = "id") val id: String?,
    @Json(name = "time") val time: String?,
    @Json(name = "level") val level: String?,
    @Json(name = "logtype") val logType: String?,
    @Json(name = "descr") val description: String?,
    @Json(name = "who") val who: String?
)

/**
 * 日志历史响应
 */
@JsonClass(generateAdapter = true)
data class LogHistoryDto(
    @Json(name = "data") val data: LogHistoryDataDto?
)

@JsonClass(generateAdapter = true)
data class LogHistoryDataDto(
    @Json(name = "items") val items: List<LogHistoryItem>?
)

@JsonClass(generateAdapter = true)
data class LogHistoryItem(
    @Json(name = "id") val id: String?,
    @Json(name = "time") val time: String?,
    @Json(name = "level") val level: String?,
    @Json(name = "type") val type: String?,
    @Json(name = "event") val event: String?,
    @Json(name = "user") val user: String?
)
