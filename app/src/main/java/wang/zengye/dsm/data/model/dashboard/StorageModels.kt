package wang.zengye.dsm.data.model.dashboard

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

@JsonClass(generateAdapter = true)
data class StorageDto(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "data") val data: StorageData?
)

@JsonClass(generateAdapter = true)
data class StorageData(
    @Json(name = "volumes") val volumes: List<VolumeInfo>?,
    @Json(name = "disks") val disks: List<DiskInfo>?,
    @Json(name = "storagePools") val storagePools: List<StoragePoolInfo>?,
    @Json(name = "ssdCaches") val ssdCaches: List<SsdCacheInfo>?,
    @Json(name = "hotSpares") val hotSpares: List<HotSpareInfo>?,
    @Json(name = "env") val env: EnvInfo?
)

@JsonClass(generateAdapter = true)
data class StoragePoolInfo(
    @Json(name = "id") val id: String?,
    @Json(name = "num_id") val numId: Int?,
    @Json(name = "name") val name: String?,
    @Json(name = "status") val status: String?,
    @Json(name = "device_type") val deviceType: String?,
    @Json(name = "raidType") val raidType: String?,
    @Json(name = "size") val size: PoolSize?,
    @Json(name = "disks") val disks: List<String>?,
    @Json(name = "spares") val spares: List<String>?,
    @Json(name = "used_by") val usedBy: String?,
    @Json(name = "pool_path") val poolPath: String?
) {
    fun getTotalSize(): Long = size?.total?.toLongOrNull() ?: 0L
    fun getUsedSize(): Long = size?.used?.toLongOrNull() ?: 0L
}

@JsonClass(generateAdapter = true)
data class PoolSize(
    @Json(name = "total") val total: String?,
    @Json(name = "used") val used: String?,
    @Json(name = "avail") val avail: String?,
    @Json(name = "reusable") val reusable: String?
)

@JsonClass(generateAdapter = true)
data class SsdCacheInfo(
    @Json(name = "id") val id: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "status") val status: String?,
    @Json(name = "size") val size: SsdCacheSize?,
    @Json(name = "type") val type: String?,
    @Json(name = "read_only") val readOnly: Boolean?,
    @Json(name = "disks") val disks: List<String>?
) {
    fun getTotalSize(): Long = size?.total?.toLongOrNull() ?: 0L
    fun getUsedSize(): Long = size?.used?.toLongOrNull() ?: size?.reusable?.toLongOrNull() ?: 0L
}

@JsonClass(generateAdapter = true)
data class SsdCacheSize(
    @Json(name = "total") val total: String?,
    @Json(name = "used") val used: String?,
    @Json(name = "reusable") val reusable: String?
)

@JsonClass(generateAdapter = true)
data class HotSpareInfo(
    @Json(name = "id") val id: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "disk_id") val diskId: String?,
    @Json(name = "status") val status: String?,
    @Json(name = "size") val size: String?
)

@JsonClass(generateAdapter = true)
data class EnvInfo(
    @Json(name = "bay_number") val bayNumber: String?,
    @Json(name = "model") val model: String?,
    @Json(name = "temperature") val temperature: Int?
)

@JsonClass(generateAdapter = true)
data class VolumeInfo(
    @Json(name = "id") val id: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "num_id") val numId: Int?,
    @Json(name = "size") val size: VolumeSize?,
    @Json(name = "used") val used: String?,
    @Json(name = "status") val status: String?,
    @Json(name = "fs_type") val fsType: String?,
    @Json(name = "type") val type: String?,
    @Json(name = "vol_type") val volType: String?,
    @Json(name = "raid_type") val raidType: String?,
    @Json(name = "usage_percent") val usagePercent: Int?,
    @Json(name = "volume_id") val volumeId: String?,
    @Json(name = "deploy_path") val deployPath: String?,
    @Json(name = "volumeLabel") val volumeLabel: String?,
    @Json(name = "display_name") val displayName: String?,
    @Json(name = "size_total") val sizeTotal: String?,
    @Json(name = "size_used") val sizeUsed: String?,
    @Json(name = "size_avail") val sizeAvail: String?,
    @Json(name = "pool_path") val poolPath: String?,
    @Json(name = "container") val container: String?
) {
    fun getTotalSize(): Long = sizeTotal?.toLongOrNull() ?: size?.total?.toLongOrNull() ?: 0L
    fun getUsedSize(): Long = sizeUsed?.toLongOrNull() ?: size?.used?.toLongOrNull() ?: 0L
}

