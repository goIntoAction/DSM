package wang.zengye.dsm.ui.photos

import wang.zengye.dsm.data.model.AlbumItem
import wang.zengye.dsm.ui.base.BaseIntent

sealed class AlbumDetailIntent : BaseIntent {
    data class LoadAlbumPhotos(val albumId: Long) : AlbumDetailIntent()
    data class SetAlbum(val album: AlbumItem) : AlbumDetailIntent()
    data object ShowDeleteDialog : AlbumDetailIntent()
    data object HideDeleteDialog : AlbumDetailIntent()
    data object DeleteAlbum : AlbumDetailIntent()
}
