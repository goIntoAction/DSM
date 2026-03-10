package wang.zengye.dsm.data.api

import org.junit.Assert.*
import org.junit.Test

class ApiResultTest {

    // ========== isSuccess / isError 测试 ==========

    @Test
    fun success_isSuccess_returnsTrue() {
        val result = ApiResult.Success("data")
        assertTrue(result.isSuccess)
        assertFalse(result.isError)
    }

    @Test
    fun error_isError_returnsTrue() {
        val result = ApiResult.Error(404, "Not Found")
        assertTrue(result.isError)
        assertFalse(result.isSuccess)
    }

    @Test
    fun exception_isError_returnsTrue() {
        val result = ApiResult.Exception(RuntimeException("Network error"))
        assertTrue(result.isError)
        assertFalse(result.isSuccess)
    }

    // ========== getOrNull 测试 ==========

    @Test
    fun getOrNull_returnsDataForSuccess() {
        val result = ApiResult.Success("test")
        assertEquals("test", result.getOrNull())
    }

    @Test
    fun getOrNull_returnsNullForError() {
        val result = ApiResult.Error(500, "Server Error")
        assertNull(result.getOrNull())
    }

    @Test
    fun getOrNull_returnsNullForException() {
        val result = ApiResult.Exception(RuntimeException("error"))
        assertNull(result.getOrNull())
    }

    @Test
    fun getOrNull_withNullableDataType() {
        val result: ApiResult<String?> = ApiResult.Success(null)
        assertNull(result.getOrNull())
    }

    @Test
    fun getOrNull_withComplexDataType() {
        data class User(val id: Int, val name: String)
        val user = User(1, "test")
        val result = ApiResult.Success(user)
        assertEquals(user, result.getOrNull())
    }

    // ========== map 测试 ==========

    @Test
    fun map_transformsSuccessData() {
        val result = ApiResult.Success(5)
        val mapped = result.map { it * 2 }
        assertEquals(10, (mapped as ApiResult.Success).data)
    }

    @Test
    fun map_preservesError() {
        val result = ApiResult.Error(404, "Not Found")
        val mapped = result.map { it: Int -> it * 2 }
        assertEquals(404, (mapped as ApiResult.Error).code)
        assertEquals("Not Found", mapped.message)
    }

    @Test
    fun map_preservesException() {
        val exception = RuntimeException("test")
        val result = ApiResult.Exception(exception)
        val mapped = result.map { it: Int -> it * 2 }
        assertEquals(exception, (mapped as ApiResult.Exception).throwable)
    }

    @Test
    fun map_chainsTransformations() {
        val result = ApiResult.Success(2)
        val mapped = result
            .map { it * 3 }
            .map { it + 1 }
        assertEquals(7, (mapped as ApiResult.Success).data)
    }

    @Test
    fun map_withTypeChange() {
        val result = ApiResult.Success(42)
        val mapped = result.map { "Value: $it" }
        assertEquals("Value: 42", (mapped as ApiResult.Success).data)
    }

    // ========== onSuccess 测试 ==========

    @Test
    fun onSuccess_callbackIsCalledForSuccess() {
        var called = false
        var receivedData = ""
        ApiResult.Success("data").onSuccess { 
            called = true
            receivedData = it
        }
        assertTrue(called)
        assertEquals("data", receivedData)
    }

    @Test
    fun onSuccess_callbackIsNotCalledForError() {
        var called = false
        ApiResult.Error(404, "Not Found").onSuccess { called = true }
        assertFalse(called)
    }

    @Test
    fun onSuccess_callbackIsNotCalledForException() {
        var called = false
        ApiResult.Exception(RuntimeException()).onSuccess { called = true }
        assertFalse(called)
    }

    @Test
    fun onSuccess_returnsSameResultForChaining() {
        val result = ApiResult.Success("test")
        val returned = result.onSuccess { }
        assertSame(result, returned)
    }

    // ========== onError 测试 ==========

    @Test
    fun onError_callbackIsCalledForError() {
        var errorMessage = ""
        ApiResult.Error(404, "Not Found").onError { errorMessage = it }
        assertEquals("Not Found", errorMessage)
    }

    @Test
    fun onError_callbackIsCalledForException() {
        var errorMessage = ""
        ApiResult.Exception(RuntimeException("Test error")).onError { errorMessage = it }
        assertEquals("Test error", errorMessage)
    }

    @Test
    fun onError_callbackIsNotCalledForSuccess() {
        var called = false
        ApiResult.Success("data").onError { called = true }
        assertFalse(called)
    }

    @Test
    fun onError_returnsSameResultForChaining() {
        val result = ApiResult.Error(404, "Not Found")
        val returned = result.onError { }
        assertSame(result, returned)
    }

    // ========== 链式调用测试 ==========

    @Test
    fun chaining_onSuccessAndOnError_handlesSuccess() {
        var successCalled = false
        var errorCalled = false
        
        ApiResult.Success("data")
            .onSuccess { successCalled = true }
            .onError { errorCalled = true }
        
        assertTrue(successCalled)
        assertFalse(errorCalled)
    }

    @Test
    fun chaining_onSuccessAndOnError_handlesError() {
        var successCalled = false
        var errorCalled = false
        
        ApiResult.Error(404, "Not Found")
            .onSuccess { successCalled = true }
            .onError { errorCalled = true }
        
        assertFalse(successCalled)
        assertTrue(errorCalled)
    }

    @Test
    fun chaining_mapAndOnSuccess() {
        var result = 0
        
        ApiResult.Success(5)
            .map { it * 2 }
            .onSuccess { result = it }
        
        assertEquals(10, result)
    }

    @Test
    fun chaining_mapAndOnError_forError() {
        var errorMessage = ""
        
        ApiResult.Error(500, "Server Error")
            .map { it: Int -> it * 2 }
            .onError { errorMessage = it }
        
        assertEquals("Server Error", errorMessage)
    }

    // ========== getErrorMessage 测试 ==========

    @Test
    fun getErrorMessage_forError() {
        val result = ApiResult.Error(404, "Not Found")
        assertEquals("Not Found", result.getErrorMessage())
    }

    @Test
    fun getErrorMessage_forException() {
        val result = ApiResult.Exception(RuntimeException("Network failed"))
        assertEquals("Network failed", result.getErrorMessage())
    }

    @Test
    fun getErrorMessage_forSuccess() {
        val result = ApiResult.Success("data")
        assertEquals("", result.getErrorMessage())
    }

    // ========== 边界情况测试 ==========

    @Test
    fun success_withNullData() {
        val result: ApiResult<String?> = ApiResult.Success(null)
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun success_withEmptyString() {
        val result = ApiResult.Success("")
        assertEquals("", result.getOrNull())
    }

    @Test
    fun success_withEmptyList() {
        val result = ApiResult.Success(emptyList<String>())
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun error_withEmptyMessage() {
        val result = ApiResult.Error(500, "")
        assertTrue(result.message.isEmpty())
    }

    @Test
    fun exception_withMessage() {
        val exception = RuntimeException("Test exception")
        val result = ApiResult.Exception(exception)
        assertEquals("Test exception", result.throwable.message)
    }

    @Test
    fun exception_withoutMessage() {
        val exception = RuntimeException()
        val result = ApiResult.Exception(exception)
        assertNull(result.throwable.message)
    }
}
