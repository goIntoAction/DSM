package wang.zengye.dsm.ui.search

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * 全局搜索 Event
 */
sealed class UniversalSearchEvent : BaseEvent {
    data class ShowError(val message: String) : UniversalSearchEvent()
    data object SearchCompleted : UniversalSearchEvent()
}
