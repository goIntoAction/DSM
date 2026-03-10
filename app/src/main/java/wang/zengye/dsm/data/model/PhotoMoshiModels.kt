package wang.zengye.dsm.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 照片列表响应
 */
@JsonClass(generateAdapter = true)
data class PhotosDto(
    @Json(name = "data") val data: PhotosDataDto
)

@JsonClass(generateAdapter = true)
data class PhotosDataDto(
    @Json(name = "list") val list: List<PhotoItem>
)

@JsonClass(generateAdapter = true)
data class PhotoItem(
    @Json(name = "id") val id: Long = 0,
    @Json(name = "filename") val filename: String = "",
    @Json(name = "filesize") val filesize: Long = 0,
    @Json(name = "time") val time: Long = 0,
    @Json(name = "indexed_time") val indexedTime: String = "",
    @Json(name = "owner_user_id") val ownerUserId: Int = 0,
    @Json(name = "folder_id") val folderId: Long = 0,
    @Json(name = "type") val type: String = "photo",
    @Json(name = "additional") val additional: PhotoAdditionalItem? = null
) {
    val isVideo: Boolean
        get() = type == "video"
}

@JsonClass(generateAdapter = true)
data class PhotoAdditionalItem(
    @Json(name = "resolution") val resolution: PhotoResolutionItem? = null,
    @Json(name = "orientation") val orientation: Int = 0,
    @Json(name = "thumbnail") val thumbnail: PhotoThumbnailItem? = null,
    @Json(name = "address") val address: PhotoAddressItem? = null,
    @Json(name = "video_meta") val videoMeta: VideoMetaItem? = null,
    @Json(name = "video_convert") val videoConvert: List<VideoConvertItem>? = null
)

@JsonClass(generateAdapter = true)
data class PhotoResolutionItem(
    @Json(name = "width") val width: Int = 0,
    @Json(name = "height") val height: Int = 0
)

@JsonClass(generateAdapter = true)
data class PhotoThumbnailItem(
    @Json(name = "m") val m: String = "",
    @Json(name = "sm") val sm: String = "",
    @Json(name = "xl") val xl: String = "",
    @Json(name = "preview") val preview: String = "",
    @Json(name = "unit_id") val unitId: Long = 0,
    @Json(name = "cache_key") val cacheKey: String = ""
)

@JsonClass(generateAdapter = true)
data class PhotoAddressItem(
    @Json(name = "country") val country: String = "",
    @Json(name = "state") val state: String = "",
    @Json(name = "city") val city: String = "",
    @Json(name = "district") val district: String = "",
    @Json(name = "street") val street: String = ""
)

@JsonClass(generateAdapter = true)
data class VideoMetaItem(
    @Json(name = "duration") val duration: Int = 0,
    @Json(name = "framerate") val framerate: Int = 0,
    @Json(name = "video_codec") val videoCodec: String = "",
    @Json(name = "audio_codec") val audioCodec: String = "",
    @Json(name = "width") val width: Int = 0,
    @Json(name = "height") val height: Int = 0
)

@JsonClass(generateAdapter = true)
data class VideoConvertItem(
    @Json(name = "quality") val quality: String = "",
    @Json(name = "metadata") val metadata: VideoConvertMetadata? = null
)

@JsonClass(generateAdapter = true)
data class VideoConvertMetadata(
    @Json(name = "duration") val duration: Int = 0,
    @Json(name = "orientation") val orientation: Int = 0,
    @Json(name = "frame_bitrate") val frameBitrate: Int = 0,
    @Json(name = "video_bitrate") val videoBitrate: Int = 0,
    @Json(name = "audio_bitrate") val audioBitrate: Int = 0,
    @Json(name = "framerate") val framerate: Double = 0.0,
    @Json(name = "resolution_x") val resolutionX: Int = 0,
    @Json(name = "resolution_y") val resolutionY: Int = 0,
    @Json(name = "video_codec") val videoCodec: String = "",
    @Json(name = "audio_codec") val audioCodec: String = "",
    @Json(name = "container_type") val containerType: String = "",
    @Json(name = "video_profile") val videoProfile: Int = 0,
    @Json(name = "video_level") val videoLevel: Int = 0,
    @Json(name = "audio_frequency") val audioFrequency: Int = 0,
    @Json(name = "audio_channel") val audioChannel: Int = 0
)

