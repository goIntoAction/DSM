package wang.zengye.dsm.data.model.control_panel

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 外部设备响应
 */
@JsonClass(generateAdapter = true)
data class ExternalDevicesDto(
    @Json(name = "data") val data: ExternalDevicesDataDto?
)

@JsonClass(generateAdapter = true)
data class ExternalDevicesDataDto(
    @Json(name = "result") val result: List<ExternalDeviceResult>?
)

@JsonClass(generateAdapter = true)
data class ExternalDeviceResult(
    @Json(name = "api") val api: String?,
    @Json(name = "data") val data: ExternalDeviceApiDataDto?
)

@JsonClass(generateAdapter = true)
data class ExternalDeviceApiDataDto(
    @Json(name = "devices") val devices: List<ExternalDeviceDataDto>?
)

@JsonClass(generateAdapter = true)
data class ExternalDeviceDataDto(
    @Json(name = "dev_id") val devId: String?,
    @Json(name = "dev_name") val devName: String?,
    @Json(name = "dev_type") val devType: String?,
    @Json(name = "vendor") val vendor: String?,
    @Json(name = "model") val model: String?,
    @Json(name = "size") val size: Long?,
    @Json(name = "status") val status: String?,
    @Json(name = "mount_point") val mountPoint: String?,
    @Json(name = "ejectable") val ejectable: Boolean?
)
