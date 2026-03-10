package wang.zengye.dsm.ui.iscsi

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * iSCSI 管理 Event
 */
sealed class IscsiEvent : BaseEvent {
    data class ShowError(val message: String) : IscsiEvent()
    data class ShowSuccess(val message: String) : IscsiEvent()
}