/**
 * 相册列表响应
 */
@JsonClass(generateAdapter = true)
data class AlbumsDto(
    @Json(name = "data") val data: AlbumsDataDto
)

@JsonClass(generateAdapter = true)
data class AlbumsDataDto(
    @Json(name = "list") val list: List<AlbumItem>
)

@JsonClass(generateAdapter = true)
data class AlbumItem(
    @Json(name = "id") val id: Long = 0,
    @Json(name = "name") val name: String = "",
    @Json(name = "item_count") val itemCount: Int = 0,
    @Json(name = "create_time") val createTime: Long = 0,
    @Json(name = "shared") val shared: Boolean = false,
    @Json(name = "passhash") val passhash: Boolean = false,
    @Json(name = "temporary_shared") val temporaryShared: Boolean = false,
    @Json(name = "type") val type: String = "",
    @Json(name = "additional") val additional: AlbumAdditionalItem? = null
)

@JsonClass(generateAdapter = true)
data class AlbumAdditionalItem(
    @Json(name = "thumbnail") val thumbnail: AlbumThumbnailItemDto? = null
)

@JsonClass(generateAdapter = true)
data class AlbumThumbnailItemDto(
    @Json(name = "id") val id: Long = 0,
    @Json(name = "cache_key") val cacheKey: String = ""
)

/**
 * 时间线响应
 */
@JsonClass(generateAdapter = true)
data class TimelineDto(
    @Json(name = "data") val data: TimelineDataDto
)

@JsonClass(generateAdapter = true)
data class TimelineDataDto(
    @Json(name = "section") val section: List<TimelineSection>? = null,
    @Json(name = "list") val list: List<TimelineItemRaw>? = null
)

@JsonClass(generateAdapter = true)
data class TimelineSection(
    @Json(name = "list") val list: List<TimelineItemRaw>
)

@JsonClass(generateAdapter = true)
data class TimelineItemRaw(
    @Json(name = "day") val day: Int = 0,
    @Json(name = "month") val month: Int = 0,
    @Json(name = "year") val year: Int = 0,
    @Json(name = "item_count") val itemCount: Int = 0,
    @Json(name = "date") val date: String = "",
    @Json(name = "start_time") val startTime: Long = 0,
    @Json(name = "time") val time: Long = 0,
    @Json(name = "count") val count: Int = 0
)

/**
 * 地理编码响应
 */
@JsonClass(generateAdapter = true)
data class GeocodingDto(
    @Json(name = "data") val data: GeocodingDataDto
)

@JsonClass(generateAdapter = true)
data class GeocodingDataDto(
    @Json(name = "list") val list: List<GeocodingItemRaw>
)

@JsonClass(generateAdapter = true)
data class GeocodingItemRaw(
    @Json(name = "id") val id: Long = 0,
    @Json(name = "name") val name: String = "",
    @Json(name = "place_name") val placeName: String = "",
    @Json(name = "item_count") val itemCount: Int = 0,
    @Json(name = "count") val count: Int = 0
)

/**
 * 基础响应（用于删除等操作）
 */
@JsonClass(generateAdapter = true)
data class BasePhotoDto(
    @Json(name = "success") val success: Boolean = true,
    @Json(name = "error") val error: PhotoError? = null
)

@JsonClass(generateAdapter = true)
data class PhotoError(
    @Json(name = "code") val code: Int = 0,
    @Json(name = "msg") val msg: String = ""
)
