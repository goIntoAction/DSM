package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseIntent

/**
 * 共享文件夹 Intent
 */
sealed class SharedFoldersIntent : BaseIntent {
    data object LoadShares : SharedFoldersIntent()
    data class DeleteShare(val name: String) : SharedFoldersIntent()
    data class CleanRecycleBin(val name: String) : SharedFoldersIntent()
}
