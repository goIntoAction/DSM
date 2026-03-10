package wang.zengye.dsm.ui.login

import wang.zengye.dsm.ui.base.BaseIntent

/**
 * 登录 Intent
 */
sealed class LoginIntent : BaseIntent {
    data class SetHost(val host: String) : LoginIntent()
    data class SetAccount(val account: String) : LoginIntent()
    data class SetPassword(val password: String) : LoginIntent()
    data class SetOtpCode(val code: String) : LoginIntent()
    data class SetRememberDevice(val remember: Boolean) : LoginIntent()
    data class SetCheckSsl(val check: Boolean) : LoginIntent()
    data object Login : LoginIntent()
    data object Logout : LoginIntent()
    data object AutoLogin : LoginIntent()
    data object ClearError : LoginIntent()
}
