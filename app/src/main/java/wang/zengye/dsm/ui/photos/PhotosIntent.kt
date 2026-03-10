package wang.zengye.dsm.ui.photos

import android.net.Uri
import wang.zengye.dsm.ui.base.BaseIntent

sealed class PhotosIntent : BaseIntent {
    data class SetTab(val tab: Int) : PhotosIntent()
    data object Refresh : PhotosIntent()
    data object LoadPhotos : PhotosIntent()
    data object LoadAlbums : PhotosIntent()
    data object LoadPlaces : PhotosIntent()
    data class LoadPhotosByDate(val timestamp: Long, val dateStr: String) : PhotosIntent()
    data object ClearSelectedDate : PhotosIntent()
    
    // 上传相关
    data class UploadPhotos(val uris: List<Uri>, val folderId: Long = 0) : PhotosIntent()
    data class UploadProgress(val progress: Int, val current: Int, val total: Int) : PhotosIntent()
    data object CancelUpload : PhotosIntent()
    data class CreateAlbum(val name: String) : PhotosIntent()
}
