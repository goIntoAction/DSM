package wang.zengye.dsm.ui.login

import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import wang.zengye.dsm.data.repository.AuthRepository

/**
 * LoginViewModel 单元测试
 * 
 * 注意：由于 LoginViewModel 依赖 SettingsManager 和 DsmApiClient（静态单例），
 * 这些测试需要更复杂的 mock 设置。此测试文件主要测试基本的 Intent 处理逻辑。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var authRepository: AuthRepository

    @Before
    fun setup() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        
        authRepository = mockk(relaxed = true)
        
        // 注意：SettingsManager 是单例，测试时需要额外处理
        // 这里我们只测试基本的 ViewModel 逻辑，不涉及 SettingsManager 的实际调用
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ========== LoginUiState 测试 ==========

    @Test
    fun loginUiState_defaultValues() {
        val state = LoginUiState()
        
        assertEquals("", state.host)
        assertEquals("", state.account)
        assertEquals("", state.password)
        assertEquals("", state.otpCode)
        assertFalse(state.rememberDevice)
        assertTrue(state.checkSsl)
        assertFalse(state.requireOtp)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNull(state.hostError)
        assertNull(state.accountError)
        assertNull(state.passwordError)
    }

    @Test
    fun loginUiState_copy_preservesValues() {
        val original = LoginUiState(
            host = "https://example.com",
            account = "admin",
            password = "password123",
            otpCode = "123456",
            rememberDevice = true,
            checkSsl = false,
            requireOtp = true,
            isLoading = true,
            error = "Test error"
        )
        
        val copied = original.copy(account = "user")
        
        assertEquals("https://example.com", copied.host)
        assertEquals("user", copied.account)
        assertEquals("password123", copied.password)
        assertEquals("123456", copied.otpCode)
        assertTrue(copied.rememberDevice)
        assertFalse(copied.checkSsl)
        assertTrue(copied.requireOtp)
        assertTrue(copied.isLoading)
        assertEquals("Test error", copied.error)
    }

    @Test
    fun loginUiState_withAllFields() {
        val state = LoginUiState(
            host = "192.168.1.1:5001",
            account = "test",
            password = "secret",
            otpCode = "654321",
            rememberDevice = true,
            checkSsl = false,
            requireOtp = true,
            isLoading = false,
            error = "Previous error",
            hostError = "Host error",
            accountError = "Account error",
            passwordError = "Password error",
            deviceId = "device-123"
        )
        
        assertEquals("192.168.1.1:5001", state.host)
        assertEquals("test", state.account)
        assertEquals("secret", state.password)
        assertEquals("654321", state.otpCode)
        assertTrue(state.rememberDevice)
        assertFalse(state.checkSsl)
        assertTrue(state.requireOtp)
        assertFalse(state.isLoading)
        assertEquals("Previous error", state.error)
        assertEquals("Host error", state.hostError)
        assertEquals("Account error", state.accountError)
        assertEquals("Password error", state.passwordError)
        assertEquals("device-123", state.deviceId)
    }

    // ========== LoginIntent 测试 ==========

    @Test
    fun loginIntent_types() {
        // 验证 Intent 类型
        val setHost = LoginIntent.SetHost("example.com")
        assertTrue(setHost is LoginIntent)
        
        val setAccount = LoginIntent.SetAccount("admin")
        assertTrue(setAccount is LoginIntent)
        
        val setPassword = LoginIntent.SetPassword("pass")
        assertTrue(setPassword is LoginIntent)
        
        val setOtp = LoginIntent.SetOtpCode("123456")
        assertTrue(setOtp is LoginIntent)
        
        val setRemember = LoginIntent.SetRememberDevice(true)
        assertTrue(setRemember is LoginIntent)
        
        val setCheckSsl = LoginIntent.SetCheckSsl(false)
        assertTrue(setCheckSsl is LoginIntent)
        
        val login = LoginIntent.Login
        assertTrue(login is LoginIntent)
        
        val logout = LoginIntent.Logout
        assertTrue(logout is LoginIntent)
        
        val autoLogin = LoginIntent.AutoLogin
        assertTrue(autoLogin is LoginIntent)
        
        val clearError = LoginIntent.ClearError
        assertTrue(clearError is LoginIntent)
    }

    // ========== LoginEvent 测试 ==========

    @Test
    fun loginEvent_types() {
        val success = LoginEvent.LoginSuccess("sid123", "token456")
        assertTrue(success is LoginEvent)
        assertEquals("sid123", success.sid)
        assertEquals("token456", success.synoToken)
        
        val error = LoginEvent.ShowError("Error message")
        assertTrue(error is LoginEvent)
        assertEquals("Error message", error.message)
        
        val requireOtp = LoginEvent.RequireOtp("OTP required")
        assertTrue(requireOtp is LoginEvent)
        assertEquals("OTP required", requireOtp.message)
        
        val deviceUnauthorized = LoginEvent.DeviceUnauthorized
        assertTrue(deviceUnauthorized is LoginEvent)
    }

    // ========== 边界情况测试 ==========

    @Test
    fun loginUiState_emptyStrings() {
        val state = LoginUiState(
            host = "",
            account = "",
            password = ""
        )
        
        assertEquals("", state.host)
        assertEquals("", state.account)
        assertEquals("", state.password)
    }

    @Test
    fun loginUiState_whitespaceStrings() {
        val state = LoginUiState(
            host = "  ",
            account = "\t",
            password = "\n"
        )
        
        assertEquals("  ", state.host)
        assertEquals("\t", state.account)
        assertEquals("\n", state.password)
    }

    @Test
    fun loginUiState_longStrings() {
        val longString = "a".repeat(10000)
        val state = LoginUiState(
            host = longString,
            account = longString,
            password = longString
        )
        
        assertEquals(longString, state.host)
        assertEquals(longString, state.account)
        assertEquals(longString, state.password)
    }

    @Test
    fun loginUiState_specialCharacters() {
        val state = LoginUiState(
            host = "https://example.com:5001/path?query=value",
            password = "p@ss!w0rd#$%^&*()",
            otpCode = "123456"
        )
        
        assertEquals("https://example.com:5001/path?query=value", state.host)
        assertEquals("p@ss!w0rd#$%^&*()", state.password)
        assertEquals("123456", state.otpCode)
    }

    @Test
    fun loginIntent_setHost_value() {
        val intent = LoginIntent.SetHost("test.local")
        assertEquals("test.local", intent.host)
    }

    @Test
    fun loginIntent_setAccount_value() {
        val intent = LoginIntent.SetAccount("username")
        assertEquals("username", intent.account)
    }

    @Test
    fun loginIntent_setPassword_value() {
        val intent = LoginIntent.SetPassword("mypassword")
        assertEquals("mypassword", intent.password)
    }

    @Test
    fun loginIntent_setOtpCode_value() {
        val intent = LoginIntent.SetOtpCode("654321")
        assertEquals("654321", intent.code)
    }

    @Test
    fun loginIntent_setRememberDevice_value() {
        val intentTrue = LoginIntent.SetRememberDevice(true)
        assertTrue(intentTrue.remember)
        
        val intentFalse = LoginIntent.SetRememberDevice(false)
        assertFalse(intentFalse.remember)
    }

    @Test
    fun loginIntent_setCheckSsl_value() {
        val intentTrue = LoginIntent.SetCheckSsl(true)
        assertTrue(intentTrue.check)
        
        val intentFalse = LoginIntent.SetCheckSsl(false)
        assertFalse(intentFalse.check)
    }

    @Test
    fun loginEvent_loginSuccess_values() {
        val event = LoginEvent.LoginSuccess("session-id-123", "syno-token-456")
        assertEquals("session-id-123", event.sid)
        assertEquals("syno-token-456", event.synoToken)
    }

    @Test
    fun loginEvent_showError_value() {
        val event = LoginEvent.ShowError("Something went wrong")
        assertEquals("Something went wrong", event.message)
    }

    @Test
    fun loginEvent_requireOtp_value() {
        val event = LoginEvent.RequireOtp("Please enter OTP code")
        assertEquals("Please enter OTP code", event.message)
    }
}