package wang.zengye.dsm.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wang.zengye.dsm.data.api.PhotoApiRetrofit
import wang.zengye.dsm.data.model.AlbumItem
import wang.zengye.dsm.data.model.PhotoItem
import wang.zengye.dsm.data.model.TimelineDto
import wang.zengye.dsm.data.model.GeocodingDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepository @Inject constructor(
    private val photoApiRetrofit: PhotoApiRetrofit
) : BaseRepository() {

    /**
     * 获取时间线
     */
    suspend fun getTimeline(
        startTime: Long? = null,
        endTime: Long? = null,
        groupUnit: String = "day"
    ): Result<TimelineDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = photoApiRetrofit.getTimelineDsm7(
                    groupUnit = groupUnit,
                    startTime = startTime,
                    endTime = endTime
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取照片列表
     */
    suspend fun getPhotos(
        offset: Int = 0,
        limit: Int = 100,
        startTime: Long? = null,
        endTime: Long? = null,
        type: String? = null
    ): Result<List<PhotoItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = photoApiRetrofit.getPhotosDsm7(
                    offset = offset,
                    limit = limit,
                    startTime = startTime,
                    endTime = endTime,
                    type = type
                )
                handleResponse(response).map { it.data.list }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取相册列表
     */
    suspend fun getAlbums(
        offset: Int = 0,
        limit: Int = 100,
        shared: Boolean = false
    ): Result<List<AlbumItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = photoApiRetrofit.getAlbumsDsm7(
                    offset = offset,
                    limit = limit,
                    shared = shared
                )
                handleResponse(response).map { it.data.list }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取相册内照片
     */
    suspend fun getAlbumPhotos(
        albumId: Long,
        offset: Int = 0,
        limit: Int = 100
    ): Result<List<PhotoItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = photoApiRetrofit.getAlbumPhotosDsm7(
                    albumId = albumId,
                    offset = offset,
                    limit = limit
                )
                handleResponse(response).map { it.data.list }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取地理编码列表
     */
    suspend fun getGeocoding(
        offset: Int = 0,
        limit: Int = 100
    ): Result<GeocodingDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = photoApiRetrofit.getGeocodingDsm7(
                    offset = offset,
                    limit = limit
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 创建相册
     */
    suspend fun createAlbum(name: String): Result<wang.zengye.dsm.data.api.AlbumCreateResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = photoApiRetrofit.createAlbumDsm7(name = name)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 删除相册
     */
    suspend fun deleteAlbum(albumId: Long): Result<wang.zengye.dsm.data.model.BasePhotoDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = photoApiRetrofit.deleteAlbumDsm7(albumId = albumId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 添加照片到相册
     */
    suspend fun addToAlbum(albumId: Long, photoIds: List<Long>): Result<wang.zengye.dsm.data.model.BasePhotoDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = photoApiRetrofit.addToAlbumDsm7(
                    albumId = albumId,
                    item = "[${photoIds.joinToString(",")}]"
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取文件夹列表
     */
    suspend fun getFolders(
        parentId: Long = 0,
        offset: Int = 0,
        limit: Int = 100
    ): Result<wang.zengye.dsm.data.api.FoldersResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = photoApiRetrofit.getFoldersDsm7(
                    parentId = parentId,
                    offset = offset,
                    limit = limit
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取标签列表
     */
    suspend fun getTags(
        offset: Int = 0,
        limit: Int = 100
    ): Result<wang.zengye.dsm.data.api.TagsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = photoApiRetrofit.getTagsDsm7(
                    offset = offset,
                    limit = limit
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取最近添加的照片
     */
    suspend fun getRecentlyAdded(
        offset: Int = 0,
        limit: Int = 100
    ): Result<wang.zengye.dsm.data.model.PhotosDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = photoApiRetrofit.getRecentlyAddedDsm7(
                    offset = offset,
                    limit = limit
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
