package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseIntent

/**
 * 用户详情 Intent
 */
sealed class UserDetailIntent : BaseIntent {
    data class LoadUserDetail(val username: String) : UserDetailIntent()
    data class LoadUserGroups(val username: String) : UserDetailIntent()
    data class UpdateDescription(val description: String) : UserDetailIntent()
    data class UpdateEmail(val email: String) : UserDetailIntent()
    data class UpdateNewPassword(val password: String) : UserDetailIntent()
    data class UpdateConfirmPassword(val password: String) : UserDetailIntent()
    data object ToggleCannotChangePassword : UserDetailIntent()
    data object TogglePasswordNeverExpire : UserDetailIntent()
    data class ToggleGroup(val groupName: String) : UserDetailIntent()
    data class Save(val username: String) : UserDetailIntent()
}
