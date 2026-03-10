package wang.zengye.dsm.data.model.dashboard

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NotificationsDto(
    @Json(name = "notifications") val notifications: List<NotificationItem>?,
    @Json(name = "items") val items: List<NotificationItem>?
)

@JsonClass(generateAdapter = true)
data class NotificationItem(
    @Json(name = "notifyId") val notifyId: Long?,
    @Json(name = "id") val id: Long?,
    @Json(name = "time") val time: Long?,
    @Json(name = "timestamp") val timestamp: Long?,
    @Json(name = "title") val title: String?,
    @Json(name = "subject") val subject: String?,
    @Json(name = "message") val message: String?,
    @Json(name = "body") val body: String?,
    @Json(name = "msg") val msg: List<String>?,
    @Json(name = "className") val className: String?,
    @Json(name = "type") val type: String?,
    @Json(name = "level") val level: String?,
    @Json(name = "read") val read: Boolean?
)
