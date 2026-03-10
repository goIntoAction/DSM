package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * 媒体索引 Event
 */
sealed class MediaIndexEvent : BaseEvent {
    data class ShowError(val message: String) : MediaIndexEvent()
    data object ReindexSuccess : MediaIndexEvent()
    data object SaveSuccess : MediaIndexEvent()
}
