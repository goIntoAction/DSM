package wang.zengye.dsm.data.repository

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import wang.zengye.dsm.R
import wang.zengye.dsm.data.api.AuthApiRetrofit
import wang.zengye.dsm.data.api.DsmApiHelper
import wang.zengye.dsm.data.model.LoginDto
import wang.zengye.dsm.data.model.OtpQrCodeDto
import wang.zengye.dsm.data.model.OtpAuthDto
import wang.zengye.dsm.data.api.ApiInfoResponse
import wang.zengye.dsm.data.api.TrustedDevicesResponse
import wang.zengye.dsm.data.api.NormalUserResponse
import wang.zengye.dsm.util.appString
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 认证 Repository
 * 使用 Retrofit + Moshi 进行 API 调用
 */
@Singleton
class AuthRepository @Inject constructor(
    private val authApiRetrofit: AuthApiRetrofit
) : BaseRepository() {

    companion object {
        /** QuickConnect 专用的纯 OkHttpClient（无 DSM 拦截器） */
        private val quickConnectClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .build()
        }
    }

    // ==================== 登录/登出 ====================

    /**
     * 登录
     */
    suspend fun login(
        account: String,
        password: String,
        otpCode: String? = null
    ): Result<LoginDto> {
        return withContext(Dispatchers.IO) {
            // 登录前清除旧会话
            DsmApiHelper.clearSession()
            
            try {
                val response = authApiRetrofit.login(
                    account = account,
                    password = password,
                    otpCode = otpCode
                )
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        // 从响应头提取完整的 cookie
                        val cookieMap = mutableMapOf<String, String>()
                        response.headers().values("set-cookie").forEach { setCookieHeader ->
                            val cookiePart = setCookieHeader.split(";", limit = 2).firstOrNull()
                            if (cookiePart != null) {
                                val parts = cookiePart.split("=", limit = 2)
                                if (parts.size == 2) {
                                    cookieMap[parts[0].trim()] = parts[1].trim()
                                }
                            }
                        }
                        val completeCookie = cookieMap.map { "${it.key}=${it.value}" }.joinToString("; ")

                        // 更新会话信息
                        body.data?.sid?.let { sid ->
                            DsmApiHelper.updateSession(sid, completeCookie, DsmApiHelper.baseUrl)
                        }
                        body.data?.synoToken?.let { DsmApiHelper.updateSynoToken(it) }
                        Result.success(body)
                    } else {
                        val errorMsg = body?.error?.errors?.firstOrNull()
                            ?: appString(R.string.api_error_unknown)
                        Result.failure(Exception(errorMsg))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 登出
     */
    suspend fun logout(): Result<OtpAuthDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiRetrofit.logout()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== API 信息 ====================

    suspend fun queryApiInfo(): Result<ApiInfoResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiRetrofit.queryApiInfo()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== QuickConnect ====================

    suspend fun quickConnectPing(serverId: String): Result<JSONObject> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://global.quickconnect.to/Serv.php"

                val formBody = FormBody.Builder()
                    .add("version", "1")
                    .add("method", "ping")
                    .add("id", serverId)
                    .build()

                val request = Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build()

                val response = quickConnectClient.newCall(request).execute()
                val body = response.body?.string()
                
                if (response.isSuccessful && !body.isNullOrEmpty()) {
                    Result.success(JSONObject(body))
                } else {
                    Result.failure(Exception(appString(R.string.api_error_quickconnect_failed)))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getQuickConnectServer(serverId: String): Result<JSONObject> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://global.quickconnect.to/Serv.php"

                val formBody = FormBody.Builder()
                    .add("version", "1")
                    .add("method", "get_server_info")
                    .add("id", serverId)
                    .build()

                val request = Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build()

                val response = quickConnectClient.newCall(request).execute()
                val body = response.body?.string()

                if (response.isSuccessful && !body.isNullOrEmpty()) {
                    Result.success(JSONObject(body))
                } else {
                    Result.failure(Exception(appString(R.string.api_error_quickconnect_failed)))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== OTP/双因素认证 ====================

    suspend fun saveOtpMail(mail: String): Result<OtpAuthDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiRetrofit.saveOtpMail(mail = mail)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getOtpQrCode(account: String): Result<OtpQrCodeDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiRetrofit.getOtpQrCode(account = account)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun authOtpCode(code: String): Result<OtpAuthDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiRetrofit.authOtpCode(code = code)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 信任设备管理 ====================

    suspend fun getTrustedDevices(): Result<TrustedDevicesResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiRetrofit.getTrustedDevices()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteTrustedDevice(deviceId: String): Result<OtpAuthDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiRetrofit.deleteTrustedDevice(deviceId = deviceId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== 普通用户设置 ====================

    suspend fun getNormalUser(): Result<NormalUserResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiRetrofit.getNormalUser()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
