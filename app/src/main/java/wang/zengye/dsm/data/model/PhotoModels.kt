package wang.zengye.dsm.data.model



/**
 * 照片信息
 */
data class PhotoInfo(
    val id: Long = 0,
    val filename: String = "",
    val filesize: Long = 0,
    val time: Long = 0,
    val indexedTime: String = "",
    val ownerUserId: Int = 0,
    val folderId: Int = 0,
    val type: String = "photo", // photo / video
    val additional: PhotoAdditional? = null
) {
    val isVideo: Boolean
        get() = type == "video"
}

data class PhotoAdditional(
    val resolution: PhotoResolution? = null,
    val orientation: Int = 0,
    val thumbnail: PhotoThumbnail? = null,
    val address: PhotoAddress? = null,
    val videoMeta: VideoMeta? = null,
    val videoConvert: VideoConvert? = null
)

data class PhotoResolution(
    val width: Int = 0,
    val height: Int = 0
)

data class PhotoThumbnail(
    val m: String = "",  // 中等尺寸
    val sm: String = "", // 小尺寸
    val xl: String = "",  // 大尺寸
    val unitId: Long = 0,
    val cacheKey: String = ""
)

data class PhotoAddress(
    val country: String = "",
    val state: String = "",
    val city: String = "",
    val district: String = "",
    val street: String = "",
    val fullAddress: String = ""
)

data class VideoMeta(
    val duration: Int = 0,
    val framerate: Int = 0,
    val videoCodec: String = "",
    val audioCodec: String = "",
    val width: Int = 0,
    val height: Int = 0
)

data class VideoConvert(
    val status: String = "",
    val progress: Int = 0
)

/**
 * 相册信息
 */
data class AlbumInfo(
    val id: Long = 0,
    val name: String = "",
    val item_count: Int = 0,
    val shared: Boolean = false,
    val createTime: Long = 0,
    val passhash: Boolean = false,
    val temporary_shared: Boolean = false,
    val type: String = "", // normal / timeline
    val thumbnail: AlbumThumbnail? = null
)

data class AlbumThumbnail(
    val id: Long = 0,
    val cache_key: String = ""
)

/**
 * 时间线项
 */
data class TimelineItem(
    val time: Long = 0,
    val count: Int = 0,
    val photos: List<PhotoInfo> = emptyList()
)

/**
 * 地理编码项
 */
data class GeocodingItem(
    val id: Long = 0,
    val name: String = "",
    val count: Int = 0,
    val thumbnail: PhotoThumbnail? = null
)
