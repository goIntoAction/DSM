package wang.zengye.dsm.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wang.zengye.dsm.data.api.DockerApiRetrofit
import wang.zengye.dsm.data.model.docker.DockerContainerDetailDto
import wang.zengye.dsm.data.model.docker.DockerContainerListDto
import wang.zengye.dsm.data.model.docker.DockerContainerLogListDto
import wang.zengye.dsm.data.model.docker.DockerContainerLogDto
import wang.zengye.dsm.data.model.docker.DockerContainerOperationDto
import wang.zengye.dsm.data.model.docker.DockerDeleteImageDto
import wang.zengye.dsm.data.model.docker.DockerImageListDto
import wang.zengye.dsm.data.model.docker.DockerNetworkListDto
import wang.zengye.dsm.data.model.docker.DockerNetworkOperationDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DockerRepository @Inject constructor(
    private val dockerApiRetrofit: DockerApiRetrofit
) : BaseRepository() {

    // ==================== Moshi 方法 ====================

    /**
     * 获取镜像列表 - Moshi 版本
     */
    suspend fun getImageList(): Result<DockerImageListDto> {
        return withContext(Dispatchers.IO) {
            try {
                val api = dockerApiRetrofit ?: return@withContext Result.failure(Exception("DockerApiRetrofit not available"))
                val response = api.getImageList()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 删除镜像 - Moshi 版本
     */
    suspend fun deleteImage(imageId: String): Result<DockerDeleteImageDto> {
        return withContext(Dispatchers.IO) {
            try {
                val api = dockerApiRetrofit ?: return@withContext Result.failure(Exception("DockerApiRetrofit not available"))
                val response = api.deleteImage(name = "\"$imageId\"")
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取容器列表 - Moshi 版本
     */
    suspend fun getContainerList(): Result<DockerContainerListDto> {
        return withContext(Dispatchers.IO) {
            try {
                val api = dockerApiRetrofit ?: return@withContext Result.failure(Exception("DockerApiRetrofit not available"))
                val response = api.getContainerList()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取容器详情 - Moshi 版本
     */
    suspend fun getContainerDetail(name: String): Result<DockerContainerDetailDto> {
        return withContext(Dispatchers.IO) {
            try {
                val api = dockerApiRetrofit ?: return@withContext Result.failure(Exception("DockerApiRetrofit not available"))
                val response = api.getContainerDetail(name = "\"$name\"")
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 启动容器 - Moshi 版本
     */
    suspend fun startContainer(name: String): Result<DockerContainerOperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val api = dockerApiRetrofit ?: return@withContext Result.failure(Exception("DockerApiRetrofit not available"))
                val response = api.startContainer(name = "\"$name\"")
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 停止容器 - Moshi 版本
     */
    suspend fun stopContainer(name: String): Result<DockerContainerOperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val api = dockerApiRetrofit ?: return@withContext Result.failure(Exception("DockerApiRetrofit not available"))
                val response = api.stopContainer(name = "\"$name\"")
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 重启容器 - Moshi 版本
     */
    suspend fun restartContainer(name: String): Result<DockerContainerOperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val api = dockerApiRetrofit ?: return@withContext Result.failure(Exception("DockerApiRetrofit not available"))
                val response = api.restartContainer(name = "\"$name\"")
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取容器日志列表 - Moshi 版本
     */
    suspend fun getContainerLogList(name: String): Result<DockerContainerLogListDto> {
        return withContext(Dispatchers.IO) {
            try {
                val api = dockerApiRetrofit ?: return@withContext Result.failure(Exception("DockerApiRetrofit not available"))
                val response = api.getContainerLogList(name = "\"$name\"")
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取容器日志 - Moshi 版本
     */
    suspend fun getContainerLog(
        name: String,
        date: String,
        limit: Int = 1000,
        offset: Int = 0,
        sortDir: String = "ASC"
    ): Result<DockerContainerLogDto> {
        return withContext(Dispatchers.IO) {
            try {
                val api = dockerApiRetrofit ?: return@withContext Result.failure(Exception("DockerApiRetrofit not available"))
                val response = api.getContainerLog(
                    name = "\"$name\"",
                    date = "\"$date\"",
                    limit = limit,
                    offset = offset,
                    sortDir = "\"$sortDir\""
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取网络列表 - Moshi 版本
     */
    suspend fun getNetworks(): Result<DockerNetworkListDto> {
        return withContext(Dispatchers.IO) {
            try {
                val api = dockerApiRetrofit ?: return@withContext Result.failure(Exception("DockerApiRetrofit not available"))
                val response = api.getNetworks()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 创建网络 - Moshi 版本
     */
    suspend fun createNetwork(
        name: String,
        driver: String = "bridge",
        subnet: String = "",
        gateway: String = ""
    ): Result<DockerNetworkOperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val api = dockerApiRetrofit ?: return@withContext Result.failure(Exception("DockerApiRetrofit not available"))
                val response = api.createNetwork(
                    name = "\"$name\"",
                    driver = "\"$driver\"",
                    subnet = if (subnet.isNotEmpty()) "\"$subnet\"" else "",
                    gateway = if (gateway.isNotEmpty()) "\"$gateway\"" else ""
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 删除网络 - Moshi 版本
     */
    suspend fun deleteNetwork(networkId: String): Result<DockerNetworkOperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val api = dockerApiRetrofit ?: return@withContext Result.failure(Exception("DockerApiRetrofit not available"))
                val response = api.deleteNetwork(id = "\"$networkId\"")
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

}
