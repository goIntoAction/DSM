package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseIntent

/**
 * 媒体索引 Intent
 */
sealed class MediaIndexIntent : BaseIntent {
    data object LoadMediaIndexStatus : MediaIndexIntent()
    data object ToggleAutoIndex : MediaIndexIntent()
    data object ToggleIndexVideo : MediaIndexIntent()
    data object ToggleIndexPhoto : MediaIndexIntent()
    data object ToggleIndexMusic : MediaIndexIntent()
    data class ToggleFolder(val path: String) : MediaIndexIntent()
    data object ShowReindexDialog : MediaIndexIntent()
    data object HideReindexDialog : MediaIndexIntent()
    data object StartReindex : MediaIndexIntent()
    data object SaveSettings : MediaIndexIntent()
}
