package wang.zengye.dsm.data.model.control_panel

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 终端设置响应
 */
@JsonClass(generateAdapter = true)
data class TerminalInfoDto(
    @Json(name = "data") val data: TerminalInfoDataDto?
)

@JsonClass(generateAdapter = true)
data class TerminalInfoDataDto(
    @Json(name = "enable_ssh") val enableSsh: Boolean?,
    @Json(name = "ssh_port") val sshPort: Int?,
    @Json(name = "allow_root_login") val allowRootLogin: Boolean?,
    @Json(name = "ssh_key_enabled") val sshKeyEnabled: Boolean?,
    @Json(name = "max_connections") val maxConnections: Int?,
    @Json(name = "sessions") val sessions: List<TerminalSessionDataDto>?
)

@JsonClass(generateAdapter = true)
data class TerminalSessionDataDto(
    @Json(name = "id") val id: String?,
    @Json(name = "user") val user: String?,
    @Json(name = "ip") val ip: String?,
    @Json(name = "start_time") val startTime: Long?,
    @Json(name = "active") val active: Boolean?
)
