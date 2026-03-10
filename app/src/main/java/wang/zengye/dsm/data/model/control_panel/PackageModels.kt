package wang.zengye.dsm.data.model.control_panel

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 套件响应 - 已安装套件
 */
@JsonClass(generateAdapter = true)
data class InstalledPackagesDto(
    @Json(name = "data") val data: InstalledPackagesDataDto?
)

@JsonClass(generateAdapter = true)
data class InstalledPackagesDataDto(
    @Json(name = "packages") val packages: List<InstalledPackageDataDto>?
)

@JsonClass(generateAdapter = true)
data class InstalledPackageDataDto(
    @Json(name = "id") val id: String?,
    @Json(name = "package") val packageName: String?,
    @Json(name = "dname") val displayName: String?,
    @Json(name = "display_name") val displayNameAlt: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "version") val version: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "status") val status: String?,
    @Json(name = "url") val url: String?,
    @Json(name = "launchable") val launchable: Any?, // 可以是 Boolean 或 Array
    @Json(name = "additional") val additional: InstalledPackageAdditional?
)

@JsonClass(generateAdapter = true)
data class InstalledPackageAdditional(
    @Json(name = "status") val status: String?,
    @Json(name = "startable") val startable: Any?, // 可以是 Boolean 或 Array
    @Json(name = "thumbnail") val thumbnail: List<String>?
)

/**
 * 服务器套件响应（官方/社群）
 */
@JsonClass(generateAdapter = true)
data class ServerPackagesDto(
    @Json(name = "data") val data: ServerPackagesDataDto?
)

@JsonClass(generateAdapter = true)
data class ServerPackagesDataDto(
    @Json(name = "packages") val packages: List<ServerPackageDataDto>?,
    @Json(name = "data") val data: List<ServerPackageDataDto>?
)

@JsonClass(generateAdapter = true)
data class ServerPackageDataDto(
    @Json(name = "id") val id: String?,
    @Json(name = "package") val packageName: String?,
    @Json(name = "dname") val displayName: String?,
    @Json(name = "display_name") val displayNameAlt: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "version") val version: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "status") val status: String?,
    @Json(name = "url") val url: String?,
    @Json(name = "launchable") val launchable: Any?, // 可以是 Boolean 或 Array
    @Json(name = "installed") val installed: Any?, // 可以是 Boolean 或 Array
    @Json(name = "thumbnail") val thumbnail: List<String>?
)
