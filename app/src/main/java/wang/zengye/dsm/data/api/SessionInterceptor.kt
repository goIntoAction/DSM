package wang.zengye.dsm.data.api

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response

/**
 * 会话拦截器
 * 负责添加 Cookie、X-SYNO-TOKEN、Accept、Origin、Referer 等请求头
 */
class SessionInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // 对于 multipart 上传请求，使用 */* 作为 Accept
        val isMultipart = originalRequest.body?.contentType()?.let {
            it.type == "multipart" && it.subtype == "form-data"
        } ?: false

        if (isMultipart) {
            requestBuilder.header("Accept", "*/*")
            requestBuilder.header("Accept-Encoding", "gzip, deflate")
        } else {
            requestBuilder.header("Accept", "application/json")
        }

        requestBuilder.header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6,zh-TW;q=0.5")

        // 添加 Cookie
        if (DsmApiHelper.cookie.isNotEmpty()) {
            requestBuilder.header("Cookie", DsmApiHelper.cookie)
        }

        // 添加 X-SYNO-TOKEN（DSM 7 CSRF 防护）
        if (DsmApiHelper.synoToken.isNotEmpty()) {
            requestBuilder.header("X-SYNO-TOKEN", DsmApiHelper.synoToken)
        }

        // 添加 Origin 和 Referer
        val base = if (DsmApiHelper.baseUrl.isNotEmpty()) DsmApiHelper.baseUrl else "http://localhost/"
        val origin = if (base.endsWith("/")) base.substring(0, base.length - 1) else base
        requestBuilder.header("Origin", origin)
        requestBuilder.header("Referer", base)

        return chain.proceed(requestBuilder.build())
    }
}
