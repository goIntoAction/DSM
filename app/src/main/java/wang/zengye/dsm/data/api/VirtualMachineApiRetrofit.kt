package wang.zengye.dsm.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import wang.zengye.dsm.data.model.virtual_machine.*

/**
 * 使用 Retrofit + Moshi 的虚拟机管理 API 接口
 */
interface VirtualMachineApiRetrofit {

    // ==================== 集群/列表 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getCluster(
        @Field("api") api: String = "SYNO.Virtualization.Cluster",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get"
    ): Response<VmClusterResponse>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getVmList(
        @Field("api") api: String = "SYNO.Virtualization.Cluster",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list"
    ): Response<VmListDto>

    // ==================== 电源控制 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun checkVmPowerOn(
        @Field("api") api: String = "SYNO.Virtualization.Guest.Action",
        @Field("version") version: String = "1",
        @Field("method") method: String = "check_poweron",
        @Field("guest_id") guestId: String
    ): Response<VmOperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun vmPowerControl(
        @Field("api") api: String = "SYNO.Virtualization.Guest.Action",
        @Field("version") version: String = "1",
        @Field("method") method: String = "pwr_ctl",
        @Field("guest_id") guestId: String,
        @Field("action") action: String
    ): Response<VmOperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun startVm(
        @Field("api") api: String = "SYNO.Virtualization.Guest.Action",
        @Field("version") version: String = "1",
        @Field("method") method: String = "pwr_ctl",
        @Field("guest_id") guestId: String,
        @Field("action") action: String = "on"
    ): Response<VmOperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun forceStopVm(
        @Field("api") api: String = "SYNO.Virtualization.Guest.Action",
        @Field("version") version: String = "1",
        @Field("method") method: String = "pwr_ctl",
        @Field("guest_id") guestId: String,
        @Field("action") action: String = "off"
    ): Response<VmOperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun shutdownVm(
        @Field("api") api: String = "SYNO.Virtualization.Guest.Action",
        @Field("version") version: String = "1",
        @Field("method") method: String = "pwr_ctl",
        @Field("guest_id") guestId: String,
        @Field("action") action: String = "shutdown"
    ): Response<VmOperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun resetVm(
        @Field("api") api: String = "SYNO.Virtualization.Guest.Action",
        @Field("version") version: String = "1",
        @Field("method") method: String = "pwr_ctl",
        @Field("guest_id") guestId: String,
        @Field("action") action: String = "reset"
    ): Response<VmOperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun suspendVm(
        @Field("api") api: String = "SYNO.Virtualization.Guest.Action",
        @Field("version") version: String = "1",
        @Field("method") method: String = "pwr_ctl",
        @Field("guest_id") guestId: String,
        @Field("action") action: String = "suspend"
    ): Response<VmOperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun resumeVm(
        @Field("api") api: String = "SYNO.Virtualization.Guest.Action",
        @Field("version") version: String = "1",
        @Field("method") method: String = "pwr_ctl",
        @Field("guest_id") guestId: String,
        @Field("action") action: String = "resume"
    ): Response<VmOperationDto>

    // ==================== 详情 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getVmDetail(
        @Field("api") api: String = "SYNO.Virtualization.Guest",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get",
        @Field("guest_id") guestId: String
    ): Response<VmDetailResponse>

    // ==================== 网络/存储 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getVmNetworks(
        @Field("api") api: String = "SYNO.Virtualization.Network",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list"
    ): Response<VmNetworkListResponse>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getVmStorages(
        @Field("api") api: String = "SYNO.Virtualization.Storage",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list"
    ): Response<VmStorageListResponse>
}

// ==================== 额外的响应模型 ====================

@JsonClass(generateAdapter = true)
data class VmClusterResponse(
    @Json(name = "data") val data: VmClusterDataDto?
)

@JsonClass(generateAdapter = true)
data class VmClusterDataDto(
    @Json(name = "cluster_id") val clusterId: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "status") val status: String?
)

@JsonClass(generateAdapter = true)
data class VmDetailResponse(
    @Json(name = "data") val data: VmDetailDataDto?
)

@JsonClass(generateAdapter = true)
data class VmDetailDataDto(
    @Json(name = "guest_id") val guestId: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "status") val status: String?,
    @Json(name = "vcpu_num") val vcpuNum: Int?,
    @Json(name = "memory") val memory: Long?,
    @Json(name = "disks") val disks: List<VmDisk>?,
    @Json(name = "networks") val networks: List<VmNetwork>?,
    @Json(name = "autostart") val autostart: Boolean?,
    @Json(name = "os_type") val osType: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "cpu_usage") val cpuUsage: Double?,
    @Json(name = "memory_usage") val memoryUsage: Long?
)

@JsonClass(generateAdapter = true)
data class VmNetworkListResponse(
    @Json(name = "data") val data: VmNetworkListDataDto?
)

@JsonClass(generateAdapter = true)
data class VmNetworkListDataDto(
    @Json(name = "networks") val networks: List<VmNetworkInfoDto>?
)

@JsonClass(generateAdapter = true)
data class VmNetworkInfoDto(
    @Json(name = "name") val name: String?,
    @Json(name = "type") val type: String?,
    @Json(name = "bridge") val bridge: String?
)

@JsonClass(generateAdapter = true)
data class VmStorageListResponse(
    @Json(name = "data") val data: VmStorageListDataDto?
)

@JsonClass(generateAdapter = true)
data class VmStorageListDataDto(
    @Json(name = "storages") val storages: List<VmStorageInfoDto>?
)

@JsonClass(generateAdapter = true)
data class VmStorageInfoDto(
    @Json(name = "name") val name: String?,
    @Json(name = "type") val type: String?,
    @Json(name = "size") val size: Long?,
    @Json(name = "used") val used: Long?
)
