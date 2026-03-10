package wang.zengye.dsm.ui.file

import wang.zengye.dsm.ui.base.BaseIntent

sealed class FavoriteIntent : BaseIntent {
    data object LoadFavorites : FavoriteIntent()
    data class ShowRenameDialog(val item: FavoriteItem) : FavoriteIntent()
    data class ShowDeleteDialog(val item: FavoriteItem) : FavoriteIntent()
    data object HideDialogs : FavoriteIntent()
    data class UpdateNewName(val name: String) : FavoriteIntent()
    data object RenameFavorite : FavoriteIntent()
    data object DeleteFavorite : FavoriteIntent()
}
