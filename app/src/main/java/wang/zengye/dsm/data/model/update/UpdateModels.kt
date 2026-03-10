package wang.zengye.dsm.data.model.update

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 固件版本响应
 * API: SYNO.Core.Upgrade.Server
 * Method: check
 */
@JsonClass(generateAdapter = true)
data class FirmwareVersionDto(
    @Json(name = "firmware_ver") val firmwareVer: String?,
    @Json(name = "model") val model: String?,
    @Json(name = "serial") val serial: String?,
    @Json(name = "buildnumber") val buildNumber: String?,
    @Json(name = "update_available") val updateAvailable: Boolean?,
    @Json(name = "version") val version: String?,
    @Json(name = "release_date") val releaseDate: String?,
    @Json(name = "release_note") val releaseNote: String?,
    @Json(name = "reboot_needed") val rebootNeeded: Boolean?
)
