package wang.zengye.dsm.data.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query
import wang.zengye.dsm.data.model.LoginDto
import wang.zengye.dsm.data.model.OtpQrCodeDto
import wang.zengye.dsm.data.model.OtpAuthDto
import wang.zengye.dsm.data.model.ApiErrorDto

/**
 * 使用 Retrofit + Moshi 的认证 API 接口
 */
interface AuthApiRetrofit {

    // ==================== 登录/登出 ====================

    @FormUrlEncoded
    @POST("auth.cgi")
    suspend fun login(
        @Field("api") api: String = "SYNO.API.Auth",
        @Field("version") version: String = "4",
        @Field("method") method: String = "login",
        @Field("account") account: String,
        @Field("passwd") password: String,
        @Field("session") session: String = "webui",
        @Field("enable_device_token") enableDeviceToken: String = "no",
        @Field("enable_sync_token") enableSyncToken: String = "yes",
        @Field("isIframeLogin") isIframeLogin: String = "yes",
        @Field("otp_code") otpCode: String? = null
    ): Response<LoginDto>

    @GET("auth.cgi")
    suspend fun logout(
        @Query("api") api: String = "SYNO.API.Auth",
        @Query("version") version: String = "3",
        @Query("method") method: String = "logout",
        @Query("session") session: String = "webui"
    ): Response<OtpAuthDto>

    // ==================== API 信息 ====================

    @GET("query.cgi")
    suspend fun queryApiInfo(
        @Query("api") api: String = "SYNO.API.Info",
        @Query("version") version: String = "1",
        @Query("method") method: String = "query",
        @Query("query") query: String = "all"
    ): Response<ApiInfoResponse>

    // ==================== OTP/双因素认证 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun saveOtpMail(
        @Field("api") api: String = "SYNO.Core.OTP",
        @Field("version") version: String = "2",
        @Field("method") method: String = "save_mail",
        @Field("mail") mail: String
    ): Response<OtpAuthDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getOtpQrCode(
        @Field("api") api: String = "SYNO.Core.OTP",
        @Field("version") version: String = "2",
        @Field("method") method: String = "get_qrcode",
        @Field("account") account: String
    ): Response<OtpQrCodeDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun authOtpCode(
        @Field("api") api: String = "SYNO.Core.OTP",
        @Field("version") version: String = "2",
        @Field("method") method: String = "auth_tmp_code",
        @Field("code") code: String
    ): Response<OtpAuthDto>

    // ==================== 信任设备管理 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getTrustedDevices(
        @Field("api") api: String = "SYNO.Core.TrustDevice",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list"
    ): Response<TrustedDevicesResponse>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun deleteTrustedDevice(
        @Field("api") api: String = "SYNO.Core.TrustDevice",
        @Field("version") version: String = "1",
        @Field("method") method: String = "delete",
        @Field("id") deviceId: String
    ): Response<OtpAuthDto>

    // ==================== 普通用户设置 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getNormalUser(
        @Field("api") api: String = "SYNO.Core.NormalUser",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get"
    ): Response<NormalUserResponse>
}

// ==================== 响应模型 ====================

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class ApiInfoResponse(
    @com.squareup.moshi.Json(name = "success") val success: Boolean = false,
    @com.squareup.moshi.Json(name = "error") val error: ApiErrorDto? = null,
    @com.squareup.moshi.Json(name = "data") val data: Map<String, ApiInfoDataDto>? = null
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class ApiInfoDataDto(
    @com.squareup.moshi.Json(name = "maxVersion") val maxVersion: Int = 0,
    @com.squareup.moshi.Json(name = "minVersion") val minVersion: Int = 0,
    @com.squareup.moshi.Json(name = "path") val path: String = ""
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class TrustedDevicesResponse(
    @com.squareup.moshi.Json(name = "success") val success: Boolean = false,
    @com.squareup.moshi.Json(name = "error") val error: ApiErrorDto? = null,
    @com.squareup.moshi.Json(name = "data") val data: TrustedDevicesDataDto? = null
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class TrustedDevicesDataDto(
    @com.squareup.moshi.Json(name = "devices") val devices: List<TrustedDevice>? = null
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class TrustedDevice(
    @com.squareup.moshi.Json(name = "id") val id: String = "",
    @com.squareup.moshi.Json(name = "name") val name: String = "",
    @com.squareup.moshi.Json(name = "type") val type: String = "",
    @com.squareup.moshi.Json(name = "last_used") val lastUsed: Long = 0
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class NormalUserResponse(
    @com.squareup.moshi.Json(name = "success") val success: Boolean = false,
    @com.squareup.moshi.Json(name = "error") val error: ApiErrorDto? = null,
    @com.squareup.moshi.Json(name = "data") val data: NormalUserDataDto? = null
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class NormalUserDataDto(
    @com.squareup.moshi.Json(name = "name") val name: String = "",
    @com.squareup.moshi.Json(name = "display_name") val displayName: String = "",
    @com.squareup.moshi.Json(name = "email") val email: String = "",
    @com.squareup.moshi.Json(name = "description") val description: String = ""
)
