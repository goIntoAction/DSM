package wang.zengye.dsm.ui.login

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * 登录 Event
 */
sealed class LoginEvent : BaseEvent {
    data class LoginSuccess(val sid: String, val synoToken: String) : LoginEvent()
    data class ShowError(val message: String) : LoginEvent()
    data class RequireOtp(val message: String) : LoginEvent()
    data object DeviceUnauthorized : LoginEvent()
}
