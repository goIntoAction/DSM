package wang.zengye.dsm.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import wang.zengye.dsm.data.api.VirtualMachineApiRetrofit
import wang.zengye.dsm.data.api.VmClusterResponse
import wang.zengye.dsm.data.api.VmDetailResponse
import wang.zengye.dsm.data.api.VmNetworkListResponse
import wang.zengye.dsm.data.api.VmStorageListResponse
import wang.zengye.dsm.data.model.virtual_machine.VmListDto
import wang.zengye.dsm.data.model.virtual_machine.VmOperationDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 虚拟机管理 Repository
 * 使用 Retrofit + Moshi 进行 API 调用
 */
@Singleton
class VirtualMachineRepository @Inject constructor(
    private val virtualMachineApi: VirtualMachineApiRetrofit
) : BaseRepository() {
    // ==================== 集群/列表 ====================

    suspend fun getCluster(): Result<VmClusterResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = virtualMachineApi.getCluster()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getVmList(): Result<VmListDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = virtualMachineApi.getVmList()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 电源控制 ====================

    suspend fun checkVmPowerOn(guestId: String): Result<VmOperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = virtualMachineApi.checkVmPowerOn(guestId = guestId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun startVm(guestId: String): Result<VmOperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = virtualMachineApi.startVm(guestId = guestId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun forceStopVm(guestId: String): Result<VmOperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = virtualMachineApi.forceStopVm(guestId = guestId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun shutdownVm(guestId: String): Result<VmOperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = virtualMachineApi.shutdownVm(guestId = guestId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun resetVm(guestId: String): Result<VmOperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = virtualMachineApi.resetVm(guestId = guestId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun suspendVm(guestId: String): Result<VmOperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = virtualMachineApi.suspendVm(guestId = guestId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun resumeVm(guestId: String): Result<VmOperationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = virtualMachineApi.resumeVm(guestId = guestId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 详情 ====================

    suspend fun getVmDetail(guestId: String): Result<VmDetailResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = virtualMachineApi.getVmDetail(guestId = guestId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 网络/存储 ====================

    suspend fun getVmNetworks(): Result<VmNetworkListResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = virtualMachineApi.getVmNetworks()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getVmStorages(): Result<VmStorageListResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = virtualMachineApi.getVmStorages()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}