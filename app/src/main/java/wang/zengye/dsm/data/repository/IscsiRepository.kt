package wang.zengye.dsm.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import wang.zengye.dsm.data.api.IscsiApiRetrofit
import wang.zengye.dsm.data.model.iscsi.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * iSCSI 管理 Repository
 * 使用 Retrofit + Moshi 进行 API 调用
 */
@Singleton
class IscsiRepository @Inject constructor(
    private val iscsiApi: IscsiApiRetrofit
) : BaseRepository() {

    // ==================== LUN 操作 ====================

    /**
     * 获取 LUN 列表
     */
    suspend fun getLunList(): Result<LunListDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = iscsiApi.getLunList()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 创建 LUN
     */
    suspend fun createLun(
        name: String,
        location: String,
        size: Long,
        thinProvision: Boolean = true
    ): Result<LunCreateDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = iscsiApi.createLun(
                    name = name,
                    location = location,
                    size = size,
                    thinProvision = thinProvision
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 删除 LUN
     */
    suspend fun deleteLun(lunId: Int): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = iscsiApi.deleteLun(lunId = lunId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取存储池列表（用于选择 LUN 位置）
     */
    suspend fun getStoragePools(): Result<StoragePoolListDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = iscsiApi.getStoragePools()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== Target 操作 ====================

    /**
     * 获取 Target 列表
     */
    suspend fun getTargetList(): Result<TargetListDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = iscsiApi.getTargetList()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 创建 Target
     */
    suspend fun createTarget(
        name: String,
        iqn: String? = null,
        mappedLunIds: List<Int> = emptyList()
    ): Result<TargetCreateDto> {
        return withContext(Dispatchers.IO) {
            try {
                val mappedLun = mappedLunIds.mapIndexed { index, lunId ->
                    mapOf("lun_id" to lunId, "mapping_index" to index)
                }.toString()
                val response = iscsiApi.createTarget(
                    name = name,
                    iqn = iqn,
                    mappedLun = mappedLun
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 删除 Target
     */
    suspend fun deleteTarget(targetId: Int): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = iscsiApi.deleteTarget(targetId = targetId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 启用/禁用 Target
     */
    suspend fun setTargetEnabled(targetId: Int, enabled: Boolean): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = iscsiApi.enableTarget(targetId = targetId, enabled = enabled)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 映射 LUN 到 Target
     */
    suspend fun mapLunToTarget(targetId: Int, lunId: Int, mappingIndex: Int = 0): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = iscsiApi.mapLunToTarget(targetId = targetId, lunId = lunId, mappingIndex = mappingIndex)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 从 Target 取消映射 LUN
     */
    suspend fun unmapLunFromTarget(targetId: Int, lunId: Int): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = iscsiApi.unmapLunFromTarget(targetId = targetId, lunId = lunId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