@JsonClass(generateAdapter = true)
data class VolumeSize(
    @Json(name = "total") val total: String?,
    @Json(name = "used") val used: String?,
    @Json(name = "avail") val avail: String?,
    @Json(name = "reusable") val reusable: String?
)

@JsonClass(generateAdapter = true)
data class DiskInfo(
    @Json(name = "id") val id: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "longName") val longName: String?,
    @Json(name = "model") val model: String?,
    @Json(name = "vendor") val vendor: String?,
    @Json(name = "serial") val serial: String?,
    @Json(name = "size") val size: String?,
    @Json(name = "size_total") val sizeTotal: String?,
    @Json(name = "temp") val temp: Int?,
    @Json(name = "status") val status: String?,
    @Json(name = "health") val health: String?,
    @Json(name = "overview_status") val overviewStatus: String?,
    @Json(name = "smart_status") val smartStatus: String?,
    @Json(name = "power_on_time") val powerOnTime: Long?,
    @Json(name = "diskType") val diskType: String?,
    @Json(name = "isSsd") val isSsd: Boolean?,
    @Json(name = "remain_life") val remainLifeRaw: RemainLifeRaw?,
    @Json(name = "unc") val unc: Int?,
    @Json(name = "firm") val firm: String?,
    @Json(name = "is4Kn") val is4Kn: Boolean?,
    @Json(name = "container") val container: DiskContainer?,
    @Json(name = "num_id") val numId: Int?,
    @Json(name = "used_by") val usedBy: String?
) {
    fun getSizeLong(): Long = sizeTotal?.toLongOrNull() ?: size?.toLongOrNull() ?: 0L
    
    /**
     * 获取剩余寿命百分比
     * API 可能返回 Int 或 Object，这里统一处理
     */
    val remainLife: Int?
        get() = remainLifeRaw?.value
}

/**
 * 用于处理 remain_life 字段的多态解析
 * API 可能返回 Int 或包含 value 字段的 Object
 */
@JsonClass(generateAdapter = true)
data class RemainLifeRaw(
    @Json(name = "value") val value: Int? = null,
    @Json(name = "remain_life") val remainLife: Int? = null
) {
    /**
     * 获取有效的剩余寿命值
     */
    fun getEffectiveValue(): Int? = value ?: remainLife
}

/**
 * RemainLifeRaw 的自定义适配器
 * 处理 API 返回 Int 或 Object 的情况
 */
class RemainLifeAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): RemainLifeRaw? {
        return when (reader.peek()) {
            JsonReader.Token.NUMBER -> {
                val value = reader.nextInt()
                RemainLifeRaw(value = value)
            }
            JsonReader.Token.BEGIN_OBJECT -> {
                val map: Map<String, Any?>? = reader.readJsonValue() as? Map<String, Any?>
                val lifeValue = (map?.get("value") as? Number)?.toInt()
                    ?: (map?.get("remain_life") as? Number)?.toInt()
                RemainLifeRaw(value = lifeValue)
            }
            JsonReader.Token.NULL -> {
                reader.nextNull<RemainLifeRaw?>()
                null
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }
    
    @ToJson
    fun toJson(writer: JsonWriter, value: RemainLifeRaw?) {
        if (value == null) {
            writer.nullValue()
        } else {
            writer.value(value.value ?: value.remainLife)
        }
    }
}

@JsonClass(generateAdapter = true)
data class DiskContainer(
    @Json(name = "str") val str: String?,
    @Json(name = "type") val type: String?
)
