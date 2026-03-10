package wang.zengye.dsm.data.api

import android.util.Log
import org.json.JSONObject
import org.json.JSONArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import wang.zengye.dsm.BuildConfig
import wang.zengye.dsm.R
import wang.zengye.dsm.util.SettingsManager
import wang.zengye.dsm.util.appString
import java.io.IOException
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.URLEncoder
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * DSM 只允许写静态方法，禁止写网络api
 */
object DsmApiHelper {

    private const val TAG = "DsmApiClient"
    private const val TIMEOUT = 30L
    private const val READ_TIMEOUT = 60L
    private const val WRITE_TIMEOUT = 60L
    private const val FILE_TRANSFER_TIMEOUT = 300L  // 文件传输超时：5分钟
    private const val MAX_RETRY_COUNT = 3

    // 基础客户端（共享连接池和 Dispatcher）
    private var baseHttpClient: OkHttpClient? = null

    // 派生客户端
    lateinit var okHttpClient: OkHttpClient           // 普通 API（60s 超时，带重试）
        private set
    lateinit var fileTransferClient: OkHttpClient     // 文件上传/下载（300s 超时，无重试）
        private set
    lateinit var imageClient: OkHttpClient            // 图片加载（60s 超时，无重试）
        private set

    // 兼容旧代码的别名
    val uploadHttpClient: OkHttpClient
        get() = fileTransferClient

    // 会话信息
    var sid: String = ""
        private set
    
    var cookie: String = ""
        private set

    var synoToken: String = ""
        private set
    
    var baseUrl: String = ""
        private set
    
    var dsmVersion: Int = 7  // DSM 版本 (6 或 7)
        private set
    
    var hostname: String = ""
        private set

    private var initialized = false

    // 会话过期自动重登录
    private val reLoginMutex = Mutex()
    private val _sessionExpiredEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    /** UI 层监听此事件，收到后跳转登录页 */
    val sessionExpiredEvent: SharedFlow<Unit> = _sessionExpiredEvent.asSharedFlow()

    private val SESSION_EXPIRED_CODES = setOf(106, 107, 109, 409)

    /**
     * 判断 API 错误码是否为会话过期
     */
    private fun isSessionExpired(code: Int): Boolean = code in SESSION_EXPIRED_CODES


    
    fun getSessionId(): String {
        return resolveSessionId(sid, cookie)
    }

    fun resolveSessionId(explicitSid: String, cookieValue: String = cookie): String {
        if (explicitSid.isNotEmpty()) {
            return explicitSid
        }
        return extractCookieValue("id", cookieValue) ?: ""
    }

    fun getCookieValue(name: String): String? {
        return extractCookieValue(name, cookie)
    }

    /**
     * 同步初始化 OkHttpClient（在 Hilt 初始化前调用）
     * 不依赖 SettingsManager Flow，使用默认配置
     */
    fun initOkHttpClient() {
        if (::okHttpClient.isInitialized) return
        recreateHttpClients(checkSsl = false)
    }

    /**
     * 异步加载会话状态并更新 OkHttpClient
     */
    suspend fun loadSessionAndInit() {
        if (initialized) return

        val checkSsl = SettingsManager.checkSsl.first()
        loadSession()

        // 仅当 SSL 设置与默认值不同时才重建 HttpClient
        if (checkSsl) {
            recreateHttpClients(checkSsl = true)
        }
        initialized = true
    }

    suspend fun init() {
        if (initialized) return

        val checkSsl = SettingsManager.checkSsl.first()
        loadSession()

        // 仅当 SSL 设置与默认值不同时才重建 HttpClient
        if (checkSsl) {
            recreateHttpClients(checkSsl = true)
        }
        initialized = true
    }

