package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * 用户详情 Event
 */
sealed class UserDetailEvent : BaseEvent {
    data class ShowError(val message: String) : UserDetailEvent()
    data object SaveSuccess : UserDetailEvent()
}
