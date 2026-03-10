package wang.zengye.dsm.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wang.zengye.dsm.data.api.PackageApiRetrofit
import wang.zengye.dsm.data.model.OperationDto
import wang.zengye.dsm.data.model.control_panel.InstalledPackagesDto
import wang.zengye.dsm.data.model.control_panel.ServerPackagesDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackageRepository @Inject constructor(
    private val packageApiRetrofit: PackageApiRetrofit
) : BaseRepository() {

    /**
     * 获取已安装套件
     */
    suspend fun getInstalledPackages(): Result<InstalledPackagesDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = packageApiRetrofit.getInstalledPackages()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取服务器套件
     */
    suspend fun getServerPackages(): Result<ServerPackagesDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = packageApiRetrofit.getServerPackages()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取社群套件
     */
    suspend fun getCommunityPackages(): Result<ServerPackagesDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = packageApiRetrofit.getCommunityPackages()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 启动套件
     */
    suspend fun startPackage(packageId: String): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = packageApiRetrofit.startPackage(id = packageId, dsmApps = packageId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 停止套件
     */
    suspend fun stopPackage(packageId: String): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = packageApiRetrofit.stopPackage(id = packageId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 安装套件
     */
    suspend fun installPackage(packageName: String, volumePath: String = "/volume1"): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = packageApiRetrofit.installPackage(name = packageName, volumePath = volumePath)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 卸载套件
     */
    suspend fun uninstallPackage(packageId: String, removeData: Boolean = false): Result<OperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = packageApiRetrofit.uninstallPackage(
                    id = packageId,
                    removeData = removeData.toString()
                )
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
