package wang.zengye.dsm.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import wang.zengye.dsm.data.model.*

/**
 * AuthRepository 单元测试
 * 主要测试 Model 类的定义和基本逻辑
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {

    // ========== LoginDto 测试 ==========

    @Test
    fun loginDto_success() {
        val dto = LoginDto(
            success = true,
            data = LoginDataDto(
                did = "device-123",
                sid = "session-456",
                synoToken = "token-789"
            )
        )
        
        assertTrue(dto.success)
        assertEquals("device-123", dto.data?.did)
        assertEquals("session-456", dto.data?.sid)
        assertEquals("token-789", dto.data?.synoToken)
    }

    @Test
    fun loginDto_error() {
        val dto = LoginDto(
            success = false,
            error = ApiErrorDto(code = 400, errors = listOf("Invalid credentials"))
        )
        
        assertFalse(dto.success)
        assertEquals(400, dto.error?.code)
        assertEquals("Invalid credentials", dto.error?.errors?.first())
    }

    @Test
    fun loginDto_withTopLevelSid() {
        val dto = LoginDto(
            success = true,
            sid = "top-level-sid"
        )
        
        assertTrue(dto.success)
        assertEquals("top-level-sid", dto.sid)
    }

    // ========== LoginDataDto 测试 ==========

    @Test
    fun loginDataDto_propertiesAreCorrect() {
        val data = LoginDataDto(
            did = "device-abc",
            sid = "session-xyz",
            synoToken = "token-123"
        )
        
        assertEquals("device-abc", data.did)
        assertEquals("session-xyz", data.sid)
        assertEquals("token-123", data.synoToken)
    }

    // ========== OtpQrCodeDto 测试 ==========

    @Test
    fun otpQrCodeDto_success() {
        val dto = OtpQrCodeDto(
            success = true,
            data = OtpQrCodeDataDto(
                qrCodeUrl = "otpauth://totp/...",
                secret = "SECRET123"
            )
        )
        
        assertTrue(dto.success)
        assertEquals("otpauth://totp/...", dto.data?.qrCodeUrl)
        assertEquals("SECRET123", dto.data?.secret)
    }

    @Test
    fun otpQrCodeDto_error() {
        val dto = OtpQrCodeDto(
            success = false,
            error = ApiErrorDto(code = 401, errors = listOf("Unauthorized"))
        )
        
        assertFalse(dto.success)
        assertEquals(401, dto.error?.code)
    }

    // ========== OtpAuthDto 测试 ==========

    @Test
    fun otpAuthDto_success() {
        val dto = OtpAuthDto(success = true)
        
        assertTrue(dto.success)
        assertNull(dto.error)
    }

    @Test
    fun otpAuthDto_error() {
        val dto = OtpAuthDto(
            success = false,
            error = ApiErrorDto(code = 400, errors = listOf("Invalid OTP code"))
        )
        
        assertFalse(dto.success)
        assertEquals(400, dto.error?.code)
    }

    // ========== ApiErrorDto 测试 ==========

    @Test
    fun apiErrorDto_propertiesAreCorrect() {
        val error = ApiErrorDto(
            code = 500,
            errors = listOf("Internal server error", "Please try again")
        )
        
        assertEquals(500, error.code)
        assertEquals(2, error.errors?.size)
        assertEquals("Internal server error", error.errors?.first())
    }

    @Test
    fun apiErrorDto_noErrors() {
        val error = ApiErrorDto(code = 404)
        
        assertEquals(404, error.code)
        assertNull(error.errors)
    }

    // ========== 边界情况测试 ==========

    @Test
    fun loginDto_defaultValues() {
        val dto = LoginDto()
        
        assertFalse(dto.success)
        assertNull(dto.error)
        assertNull(dto.data)
        assertNull(dto.sid)
    }

    @Test
    fun loginDataDto_defaultValues() {
        val data = LoginDataDto()
        
        assertEquals("", data.did)
        assertEquals("", data.sid)
        assertEquals("", data.synoToken)
    }

    @Test
    fun otpQrCodeDataDto_defaultValues() {
        val data = OtpQrCodeDataDto()
        
        assertEquals("", data.qrCodeUrl)
        assertEquals("", data.secret)
    }

    @Test
    fun otpAuthDto_defaultValues() {
        val dto = OtpAuthDto()
        
        assertFalse(dto.success)
        assertNull(dto.error)
    }
}
