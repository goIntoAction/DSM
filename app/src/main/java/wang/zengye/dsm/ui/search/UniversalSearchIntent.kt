package wang.zengye.dsm.ui.search

import wang.zengye.dsm.ui.base.BaseIntent

/**
 * 全局搜索 Intent
 */
sealed class UniversalSearchIntent : BaseIntent {
    data class SetQuery(val query: String) : UniversalSearchIntent()
    data class SetScope(val scope: SearchScope) : UniversalSearchIntent()
    data object Clear : UniversalSearchIntent()
}
