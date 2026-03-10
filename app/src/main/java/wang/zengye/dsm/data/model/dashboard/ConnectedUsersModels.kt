package wang.zengye.dsm.data.model.dashboard

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConnectedUsersDataDto(
    @Json(name = "items") val items: List<ConnectedUser>?
)

@JsonClass(generateAdapter = true)
data class ConnectedUser(
    @Json(name = "user") val user: String?
)
