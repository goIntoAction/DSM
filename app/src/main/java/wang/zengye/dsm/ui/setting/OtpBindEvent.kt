package wang.zengye.dsm.ui.setting

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * OTP绑定 Event
 */
sealed class OtpBindEvent : BaseEvent {
    data class ShowError(val message: String) : OtpBindEvent()
    data class ShowSuccess(val message: String) : OtpBindEvent()
    data object OtpEnabled : OtpBindEvent()
    data object OtpDisabled : OtpBindEvent()
}
