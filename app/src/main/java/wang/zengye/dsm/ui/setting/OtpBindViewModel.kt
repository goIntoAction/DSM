package wang.zengye.dsm.ui.setting

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wang.zengye.dsm.R
import wang.zengye.dsm.data.repository.AuthRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import wang.zengye.dsm.util.appString
import javax.inject.Inject

/**
 * OTP绑定UI状态
 */
data class OtpBindUiState(
    override val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    override val error: String? = null,
    val success: String? = null,
    val currentStep: Int = 0, // 0: 初始, 1: 输入邮箱, 2: 显示二维码, 3: 验证码验证, 4: 完成
    val email: String = "",
    val qrCodeUrl: String = "",
    val secret: String = "",
    val account: String = "",
    val verificationCode: String = "",
    val isOtpEnabled: Boolean = false,
    val showDisableDialog: Boolean = false
) : wang.zengye.dsm.ui.base.BaseState

/**
 * OTP绑定ViewModel
 * 使用 Retrofit + Moshi API
 */
@HiltViewModel
class OtpBindViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel<OtpBindUiState, OtpBindIntent, OtpBindEvent>() {

    companion object {
        private const val TAG = "OtpBindViewModel"
    }

    private val _state = MutableStateFlow(OtpBindUiState())
    override val state: StateFlow<OtpBindUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<OtpBindEvent>(extraBufferCapacity = 1)
    override val events = _events.asSharedFlow()

    override suspend fun processIntent(intent: OtpBindIntent) {
        when (intent) {
            is OtpBindIntent.SetEmailAndGetQrCode -> setEmailAndGetQrCode(intent.email)
            is OtpBindIntent.SetVerificationCode -> setVerificationCode(intent.code)
            is OtpBindIntent.VerifyOtpCode -> verifyOtpCode()
            is OtpBindIntent.ShowDisableDialog -> _state.update { it.copy(showDisableDialog = true) }
            is OtpBindIntent.HideDisableDialog -> _state.update { it.copy(showDisableDialog = false) }
            is OtpBindIntent.DisableOtp -> disableOtp(intent.code)
            is OtpBindIntent.Reset -> reset()
            is OtpBindIntent.ClearMessages -> clearMessages()
        }
    }

    private suspend fun setEmailAndGetQrCode(email: String) {
        _state.update { it.copy(isLoading = true, email = email, error = null) }

        // 先保存邮箱
        authRepository.saveOtpMail(email)
            .onSuccess {
                // 获取二维码
                getQrCode(email)
            }
            .onFailure { exception ->
                val errorMsg = exception.message ?: appString(R.string.common_error)
                _state.update { it.copy(isLoading = false, error = errorMsg) }
                _events.emit(OtpBindEvent.ShowError(errorMsg))
            }
    }

    private suspend fun getQrCode(account: String) {
        authRepository.getOtpQrCode(account)
            .onSuccess { response ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        currentStep = 2,
                        qrCodeUrl = response.data?.qrCodeUrl ?: "",
                        secret = response.data?.secret ?: "",
                        account = account
                    )
                }
            }
            .onFailure { exception ->
                val errorMsg = exception.message ?: appString(R.string.common_error)
                _state.update { it.copy(isLoading = false, error = errorMsg) }
                _events.emit(OtpBindEvent.ShowError(errorMsg))
            }
    }

    private fun setVerificationCode(code: String) {
        _state.update { it.copy(verificationCode = code) }
    }

    private suspend fun verifyOtpCode() {
        val code = _state.value.verificationCode
        if (code.length < 6) {
            val errorMsg = appString(R.string.otp_enter_6_digit_error)
            _state.update { it.copy(error = errorMsg) }
            _events.emit(OtpBindEvent.ShowError(errorMsg))
            return
        }

        _state.update { it.copy(isSaving = true, error = null) }

        authRepository.authOtpCode(code)
            .onSuccess { response ->
                _state.update {
                    it.copy(
                        isSaving = false,
                        currentStep = 4,
                        success = appString(R.string.otp_2fa_enabled_msg),
                        isOtpEnabled = true
                    )
                }
                _events.emit(OtpBindEvent.OtpEnabled)
                _events.emit(OtpBindEvent.ShowSuccess(appString(R.string.otp_2fa_enabled_msg)))
            }
            .onFailure { exception ->
                val errorMsg = exception.message ?: appString(R.string.common_error)
                _state.update { it.copy(isSaving = false, error = errorMsg) }
                _events.emit(OtpBindEvent.ShowError(errorMsg))
            }
    }

    private suspend fun disableOtp(code: String) {
        _state.update { it.copy(isSaving = true, showDisableDialog = false, error = null) }

        authRepository.authOtpCode(code)
            .onSuccess {
                _state.update {
                    it.copy(
                        isSaving = false,
                        isOtpEnabled = false,
                        currentStep = 0,
                        success = appString(R.string.otp_2fa_disabled_msg)
                    )
                }
                _events.emit(OtpBindEvent.OtpDisabled)
                _events.emit(OtpBindEvent.ShowSuccess(appString(R.string.otp_2fa_disabled_msg)))
            }
            .onFailure { exception ->
                val errorMsg = exception.message ?: appString(R.string.common_error)
                _state.update { it.copy(isSaving = false, error = errorMsg) }
                _events.emit(OtpBindEvent.ShowError(errorMsg))
            }
    }

    private fun reset() {
        _state.update {
            OtpBindUiState(
                isOtpEnabled = _state.value.isOtpEnabled
            )
        }
    }

    private fun clearMessages() {
        _state.update { it.copy(error = null, success = null) }
    }
}
