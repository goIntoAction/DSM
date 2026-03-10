package wang.zengye.dsm.ui.photos

import wang.zengye.dsm.ui.base.BaseEvent

sealed class PhotosEvent : BaseEvent {
    data class ShowError(val message: String) : PhotosEvent()
    data class UploadComplete(val successCount: Int, val failCount: Int) : PhotosEvent()
    data class AlbumCreated(val albumId: Long, val albumName: String) : PhotosEvent()
}
