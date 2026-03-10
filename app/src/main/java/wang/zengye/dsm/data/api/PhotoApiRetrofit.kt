package wang.zengye.dsm.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import wang.zengye.dsm.data.model.*

/**
 * 使用 Retrofit + Moshi 的照片管理 API 接口
 * 支持 DSM 6 (Photo) 和 DSM 7 (Foto)
 */
interface PhotoApiRetrofit {

    // ==================== 时间线 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getTimelineDsm7(
        @Field("api") api: String = "SYNO.Foto.Browse.Timeline",
        @Field("version") version: String = "2",
        @Field("method") method: String = "get",
        @Field("timeline_group_unit") groupUnit: String = "day",
        @Field("start_time") startTime: Long? = null,
        @Field("end_time") endTime: Long? = null
    ): Response<TimelineDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getTimelineDsm6(
        @Field("api") api: String = "SYNO.Photo.Browse.Timeline",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get",
        @Field("timeline_group_unit") groupUnit: String = "day",
        @Field("start_time") startTime: Long? = null,
        @Field("end_time") endTime: Long? = null
    ): Response<TimelineDto>

    // ==================== 照片列表 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getPhotosDsm7(
        @Field("api") api: String = "SYNO.Foto.Browse.Item",
        @Field("version") version: String = "2",
        @Field("method") method: String = "list",
        @Field("offset") offset: Int = 0,
        @Field("limit") limit: Int = 5000,
        @Field("additional") additional: String = "[\"thumbnail\",\"resolution\",\"orientation\",\"video_convert\",\"video_meta\",\"address\"]",
        @Field("start_time") startTime: Long? = null,
        @Field("end_time") endTime: Long? = null,
        @Field("type") type: String? = null
    ): Response<PhotosDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getPhotosDsm6(
        @Field("api") api: String = "SYNO.Photo.Browse.Item",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list",
        @Field("offset") offset: Int = 0,
        @Field("limit") limit: Int = 5000,
        @Field("additional") additional: String = "[\"thumbnail\",\"resolution\",\"orientation\",\"video_convert\",\"video_meta\",\"address\"]",
        @Field("start_time") startTime: Long? = null,
        @Field("end_time") endTime: Long? = null,
        @Field("type") type: String? = null
    ): Response<PhotosDto>

    // ==================== 相册 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getAlbumsDsm7(
        @Field("api") api: String = "SYNO.Foto.Browse.Album",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list",
        @Field("offset") offset: Int = 0,
        @Field("limit") limit: Int = 5000,
        @Field("additional") additional: String = "[\"thumbnail\"]",
        @Field("sort_by") sortBy: String = "create_time",
        @Field("sort_direction") sortDirection: String = "desc",
        @Field("shared") shared: Boolean = false
    ): Response<AlbumsDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getAlbumsDsm6(
        @Field("api") api: String = "SYNO.Photo.Browse.Album",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list",
        @Field("offset") offset: Int = 0,
        @Field("limit") limit: Int = 5000,
        @Field("additional") additional: String = "[\"thumbnail\"]",
        @Field("sort_by") sortBy: String = "create_time",
        @Field("sort_direction") sortDirection: String = "desc",
        @Field("shared") shared: Boolean = false
    ): Response<AlbumsDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getAlbumPhotosDsm7(
        @Field("api") api: String = "SYNO.Foto.Browse.Album",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get",
        @Field("id") albumId: Long,
        @Field("offset") offset: Int = 0,
        @Field("limit") limit: Int = 5000,
        @Field("additional") additional: String = "[\"thumbnail\",\"resolution\",\"orientation\",\"video_convert\",\"video_meta\"]"
    ): Response<PhotosDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getAlbumPhotosDsm6(
        @Field("api") api: String = "SYNO.Photo.Browse.Album",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get",
        @Field("id") albumId: Long,
        @Field("offset") offset: Int = 0,
        @Field("limit") limit: Int = 5000,
        @Field("additional") additional: String = "[\"thumbnail\",\"resolution\",\"orientation\",\"video_convert\",\"video_meta\"]"
    ): Response<PhotosDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun createAlbumDsm7(
        @Field("api") api: String = "SYNO.Foto.Browse.Album",
        @Field("version") version: String = "1",
        @Field("method") method: String = "create",
        @Field("name") name: String
    ): Response<AlbumCreateResponse>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun createAlbumDsm6(
        @Field("api") api: String = "SYNO.Photo.Browse.Album",
        @Field("version") version: String = "1",
        @Field("method") method: String = "create",
        @Field("name") name: String
    ): Response<AlbumCreateResponse>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun deleteAlbumDsm7(
        @Field("api") api: String = "SYNO.Foto.Browse.Album",
        @Field("version") version: String = "1",
        @Field("method") method: String = "delete",
        @Field("id") albumId: Long
    ): Response<BasePhotoDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun deleteAlbumDsm6(
        @Field("api") api: String = "SYNO.Photo.Browse.Album",
        @Field("version") version: String = "1",
        @Field("method") method: String = "delete",
        @Field("id") albumId: Long
    ): Response<BasePhotoDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun addToAlbumDsm7(
        @Field("api") api: String = "SYNO.Foto.Browse.Album",
        @Field("version") version: String = "1",
        @Field("method") method: String = "add_item",
        @Field("id") albumId: Long,
        @Field("item") item: String
    ): Response<BasePhotoDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun addToAlbumDsm6(
        @Field("api") api: String = "SYNO.Photo.Browse.Album",
        @Field("version") version: String = "1",
        @Field("method") method: String = "add_item",
        @Field("id") albumId: Long,
        @Field("item") item: String
    ): Response<BasePhotoDto>

    // ==================== 文件夹 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getFoldersDsm7(
        @Field("api") api: String = "SYNO.Foto.Browse.Folder",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list",
        @Field("id") parentId: Long = 0,
        @Field("offset") offset: Int = 0,
        @Field("limit") limit: Int = 5000,
        @Field("additional") additional: String = "[\"thumbnail\"]"
    ): Response<FoldersResponse>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getFoldersDsm6(
        @Field("api") api: String = "SYNO.Photo.Browse.Folder",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list",
        @Field("id") parentId: Long = 0,
        @Field("offset") offset: Int = 0,
        @Field("limit") limit: Int = 5000,
        @Field("additional") additional: String = "[\"thumbnail\"]"
    ): Response<FoldersResponse>

    // ==================== 地理编码 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getGeocodingDsm7(
        @Field("api") api: String = "SYNO.Foto.Browse.Geocoding",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list",
        @Field("offset") offset: Int = 0,
        @Field("limit") limit: Int = 5000,
        @Field("additional") additional: String = "[\"thumbnail\"]"
    ): Response<GeocodingDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getGeocodingDsm6(
        @Field("api") api: String = "SYNO.Photo.Browse.Geocoding",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list",
        @Field("offset") offset: Int = 0,
        @Field("limit") limit: Int = 5000,
        @Field("additional") additional: String = "[\"thumbnail\"]"
    ): Response<GeocodingDto>

    // ==================== 标签 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getTagsDsm7(
        @Field("api") api: String = "SYNO.Foto.Browse.GeneralTag",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list",
        @Field("offset") offset: Int = 0,
        @Field("limit") limit: Int = 5000,
        @Field("additional") additional: String = "[\"thumbnail\"]"
    ): Response<TagsResponse>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getTagsDsm6(
        @Field("api") api: String = "SYNO.Photo.Browse.GeneralTag",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list",
        @Field("offset") offset: Int = 0,
        @Field("limit") limit: Int = 5000,
        @Field("additional") additional: String = "[\"thumbnail\"]"
    ): Response<TagsResponse>

    // ==================== 最近添加 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getRecentlyAddedDsm7(
        @Field("api") api: String = "SYNO.Foto.Browse.RecentlyAdded",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list",
        @Field("offset") offset: Int = 0,
        @Field("limit") limit: Int = 5000,
        @Field("additional") additional: String = "[\"thumbnail\",\"resolution\",\"orientation\",\"video_convert\",\"video_meta\"]"
    ): Response<PhotosDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getRecentlyAddedDsm6(
        @Field("api") api: String = "SYNO.Photo.Browse.RecentlyAdded",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list",
        @Field("offset") offset: Int = 0,
        @Field("limit") limit: Int = 5000,
        @Field("additional") additional: String = "[\"thumbnail\",\"resolution\",\"orientation\",\"video_convert\",\"video_meta\"]"
    ): Response<PhotosDto>

    // ==================== 照片上传 (个人空间) ====================

    /**
     * 上传照片到个人空间 (DSM 7)
     * API: SYNO.Foto.Upload.Item
     */
    @Multipart
    @POST("entry.cgi")
    suspend fun uploadPhotoDsm7(
        @Part("api") api: RequestBody,
        @Part("version") version: RequestBody,
        @Part("method") method: RequestBody,
        @Part("path") path: RequestBody,
        @Part("folder_id") folderId: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<PhotoUploadResponse>

    /**
     * 上传照片到个人空间 (DSM 6)
     * API: SYNO.Photo.Upload
     */
    @Multipart
    @POST("entry.cgi")
    suspend fun uploadPhotoDsm6(
        @Part("api") api: RequestBody,
        @Part("version") version: RequestBody,
        @Part("method") method: RequestBody,
        @Part("path") path: RequestBody,
        @Part("folder_id") folderId: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<PhotoUploadResponse>

    // ==================== 照片上传 (共享空间) ====================

    /**
     * 上传照片到共享空间 (DSM 7)
     * API: SYNO.FotoTeam.Upload.Item
     */
    @Multipart
    @POST("entry.cgi")
    suspend fun uploadPhotoToTeamDsm7(
        @Part("api") api: RequestBody,
        @Part("version") version: RequestBody,
        @Part("method") method: RequestBody,
        @Part("path") path: RequestBody,
        @Part("folder_id") folderId: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<PhotoUploadResponse>

    /**
     * 上传照片到共享空间 (DSM 6)
     * API: SYNO.PhotoTeam.Upload
     */
    @Multipart
    @POST("entry.cgi")
    suspend fun uploadPhotoToTeamDsm6(
        @Part("api") api: RequestBody,
        @Part("version") version: RequestBody,
        @Part("method") method: RequestBody,
        @Part("path") path: RequestBody,
        @Part("folder_id") folderId: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<PhotoUploadResponse>
}

// ==================== 额外的响应模型 ====================

@JsonClass(generateAdapter = true)
data class AlbumCreateResponse(
    @Json(name = "success") val success: Boolean = true,
    @Json(name = "error") val error: PhotoError? = null,
    @Json(name = "data") val data: AlbumCreateDataDto? = null
)

@JsonClass(generateAdapter = true)
data class AlbumCreateDataDto(
    @Json(name = "id") val id: Long = 0,
    @Json(name = "name") val name: String = ""
)

@JsonClass(generateAdapter = true)
data class FoldersResponse(
    @Json(name = "data") val data: FoldersDataDto
)

@JsonClass(generateAdapter = true)
data class FoldersDataDto(
    @Json(name = "list") val list: List<FolderItemDto>
)

@JsonClass(generateAdapter = true)
data class FolderItemDto(
    @Json(name = "id") val id: Long = 0,
    @Json(name = "name") val name: String = "",
    @Json(name = "parent") val parent: Long = 0,
    @Json(name = "type") val type: String = "",
    @Json(name = "item_count") val itemCount: Int = 0,
    @Json(name = "additional") val additional: FolderAdditionalItemDto? = null
)

@JsonClass(generateAdapter = true)
data class FolderAdditionalItemDto(
    @Json(name = "thumbnail") val thumbnail: AlbumThumbnailItemDto? = null
)

@JsonClass(generateAdapter = true)
data class TagsResponse(
    @Json(name = "data") val data: TagsDataDto
)

@JsonClass(generateAdapter = true)
data class TagsDataDto(
    @Json(name = "list") val list: List<TagItemDto>
)

@JsonClass(generateAdapter = true)
data class TagItemDto(
    @Json(name = "id") val id: Long = 0,
    @Json(name = "name") val name: String = "",
    @Json(name = "item_count") val itemCount: Int = 0,
    @Json(name = "additional") val additional: TagAdditionalItemDto? = null
)

@JsonClass(generateAdapter = true)
data class TagAdditionalItemDto(
    @Json(name = "thumbnail") val thumbnail: AlbumThumbnailItemDto? = null
)

/**
 * 照片上传响应
 */
@JsonClass(generateAdapter = true)
data class PhotoUploadResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "error") val error: PhotoError? = null,
    @Json(name = "data") val data: PhotoUploadDataDto? = null
)

@JsonClass(generateAdapter = true)
data class PhotoUploadDataDto(
    @Json(name = "id") val id: Long = 0,
    @Json(name = "filename") val filename: String = "",
    @Json(name = "filesize") val filesize: Long = 0,
    @Json(name = "folder_id") val folderId: Long = 0
)
