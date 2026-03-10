package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * 共享文件夹 Event
 */
sealed class SharedFoldersEvent : BaseEvent {
    data class ShowError(val message: String) : SharedFoldersEvent()
    data object DeleteSuccess : SharedFoldersEvent()
    data object CleanRecycleBinSuccess : SharedFoldersEvent()
}
