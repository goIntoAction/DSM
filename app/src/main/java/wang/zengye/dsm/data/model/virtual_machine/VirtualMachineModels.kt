package wang.zengye.dsm.data.model.virtual_machine

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 虚拟机磁盘信息
 */
@JsonClass(generateAdapter = true)
data class VmDisk(
    @Json(name = "size") val size: Long?
)

/**
 * 虚拟机网络信息
 */
@JsonClass(generateAdapter = true)
data class VmNetwork(
    @Json(name = "network") val network: String?
)

/**
 * 虚拟机列表项
 */
@JsonClass(generateAdapter = true)
data class VmItemDto(
    @Json(name = "guest_id") val guestId: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "status") val status: String?,
    @Json(name = "vcpu_num") val vcpuNum: Int?,
    @Json(name = "memory") val memory: Long?,
    @Json(name = "disks") val disks: List<VmDisk>?,
    @Json(name = "networks") val networks: List<VmNetwork>?,
    @Json(name = "autostart") val autostart: Boolean?
)

/**
 * 虚拟机列表数据
 */
@JsonClass(generateAdapter = true)
data class VmListDataDto(
    @Json(name = "guests") val guests: List<VmItemDto>?
)

/**
 * 虚拟机列表响应
 * API: SYNO.Virtualization.Cluster
 * Method: list
 */
@JsonClass(generateAdapter = true)
data class VmListDto(
    @Json(name = "data") val data: VmListDataDto?
)

/**
 * 虚拟机操作响应（start/stop/reset 等）
 */
@JsonClass(generateAdapter = true)
data class VmOperationDto(
    @Json(name = "success") val success: Boolean?
)
