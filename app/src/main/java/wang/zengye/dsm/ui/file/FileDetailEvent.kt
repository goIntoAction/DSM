package wang.zengye.dsm.ui.file

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * 文件详情 Event
 */
sealed class FileDetailEvent : BaseEvent {
    data class ShowError(val message: String) : FileDetailEvent()
    data class ShowMessage(val message: String) : FileDetailEvent()
    data object FileDeleted : FileDetailEvent()
    data object FileRenamed : FileDetailEvent()
    data class StartDownload(val path: String, val fileName: String) : FileDetailEvent()
}
