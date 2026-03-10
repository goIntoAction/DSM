package wang.zengye.dsm.ui.file

import wang.zengye.dsm.ui.base.BaseIntent

/**
 * 文件详情 Intent
 */
sealed class FileDetailIntent : BaseIntent {
    data class LoadFileDetail(val path: String) : FileDetailIntent()
    data class RenameFile(val path: String, val newName: String) : FileDetailIntent()
    data class DeleteFile(val path: String) : FileDetailIntent()
    data class ToggleFavorite(val path: String, val name: String) : FileDetailIntent()
    data class CreateShareLink(val path: String) : FileDetailIntent()
    data object ShowRenameDialog : FileDetailIntent()
    data object HideRenameDialog : FileDetailIntent()
    data object ShowDeleteDialog : FileDetailIntent()
    data object HideDeleteDialog : FileDetailIntent()
    data object ShowShareDialog : FileDetailIntent()
    data object HideShareDialog : FileDetailIntent()
    data object DownloadFile : FileDetailIntent()
    data object ClearMessage : FileDetailIntent()
}
