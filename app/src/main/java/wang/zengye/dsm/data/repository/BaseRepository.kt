package wang.zengye.dsm.data.repository

import retrofit2.Response

/**
 * Repository 基类
 * 提供通用的响应处理方法
 */
abstract class BaseRepository {

    /**
     * 处理 Retrofit Response
     * @return Result<T> 成功返回 body，失败返回异常
     */
    protected fun <T> handleResponse(response: Response<T>): Result<T> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception("Empty response body"))
            }
        } else {
            Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
        }
    }
}
