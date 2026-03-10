package wang.zengye.dsm.ui.file

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * 文件搜索 Event
 */
sealed class FileSearchEvent : BaseEvent {
    data class ShowError(val message: String) : FileSearchEvent()
    data object SearchCompleted : FileSearchEvent()
}
