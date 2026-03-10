package wang.zengye.dsm.data.api

import wang.zengye.dsm.data.model.OperationDto
import wang.zengye.dsm.data.model.control_panel.InstalledPackagesDto
import wang.zengye.dsm.data.model.control_panel.ServerPackagesDto
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * 使用 Retrofit + Moshi 的套件中心 API 接口
 */
interface PackageApiRetrofit {

    /**
     * 获取已安装套件列表
     * API: SYNO.Core.Package
     * Method: list
     */
    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getInstalledPackages(
        @Field("api") api: String = "SYNO.Core.Package",
        @Field("version") version: String = "2",
        @Field("method") method: String = "list",
        @Field("additional") additional: String = "[\"description\",\"status\",\"url\",\"startable\"]",
        @Field("polling_interval") pollingInterval: String = "15"
    ): Response<InstalledPackagesDto>

    /**
     * 获取服务器可用套件列表（官方套件）
     * API: SYNO.Core.Package.Server
     * Method: list
     */
    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getServerPackages(
        @Field("api") api: String = "SYNO.Core.Package.Server",
        @Field("version") version: String = "2",
        @Field("method") method: String = "list",
        @Field("updateSprite") updateSprite: String = "true",
        @Field("blforcereload") blforcereload: String = "false",
        @Field("blloadothers") blloadothers: String = "false"
    ): Response<ServerPackagesDto>

    /**
     * 获取社群套件列表（第三方套件）
     * API: SYNO.Core.Package.Server
     * Method: list
     */
    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getCommunityPackages(
        @Field("api") api: String = "SYNO.Core.Package.Server",
        @Field("version") version: String = "2",
        @Field("method") method: String = "list",
        @Field("updateSprite") updateSprite: String = "true",
        @Field("blforcereload") blforcereload: String = "false",
        @Field("blloadothers") blloadothers: String = "true"
    ): Response<ServerPackagesDto>

    // ==================== 套件操作 ====================

    /**
     * 启动套件
     * API: SYNO.Core.Package.Control
     * Method: start
     */
    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun startPackage(
        @Field("api") api: String = "SYNO.Core.Package.Control",
        @Field("version") version: String = "1",
        @Field("method") method: String = "start",
        @Field("id") id: String,
        @Field("dsm_apps") dsmApps: String
    ): Response<OperationDto>

    /**
     * 停止套件
     * API: SYNO.Core.Package.Control
     * Method: stop
     */
    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun stopPackage(
        @Field("api") api: String = "SYNO.Core.Package.Control",
        @Field("version") version: String = "1",
        @Field("method") method: String = "stop",
        @Field("id") id: String
    ): Response<OperationDto>

    /**
     * 安装套件
     * API: SYNO.Core.Package.Installation
     * Method: install
     */
    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun installPackage(
        @Field("api") api: String = "SYNO.Core.Package.Installation",
        @Field("version") version: String = "1",
        @Field("method") method: String = "install",
        @Field("name") name: String,
        @Field("volume_path") volumePath: String = "/volume1"
    ): Response<OperationDto>

    /**
     * 卸载套件
     * API: SYNO.Core.Package.Uninstallation
     * Method: uninstall
     */
    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun uninstallPackage(
        @Field("api") api: String = "SYNO.Core.Package.Uninstallation",
        @Field("version") version: String = "1",
        @Field("method") method: String = "uninstall",
        @Field("id") id: String,
        @Field("remove_data") removeData: String = "false"
    ): Response<OperationDto>
}
