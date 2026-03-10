package wang.zengye.dsm.ui.file

import wang.zengye.dsm.ui.base.BaseIntent

/**
 * 文件搜索 Intent
 */
sealed class FileSearchIntent : BaseIntent {
    data class SetSearchQuery(val query: String) : FileSearchIntent()
    data class SetSearchPath(val path: String) : FileSearchIntent()
    data class SetRecursive(val enabled: Boolean) : FileSearchIntent()
    data class SetSearchContent(val enabled: Boolean) : FileSearchIntent()
    data object StartSearch : FileSearchIntent()
    data object CancelSearch : FileSearchIntent()
    data object ClearResults : FileSearchIntent()
}