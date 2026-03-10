package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * 添加/编辑共享文件夹 Event
 */
sealed class AddShareFolderEvent : BaseEvent {
    data class ShowError(val message: String) : AddShareFolderEvent()
    data object SaveSuccess : AddShareFolderEvent()
}
