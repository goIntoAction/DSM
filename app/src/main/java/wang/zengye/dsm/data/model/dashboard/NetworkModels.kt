package wang.zengye.dsm.data.model.dashboard

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkInterfaceDto(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "data") val data: List<NetworkInterface>?
)

@JsonClass(generateAdapter = true)
data class NetworkInterface(
    @Json(name = "ifname") val ifname: String?,
    @Json(name = "ip") val ip: String?,
    @Json(name = "mask") val mask: String?,
    @Json(name = "speed") val speed: Int?,
    @Json(name = "status") val status: String?,
    @Json(name = "type") val type: String?,
    @Json(name = "use_dhcp") val useDhcp: Boolean?
)

@JsonClass(generateAdapter = true)
data class NetworkUtilizationDataDto(
    @Json(name = "device") val device: String?,
    @Json(name = "rx") val rx: Long?,
    @Json(name = "tx") val tx: Long?
)
