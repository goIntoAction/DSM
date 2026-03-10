package wang.zengye.dsm.ui.login

import android.util.Log
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wang.zengye.dsm.R
import wang.zengye.dsm.data.api.DsmApiHelper
import wang.zengye.dsm.data.repository.AuthRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import wang.zengye.dsm.util.SettingsManager
import wang.zengye.dsm.util.appString
import java.util.UUID
import javax.inject.Inject

data class LoginUiState(
    val host: String = "",
    val account: String = "",
    val password: String = "",
    val otpCode: String = "",
    val rememberDevice: Boolean = false,
    val checkSsl: Boolean = true,
    val requireOtp: Boolean = false,
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val hostError: String? = null,
    val accountError: String? = null,
    val passwordError: String? = null,
    val deviceId: String = ""
) : wang.zengye.dsm.ui.base.BaseState

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel<LoginUiState, LoginIntent, LoginEvent>() {

    companion object {
        private const val TAG = "LoginViewModel"
    }

    private val _state = MutableStateFlow(LoginUiState(deviceId = UUID.randomUUID().toString()))
    override val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>(extraBufferCapacity = 1)
    override val events = _events.asSharedFlow()

    private var autoLoginTriggered = false

    init {
        loadSavedData()
    }

    override suspend fun processIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.SetHost -> setHost(intent.host)
            is LoginIntent.SetAccount -> setAccount(intent.account)
            is LoginIntent.SetPassword -> setPassword(intent.password)
            is LoginIntent.SetOtpCode -> setOtpCode(intent.code)
            is LoginIntent.SetRememberDevice -> setRememberDevice(intent.remember)
            is LoginIntent.SetCheckSsl -> setCheckSsl(intent.check)
            is LoginIntent.Login -> login()
            is LoginIntent.Logout -> logout()
            is LoginIntent.AutoLogin -> autoLoginIfPossible()
            is LoginIntent.ClearError -> clearError()
        }
    }

    private fun loadSavedData() {
        viewModelScope.launch {
            val savedHost = SettingsManager.host.first()
            val savedAccount = SettingsManager.account.first()
            val savedPassword = SettingsManager.password.value
            val savedCheckSsl = SettingsManager.checkSsl.first()

            _state.update { state ->
                state.copy(
                    host = savedHost,
                    account = savedAccount,
                    password = savedPassword,
                    checkSsl = savedCheckSsl
                )
            }
        }
    }

    private fun setHost(host: String) {
        _state.update { it.copy(host = host.trim(), hostError = null, error = null) }
    }

    private fun setAccount(account: String) {
        _state.update { it.copy(account = account.trim(), accountError = null, error = null) }
    }

    private fun setPassword(password: String) {
        _state.update { it.copy(password = password, passwordError = null, error = null) }
    }

    private fun setOtpCode(code: String) {
        _state.update { it.copy(otpCode = code.trim(), error = null) }
    }

    private fun setRememberDevice(remember: Boolean) {
        _state.update { it.copy(rememberDevice = remember) }
    }

    private fun setCheckSsl(check: Boolean) {
        _state.update { it.copy(checkSsl = check) }
    }

    private fun clearError() {
        _state.update { it.copy(error = null, hostError = null, accountError = null, passwordError = null) }
    }

    private suspend fun autoLoginIfPossible() {
        val state = _state.value
        if (autoLoginTriggered) return
        if (state.host.isBlank() || state.account.isBlank() || state.password.isBlank()) return
        autoLoginTriggered = true
        login()
    }

    private suspend fun login() {
        val state = _state.value

        // 验证输入
        var hasError = false
        _state.update { currentState ->
            var newState = currentState
            if (state.host.isBlank()) {
                newState = newState.copy(hostError = appString(R.string.login_error_host_required))
                hasError = true
            }
            if (state.account.isBlank()) {
                newState = newState.copy(accountError = appString(R.string.login_error_account_required))
                hasError = true
            }
            if (state.password.isBlank()) {
                newState = newState.copy(passwordError = appString(R.string.login_error_password_required))
                hasError = true
            }
            newState
        }

        if (hasError) return

        _state.update { it.copy(isLoading = true, error = null) }

        try {
            // 智能构建基础 URL
            var host = state.host.trim()
            val useHttps: Boolean

            when {
                host.startsWith("https://", ignoreCase = true) -> {
                    useHttps = true
                }
                host.startsWith("http://", ignoreCase = true) -> {
                    useHttps = false
                }
                host.contains("quickconnect", ignoreCase = true) -> {
                    useHttps = true
                    host = "https://$host"
                }
                else -> {
                    useHttps = true
                    if (!host.contains(":")) {
                        host = "$host:5001"
                    }
                    host = "https://$host"
                }
            }

            val baseUrl = host

            // 保存 SSL 设置
            SettingsManager.setCheckSsl(state.checkSsl)
            DsmApiHelper.recreateHttpClient(state.checkSsl)
            DsmApiHelper.updateSession("", "", baseUrl)

            // 使用 Moshi API 登录
            val result = authRepository.login(
                account = state.account,
                password = state.password,
                otpCode = state.otpCode.ifBlank { null }
            )

            result
                .onSuccess { response ->
                    // 获取 sid - 可能在 data 对象中或顶层
                    val sid = response.data?.sid ?: response.sid ?: ""
                    val synoToken = response.data?.synoToken ?: DsmApiHelper.synoToken

                    val currentCookie = DsmApiHelper.cookie
                    val sessionId = DsmApiHelper.resolveSessionId(sid, currentCookie)
                    DsmApiHelper.updateSession(sessionId, currentCookie, baseUrl)
                    if (synoToken.isNotBlank()) {
                        DsmApiHelper.updateSynoToken(synoToken)
                    }

                    // 保存设置
                    SettingsManager.setHost(baseUrl)
                    SettingsManager.setAccount(state.account)
                    SettingsManager.setPassword(state.password)
                    SettingsManager.setSid(sessionId)
                    SettingsManager.setCookie(currentCookie)
                    if (synoToken.isNotBlank()) {
                        SettingsManager.setSynoToken(synoToken)
                    }

                    _state.update { it.copy(isLoading = false) }
                    _events.emit(LoginEvent.LoginSuccess(sessionId, synoToken))
                }
                .onFailure { exception ->
                    Log.e(TAG, "Login failed", exception)
                    val errorMessage = exception.message ?: appString(R.string.login_error_failed)

                    // 检查特定错误码
                    when {
                        errorMessage.contains("403:") || errorMessage.contains("407:") || 
                        errorMessage.contains("OTP", ignoreCase = true) -> {
                            _state.update {
                                it.copy(isLoading = false, requireOtp = true, error = appString(R.string.login_error_otp_required))
                            }
                            _events.emit(LoginEvent.RequireOtp(appString(R.string.login_error_otp_required)))
                        }
                        errorMessage.contains("408:") || errorMessage.contains("416:") -> {
                            _state.update { it.copy(isLoading = false, error = appString(R.string.login_error_device_unauthorized)) }
                            _events.emit(LoginEvent.DeviceUnauthorized)
                        }
                        else -> {
                            _state.update { it.copy(isLoading = false, error = errorMessage) }
                            _events.emit(LoginEvent.ShowError(errorMessage))
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Login exception", e)
            val errorMsg = e.message ?: appString(R.string.login_error_failed)
            _state.update { it.copy(isLoading = false, error = errorMsg) }
            _events.emit(LoginEvent.ShowError(errorMsg))
        }
    }

    private suspend fun logout() {
        authRepository.logout()
        SettingsManager.clearSession()
        DsmApiHelper.clearSession()
    }
}