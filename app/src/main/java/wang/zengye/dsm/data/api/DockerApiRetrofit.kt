package wang.zengye.dsm.data.api

import wang.zengye.dsm.data.model.docker.DockerContainerDetailDto
import wang.zengye.dsm.data.model.docker.DockerContainerListDto
import wang.zengye.dsm.data.model.docker.DockerContainerLogListDto
import wang.zengye.dsm.data.model.docker.DockerContainerLogDto
import wang.zengye.dsm.data.model.docker.DockerContainerOperationDto
import wang.zengye.dsm.data.model.docker.DockerDeleteImageDto
import wang.zengye.dsm.data.model.docker.DockerImageListDto
import wang.zengye.dsm.data.model.docker.DockerNetworkListDto
import wang.zengye.dsm.data.model.docker.DockerNetworkOperationDto
import wang.zengye.dsm.data.model.docker.DockerRegistryListDto
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * 使用 Retrofit + Moshi 的 Docker API 接口
 */
interface DockerApiRetrofit {

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getImageList(
        @Field("api") api: String = "SYNO.Docker.Image",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list",
        @Field("limit") limit: String = "-1",
        @Field("offset") offset: Int = 0,
        @Field("show_dsm") showDsm: Boolean = false
    ): Response<DockerImageListDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun deleteImage(
        @Field("api") api: String = "SYNO.Docker.Image",
        @Field("version") version: String = "1",
        @Field("method") method: String = "delete",
        @Field("name") name: String
    ): Response<DockerDeleteImageDto>

    // ============== 容器相关 API ==============

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getContainerList(
        @Field("api") api: String = "SYNO.Docker.Container",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list",
        @Field("limit") limit: String = "-1",
        @Field("offset") offset: Int = 0,
        @Field("type") type: String = "all"
    ): Response<DockerContainerListDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getContainerDetail(
        @Field("api") api: String = "SYNO.Docker.Container",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get",
        @Field("name") name: String
    ): Response<DockerContainerDetailDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun startContainer(
        @Field("api") api: String = "SYNO.Docker.Container",
        @Field("version") version: String = "1",
        @Field("method") method: String = "start",
        @Field("name") name: String
    ): Response<DockerContainerOperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun stopContainer(
        @Field("api") api: String = "SYNO.Docker.Container",
        @Field("version") version: String = "1",
        @Field("method") method: String = "stop",
        @Field("name") name: String
    ): Response<DockerContainerOperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun restartContainer(
        @Field("api") api: String = "SYNO.Docker.Container",
        @Field("version") version: String = "1",
        @Field("method") method: String = "restart",
        @Field("name") name: String
    ): Response<DockerContainerOperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun killContainer(
        @Field("api") api: String = "SYNO.Docker.Container",
        @Field("version") version: String = "1",
        @Field("method") method: String = "signal",
        @Field("name") name: String,
        @Field("signal") signal: Int = 9
    ): Response<DockerContainerOperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun deleteContainer(
        @Field("api") api: String = "SYNO.Docker.Container",
        @Field("version") version: String = "1",
        @Field("method") method: String = "delete",
        @Field("name") name: String
    ): Response<DockerContainerOperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getContainerStatus(
        @Field("api") api: String = "SYNO.Docker.Container",
        @Field("version") version: String = "1",
        @Field("method") method: String = "status",
        @Field("name") name: String
    ): Response<DockerContainerOperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getContainerLogList(
        @Field("api") api: String = "SYNO.Docker.Container.Log",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list",
        @Field("name") name: String
    ): Response<DockerContainerLogListDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getContainerLog(
        @Field("api") api: String = "SYNO.Docker.Container.Log",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get",
        @Field("name") name: String,
        @Field("date") date: String,
        @Field("limit") limit: Int = 1000,
        @Field("offset") offset: Int = 0,
        @Field("sort_dir") sortDir: String = "ASC"
    ): Response<DockerContainerLogDto>

    // ============== 网络相关 API ==============

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getNetworks(
        @Field("api") api: String = "SYNO.Docker.Network",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list"
    ): Response<DockerNetworkListDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun createNetwork(
        @Field("api") api: String = "SYNO.Docker.Network",
        @Field("version") version: String = "1",
        @Field("method") method: String = "create",
        @Field("name") name: String,
        @Field("driver") driver: String = "bridge",
        @Field("subnet") subnet: String = "",
        @Field("gateway") gateway: String = ""
    ): Response<DockerNetworkOperationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun deleteNetwork(
        @Field("api") api: String = "SYNO.Docker.Network",
        @Field("version") version: String = "1",
        @Field("method") method: String = "delete",
        @Field("id") id: String
    ): Response<DockerNetworkOperationDto>

    // ============== 注册表相关 API ==============

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getRegistryList(
        @Field("api") api: String = "SYNO.Docker.Registry",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get",
        @Field("limit") limit: String = "-1",
        @Field("offset") offset: Int = 0
    ): Response<DockerRegistryListDto>
}
