package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * 用户管理 Event
 */
sealed class UsersEvent : BaseEvent {
    data class ShowError(val message: String) : UsersEvent()
    data object CreateUserSuccess : UsersEvent()
    data object UpdateUserSuccess : UsersEvent()
    data object DeleteUserSuccess : UsersEvent()
}
