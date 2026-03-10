package wang.zengye.dsm.data.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import wang.zengye.dsm.data.model.iscsi.*

/**
 * 使用 Retrofit + Moshi 的 iSCSI API 接口
 */
interface IscsiApiRetrofit {

    // ==================== LUN 管理 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getLunList(
        @Field("api") api: String = "SYNO.Core.ISCSI.LUN",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list",
        @Field("additional") additional: String = "[\"status\",\"is_mapped\"]"
    ): Response<LunListDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun createLun(
        @Field("api") api: String = "SYNO.Core.ISCSI.LUN",
        @Field("version") version: String = "1",
        @Field("method") method: String = "create",
        @Field("name") name: String,
        @Field("location") location: String,
        @Field("size") size: Long,
        @Field("type") type: String = "FILE",
        @Field("thin_provision") thinProvision: Boolean = true
    ): Response<LunCreateDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun deleteLun(
        @Field("api") api: String = "SYNO.Core.ISCSI.LUN",
        @Field("version") version: String = "1",
        @Field("method") method: String = "delete",
        @Field("lun_id") lunId: Int
    ): Response<OperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getStoragePools(
        @Field("api") api: String = "SYNO.Storage.Pool",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list"
    ): Response<StoragePoolListDto>

    // ==================== Target 管理 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getTargetList(
        @Field("api") api: String = "SYNO.Core.ISCSI.Target",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list",
        @Field("additional") additional: String = "[\"connected_session\",\"mapped_lun\"]"
    ): Response<TargetListDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun createTarget(
        @Field("api") api: String = "SYNO.Core.ISCSI.Target",
        @Field("version") version: String = "1",
        @Field("method") method: String = "create",
        @Field("name") name: String,
        @Field("iqn") iqn: String? = null,
        @Field("mapped_lun") mappedLun: String = "[]"
    ): Response<TargetCreateDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun deleteTarget(
        @Field("api") api: String = "SYNO.Core.ISCSI.Target",
        @Field("version") version: String = "1",
        @Field("method") method: String = "delete",
        @Field("target_id") targetId: Int
    ): Response<OperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun enableTarget(
        @Field("api") api: String = "SYNO.Core.ISCSI.Target",
        @Field("version") version: String = "1",
        @Field("method") method: String = "set",
        @Field("target_id") targetId: Int,
        @Field("enabled") enabled: Boolean = true
    ): Response<OperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun mapLunToTarget(
        @Field("api") api: String = "SYNO.Core.ISCSI.Target",
        @Field("version") version: String = "1",
        @Field("method") method: String = "map_lun",
        @Field("target_id") targetId: Int,
        @Field("lun_id") lunId: Int,
        @Field("mapping_index") mappingIndex: Int = 0
    ): Response<OperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun unmapLunFromTarget(
        @Field("api") api: String = "SYNO.Core.ISCSI.Target",
        @Field("version") version: String = "1",
        @Field("method") method: String = "unmap_lun",
        @Field("target_id") targetId: Int,
        @Field("lun_id") lunId: Int
    ): Response<OperationDto>
}