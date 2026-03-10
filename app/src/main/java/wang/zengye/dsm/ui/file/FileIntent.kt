package wang.zengye.dsm.ui.file

import wang.zengye.dsm.ui.base.BaseIntent

sealed class FileIntent : BaseIntent {
    data class LoadFiles(val path: String) : FileIntent()
    data class NavigateTo(val path: String) : FileIntent()
    data object NavigateUp : FileIntent()
    data class ToggleSelection(val path: String) : FileIntent()
    data object ClearSelection : FileIntent()
    data object ToggleSelectAll : FileIntent()
    data object EnterSelectionMode : FileIntent()
    data object ExitSelectionMode : FileIntent()
    data object ToggleViewMode : FileIntent()
    data class SortFiles(val sortBy: String) : FileIntent()

    // 文件操作
    data class CreateFolder(val name: String) : FileIntent()
    data class Rename(val path: String, val newName: String) : FileIntent()
    data class Delete(val paths: List<String>) : FileIntent()
    data class Copy(val paths: List<String>, val destPath: String) : FileIntent()
    data class Move(val paths: List<String>, val destPath: String) : FileIntent()
    data class Compress(val paths: List<String>, val destPath: String, val password: String? = null) : FileIntent()
    data class Extract(val filePath: String, val destPath: String, val password: String? = null) : FileIntent()

    // 搜索
    data class Search(val query: String) : FileIntent()
    data object ClearSearch : FileIntent()

    // 其他
    data class AddToFavorite(val path: String, val name: String) : FileIntent()
    data class CreateShareLink(val path: String) : FileIntent()

    // 文件点击处理
    data class HandleFileClick(
        val path: String,
        val name: String,
        val isDir: Boolean
    ) : FileIntent()
}
