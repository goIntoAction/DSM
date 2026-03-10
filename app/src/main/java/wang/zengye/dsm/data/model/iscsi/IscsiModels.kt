package wang.zengye.dsm.data.model.iscsi

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 通用操作响应
 */
@JsonClass(generateAdapter = true)
data class OperationDto(
    @Json(name = "success") val success: Boolean?
)

/**
 * LUN 列表项
 */
@JsonClass(generateAdapter = true)
data class LunItemDto(
    @Json(name = "lun_id") val lunId: Int?,
    @Json(name = "name") val name: String?,
    @Json(name = "location") val location: String?,
    @Json(name = "size") val size: Long?,
    @Json(name = "used_size") val usedSize: Long?,
    @Json(name = "status") val status: String?,
    @Json(name = "is_thin_provision") val isThinProvision: Boolean?
)

/**
 * LUN 列表数据
 */
@JsonClass(generateAdapter = true)
data class LunListDataDto(
    @Json(name = "luns") val luns: List<LunItemDto>?
)

/**
 * LUN 列表响应
 * API: SYNO.Core.ISCSI.LUN
 * Method: list
 */
@JsonClass(generateAdapter = true)
data class LunListDto(
    @Json(name = "data") val data: LunListDataDto?
)

/**
 * LUN 创建响应
 */
@JsonClass(generateAdapter = true)
data class LunCreateDto(
    @Json(name = "success") val success: Boolean?,
    @Json(name = "data") val data: LunCreateDataDto?
)

@JsonClass(generateAdapter = true)
data class LunCreateDataDto(
    @Json(name = "lun_id") val lunId: Int?
)

/**
 * Target 列表项
 */
@JsonClass(generateAdapter = true)
data class TargetItemDto(
    @Json(name = "target_id") val targetId: Int?,
    @Json(name = "name") val name: String?,
    @Json(name = "iqn") val iqn: String?,
    @Json(name = "status") val status: String?,
    @Json(name = "enabled") val enabled: Boolean?,
    @Json(name = "connected_sessions") val connectedSessions: List<Any>?,
    @Json(name = "mapped_lun_ids") val mappedLunIds: List<Int>?
)

/**
 * Target 列表数据
 */
@JsonClass(generateAdapter = true)
data class TargetListDataDto(
    @Json(name = "targets") val targets: List<TargetItemDto>?
)

/**
 * Target 列表响应
 * API: SYNO.Core.ISCSI.Target
 * Method: list
 */
@JsonClass(generateAdapter = true)
data class TargetListDto(
    @Json(name = "data") val data: TargetListDataDto?
)

/**
 * Target 创建响应
 */
@JsonClass(generateAdapter = true)
data class TargetCreateDto(
    @Json(name = "success") val success: Boolean?,
    @Json(name = "data") val data: TargetCreateDataDto?
)

@JsonClass(generateAdapter = true)
data class TargetCreateDataDto(
    @Json(name = "target_id") val targetId: Int?
)

/**
 * 存储池列表项
 */
@JsonClass(generateAdapter = true)
data class StoragePoolItemDto(
    @Json(name = "pool_id") val poolId: Int?,
    @Json(name = "name") val name: String?,
    @Json(name = "size_total_byte") val sizeTotal: Long?,
    @Json(name = "size_free_byte") val sizeFree: Long?,
    @Json(name = "raid_type") val raidType: String?,
    @Json(name = "status") val status: String?
)

/**
 * 存储池列表数据
 */
@JsonClass(generateAdapter = true)
data class StoragePoolListDataDto(
    @Json(name = "pools") val pools: List<StoragePoolItemDto>?
)

/**
 * 存储池列表响应
 */
@JsonClass(generateAdapter = true)
data class StoragePoolListDto(
    @Json(name = "data") val data: StoragePoolListDataDto?
)
