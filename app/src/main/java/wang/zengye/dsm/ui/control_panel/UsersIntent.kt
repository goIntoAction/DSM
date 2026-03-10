package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseIntent

/**
 * 用户管理 Intent
 */
sealed class UsersIntent : BaseIntent {
    data object LoadUsers : UsersIntent()
    data class CreateUser(
        val username: String,
        val password: String,
        val description: String = "",
        val email: String = ""
    ) : UsersIntent()
    data class UpdateUser(
        val username: String,
        val description: String? = null,
        val email: String? = null,
        val newPassword: String? = null
    ) : UsersIntent()
    data class DeleteUser(val username: String) : UsersIntent()
}
