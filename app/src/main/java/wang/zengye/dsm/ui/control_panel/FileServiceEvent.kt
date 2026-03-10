package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * 文件服务 Event
 */
sealed class FileServiceEvent : BaseEvent {
    data class ShowError(val message: String) : FileServiceEvent()
    data object ToggleSuccess : FileServiceEvent()
    data object SaveSuccess : FileServiceEvent()
}