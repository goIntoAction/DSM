package wang.zengye.dsm.ui.login

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * 账户管理 Event
 */
sealed class AccountsEvent : BaseEvent {
    data object AccountAdded : AccountsEvent()
    data object AccountUpdated : AccountsEvent()
    data object AccountDeleted : AccountsEvent()
    data object DefaultAccountChanged : AccountsEvent()
}
