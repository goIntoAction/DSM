package wang.zengye.dsm.data.model.dashboard

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UtilizationDto(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "data") val data: UtilizationData?
)

@JsonClass(generateAdapter = true)
data class UtilizationData(
    @Json(name = "cpu") val cpu: CpuUtilization?,
    @Json(name = "memory") val memory: MemoryUtilization?,
    @Json(name = "network") val network: List<NetworkUtilization>?,
    @Json(name = "disk") val disk: DiskDto?,
    @Json(name = "space") val space: SpaceDto?
)

@JsonClass(generateAdapter = true)
data class DiskDto(
    @Json(name = "total") val total: DiskTotal?
)

@JsonClass(generateAdapter = true)
data class DiskTotal(
    @Json(name = "read_byte") val readByte: Long?,
    @Json(name = "write_byte") val writeByte: Long?
)

@JsonClass(generateAdapter = true)
data class SpaceDto(
    @Json(name = "total") val total: SpaceTotal?
)

@JsonClass(generateAdapter = true)
data class SpaceTotal(
    @Json(name = "read_byte") val readByte: Long?,
    @Json(name = "write_byte") val writeByte: Long?
)

@JsonClass(generateAdapter = true)
data class CpuUtilization(
    @Json(name = "user_load") val userLoad: Int?,
    @Json(name = "system_load") val systemLoad: Int?,
    @Json(name = "other_load") val otherLoad: Int?
)

@JsonClass(generateAdapter = true)
data class MemoryUtilization(
    @Json(name = "real_usage") val realUsage: Int?,
    @Json(name = "memory_size") val memorySize: Long?
)

@JsonClass(generateAdapter = true)
data class NetworkUtilization(
    @Json(name = "device") val device: String?,
    @Json(name = "rx") val rx: Long?,
    @Json(name = "tx") val tx: Long?
)

@JsonClass(generateAdapter = true)
data class DiskUtilization(
    @Json(name = "device") val device: String?,
    @Json(name = "read_access") val readAccess: Long?,
    @Json(name = "write_access") val writeAccess: Long?
)
