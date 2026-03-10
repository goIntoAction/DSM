package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * 群组管理 Event
 */
sealed class GroupsEvent : BaseEvent {
    data class ShowError(val message: String) : GroupsEvent()
    data object DeleteSuccess : GroupsEvent()
}
