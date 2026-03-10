package wang.zengye.dsm.ui.setting

import wang.zengye.dsm.ui.base.BaseIntent

/**
 * OTP绑定 Intent
 */
sealed class OtpBindIntent : BaseIntent {
    data class SetEmailAndGetQrCode(val email: String) : OtpBindIntent()
    data class SetVerificationCode(val code: String) : OtpBindIntent()
    data object VerifyOtpCode : OtpBindIntent()
    data object ShowDisableDialog : OtpBindIntent()
    data object HideDisableDialog : OtpBindIntent()
    data class DisableOtp(val code: String) : OtpBindIntent()
    data object Reset : OtpBindIntent()
    data object ClearMessages : OtpBindIntent()
}