    /**
     * 重新创建所有 OkHttpClient（在 SSL 设置变化后调用）
     * 使用 newBuilder() 派生客户端，共享连接池和 Dispatcher
     */
    fun recreateHttpClients(checkSsl: Boolean) {
        // 1. 创建基础客户端（最小配置，不含日志拦截器）
        baseHttpClient = createBaseHttpClient(checkSsl)

        // 2. 派生各用途客户端，覆盖超时配置并添加特定拦截器
        okHttpClient = baseHttpClient!!.newBuilder()
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(RetryInterceptor(MAX_RETRY_COUNT))
            .addInterceptor(createLoggingInterceptor(SafeLoggingInterceptor.Level.BODY))
            .build()

        fileTransferClient = baseHttpClient!!.newBuilder()
            .readTimeout(FILE_TRANSFER_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(FILE_TRANSFER_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(createLoggingInterceptor(SafeLoggingInterceptor.Level.HEADERS))
            .build()

        imageClient = baseHttpClient!!.newBuilder()
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(createLoggingInterceptor(SafeLoggingInterceptor.Level.HEADERS))
            .build()
    }

    private fun createLoggingInterceptor(level: SafeLoggingInterceptor.Level): SafeLoggingInterceptor {
        return SafeLoggingInterceptor().apply {
            this.level = if (BuildConfig.DEBUG) level else SafeLoggingInterceptor.Level.NONE
        }
    }

    /**
     * 创建基础 OkHttpClient（共享连接池和 Dispatcher）
     * 包含通用配置：SSL、Cookie、拦截器
     * 不含日志拦截器，由派生客户端自行配置
     */
    private fun createBaseHttpClient(checkSsl: Boolean): OkHttpClient {
        val cookieManager = CookieManager()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)

        val builder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            // 允许明文 HTTP 通信（DSM 可能使用 HTTP）
            .connectionSpecs(listOf(ConnectionSpec.CLEARTEXT, ConnectionSpec.MODERN_TLS))
            // 添加会话拦截器（Cookie、Accept、Origin 等）
            .addInterceptor(SessionInterceptor())
            // 添加动态 baseUrl 拦截器（用于 Retrofit 请求）
            .addInterceptor(DynamicBaseUrlInterceptor())

        // SSL 证书处理 - 仅在用户明确禁用时才跳过验证
        if (!checkSsl) {
            configureUnsafeSsl(builder)
        }

        return builder.build()
    }

    /**
     * 配置跳过 SSL 验证
     */
    private fun configureUnsafeSsl(builder: OkHttpClient.Builder) {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, SecureRandom())

            builder.sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }
            Log.w(TAG, "SSL certificate verification is disabled - use with caution!")
        } catch (e: Exception) {
            Log.e(TAG, "SSL setup failed", e)
        }
    }

    /**
     * 重新创建 OkHttpClient（在 SSL 设置变化后调用）
     * @deprecated 使用 recreateHttpClients 替代
     */
    @Deprecated("Use recreateHttpClients instead", ReplaceWith("recreateHttpClients(checkSsl)"))
    fun recreateHttpClient(checkSsl: Boolean) {
        recreateHttpClients(checkSsl)
    }
    
    private suspend fun loadSession() {
        sid = SettingsManager.sid.value
        cookie = SettingsManager.cookie.value
        synoToken = SettingsManager.synoToken.value
        baseUrl = SettingsManager.host.first()
    }

    fun updateSession(newSid: String, newCookie: String, newBaseUrl: String) {
        sid = newSid
        cookie = newCookie
        baseUrl = newBaseUrl
    }

    fun isInitialized(): Boolean = initialized

    fun updateSynoToken(newToken: String) {
        synoToken = newToken
    }
    
    fun updateDsmVersion(version: Int) {
        dsmVersion = version
    }
    
    fun updateHostname(name: String) {
        hostname = name
    }

    /**
     * 网络重试拦截器
     * 在网络错误时自动重试
     */
    private class RetryInterceptor(
        private val maxRetryCount: Int
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var request = chain.request()
            var response: Response? = null
            var exception: IOException? = null
            var retryCount = 0
            
            while (retryCount <= maxRetryCount) {
                try {
                    response = chain.proceed(request)
                    if (response.isSuccessful || retryCount >= maxRetryCount) {
                        return response
                    }
                    // 服务器错误 (5xx) 时重试
                    if (response.code in 500..599) {
                        response.close()
                        retryCount++
                        if (retryCount <= maxRetryCount) {
                            Thread.sleep(1000L * retryCount) // 指数退避
                            continue
                        }
                    }
                    return response
                } catch (e: IOException) {
                    exception = e
                    retryCount++
                    if (retryCount <= maxRetryCount) {
                        Log.w(TAG, "Request failed, retrying ($retryCount/$maxRetryCount): ${e.message}")
                        Thread.sleep(1000L * retryCount)
                    }
                }
            }
            
            // 返回最后的响应或抛出异常
            response?.let { return it }
            throw exception ?: IOException("Unknown error after retries")
        }
    }

    /**
     * 从 Cookie 字符串中提取指定名称的值
     */
    private fun extractCookieValue(name: String, cookieValue: String): String? {
        if (cookieValue.isBlank()) return null
        return cookieValue.split(";")
            .map { it.trim() }
            .firstOrNull { it.startsWith("$name=") }
            ?.substringAfter("=")
    }

    /**
     * 获取 API 错误消息
     */
    fun getApiErrorMessage(code: Int): String {
        return when (code) {
            // 通用错误
            100 -> appString(R.string.api_error_unknown)
            101 -> appString(R.string.api_error_account_not_found)
            102 -> appString(R.string.api_error_account_disabled)
            103 -> appString(R.string.api_error_wrong_password)
            104 -> appString(R.string.api_error_permission_denied)
            105 -> appString(R.string.api_error_service_unavailable)
            106 -> appString(R.string.api_error_session_timeout)
            107 -> appString(R.string.api_error_session_interrupted)
            109 -> appString(R.string.api_error_session_not_found)

            // 登录错误
            400 -> appString(R.string.api_error_login_wrong_credentials)
            401 -> appString(R.string.api_error_login_account_disabled)
            402 -> appString(R.string.api_error_login_permission_denied)
            403 -> appString(R.string.api_error_login_otp_required)
            404 -> appString(R.string.api_error_login_account_not_found)
            405 -> appString(R.string.api_error_login_otp_wrong)
            406 -> appString(R.string.api_error_login_2fa_required)
            407 -> appString(R.string.api_error_login_otp_wrong_2)
            408 -> appString(R.string.api_error_login_device_unauthorized)
            409 -> appString(R.string.api_error_login_session_expired)
            410 -> appString(R.string.api_error_login_account_locked)
            411 -> appString(R.string.api_error_login_account_expired)
            412 -> appString(R.string.api_error_login_password_expired)
            413 -> appString(R.string.api_error_login_account_inactive)
            414 -> appString(R.string.api_error_login_otp_not_enabled)
            415 -> appString(R.string.api_error_login_device_token_wrong)
            416 -> appString(R.string.api_error_login_device_auth_required)
            417 -> appString(R.string.api_error_login_device_auth_failed)

            // 文件操作错误
            1000 -> appString(R.string.api_error_file_not_found)
            1001 -> appString(R.string.api_error_folder_not_found)
            1002 -> appString(R.string.api_error_file_exists)
            1003 -> appString(R.string.api_error_folder_exists)
            1004 -> appString(R.string.api_error_invalid_filename)
            1005 -> appString(R.string.api_error_invalid_path)
            1006 -> appString(R.string.api_error_insufficient_permission)
            1007 -> appString(R.string.api_error_insufficient_storage)
            1008 -> appString(R.string.api_error_file_in_use)
            1009 -> appString(R.string.api_error_filesystem_error)

            // 下载错误
            4000 -> appString(R.string.api_error_task_not_found)
            4001 -> appString(R.string.api_error_task_create_failed)
            4002 -> appString(R.string.api_error_invalid_download_link)
            4003 -> appString(R.string.api_error_download_task_failed)

            else -> appString(R.string.api_error_unknown_code, code)
        }
    }
    
    /**
     * 清除会话
     */
    suspend fun clearSession() {
        sid = ""
        cookie = ""
        synoToken = ""
        // 保留 baseUrl，因为在重新登录时需要使用它
    }

    /**
     * 获取文件下载 URL
     */
    fun getDownloadUrl(path: String): String {
        val sessionId = getSessionId()
        val base = baseUrl
        val baseUrlStr = if (base.endsWith("/")) base else "$base/"
        return "${baseUrlStr}webapi/entry.cgi?api=SYNO.FileStation.Download&version=2&method=download&path=${java.net.URLEncoder.encode(path, "UTF-8")}&mode=open&_sid=$sessionId"
    }

    /**
     * 获取文件缩略图 URL
     */
    fun getThumbnailUrl(path: String, size: String = "large"): String {
        val sessionId = getSessionId()
        val base = baseUrl
        val baseUrlStr = if (base.endsWith("/")) base else "$base/"
        return "${baseUrlStr}webapi/entry.cgi?api=SYNO.FileStation.Thumb&version=2&method=get&path=${java.net.URLEncoder.encode(path, "UTF-8")}&size=$size&_sid=$sessionId"
    }

    // ==================== Photo 相关 URL ====================

    /**
     * 获取照片缩略图 URL (带 cache_key)
     */
    fun getPhotoThumbnailUrl(
        unitId: Long,
        cacheKey: String,
        size: String = "sm"
    ): String {
        val apiName = if (dsmVersion >= 7) "SYNO.Foto.Thumbnail" else "SYNO.Photo.Thumbnail"
        return buildString {
            append(baseUrl)
            append("/webapi/entry.cgi?")
            append("id=$unitId")
            append("&cache_key=\"$cacheKey\"")
            append("&type=\"unit\"")
            append("&size=\"$size\"")
            append("&api=\"$apiName\"")
            append("&method=\"get\"")
            append("&version=1")
            append("&_sid=$sid")
        }
    }

    /**
     * 获取照片缩略图 URL (简化版)
     */
    fun getPhotoThumbnailUrl(photoId: Long, size: String = "m"): String {
        val apiName = if (dsmVersion >= 7) "SYNO.Foto.Thumbnail" else "SYNO.Photo.Thumbnail"
        return buildString {
            append(baseUrl)
            append("/webapi/entry.cgi?")
            append("id=$photoId")
            append("&size=\"$size\"")
            append("&api=\"$apiName\"")
            append("&method=\"get\"")
            append("&version=1")
            append("&_sid=$sid")
        }
    }

    /**
     * 获取照片/视频下载 URL
     */
    fun getPhotoDownloadUrl(photoId: Long): String {
        val apiName = if (dsmVersion >= 7) "SYNO.Foto.Download" else "SYNO.Photo.Download"
        return buildString {
            append(baseUrl)
            append("/webapi/entry.cgi?")
            append("api=$apiName")
            append("&version=1")
            append("&method=download")
            append("&item_id=%5B$photoId%5D")
            append("&_sid=$sid")
        }
    }

    // ==================== Package 相关 URL ====================

    /**
     * 获取套件访问 URL
     */
    fun getPackageUrl(packageName: String): String {
        return "$baseUrl/webman/3rdparty/$packageName/?_sid=$sid"
    }

    /**
     * 获取套件图标 URL
     */
    fun getPackageIconUrl(packageName: String, size: Int = 64): String {
        return "$baseUrl/webman/3rdparty/$packageName/images/icon_${size}x${size}.png?_sid=$sid"
    }
}


