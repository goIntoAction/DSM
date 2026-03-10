package wang.zengye.dsm.data.api

import okhttp3.Interceptor
import okhttp3.Response

/**
 * 动态 baseUrl 拦截器
 * DSM 的 baseUrl 是用户配置的，每次请求时从 DsmApiClient 同步获取
 */
class DynamicBaseUrlInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        // 同步获取当前 baseUrl（无阻塞风险）
        val baseUrl = DsmApiHelper.baseUrl

        if (baseUrl.isBlank()) {
            return chain.proceed(originalRequest)
        }

        // 从用户配置的 URL 中解析 scheme、host、port
        val scheme = when {
            baseUrl.startsWith("https://") -> "https"
            baseUrl.startsWith("http://") -> "http"
            else -> "https" // 默认 HTTPS
        }
        val hostPart = baseUrl
            .removePrefix("http://")
            .removePrefix("https://")
            .substringBefore("/")
        val host = hostPart.substringBefore(":")
        val port = extractPort(baseUrl, scheme)

        // 构建新的 URL，保留原请求的 path 和 query
        val newUrl = originalUrl.newBuilder()
            .scheme(scheme)
            .host(host)
            .port(port)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }

    private fun extractPort(url: String, scheme: String): Int {
        val hostPart = url
            .removePrefix("http://")
            .removePrefix("https://")
            .substringBefore("/")
        if (hostPart.contains(":")) {
            hostPart.substringAfter(":").toIntOrNull()?.let { return it }
        }
        // 无显式端口时按 scheme 返回 DSM 默认端口
        return if (scheme == "https") 5001 else 5000
    }
}
