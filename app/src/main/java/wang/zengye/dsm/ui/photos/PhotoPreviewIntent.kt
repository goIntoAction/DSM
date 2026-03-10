package wang.zengye.dsm.ui.photos

import wang.zengye.dsm.data.model.PhotoItem
import wang.zengye.dsm.ui.base.BaseIntent

sealed class PhotoPreviewIntent : BaseIntent {
    data class LoadPhoto(val photoId: Long) : PhotoPreviewIntent()
    data class SetPhoto(val photo: PhotoItem) : PhotoPreviewIntent()
    data object ToggleInfo : PhotoPreviewIntent()
    data object ToggleMenu : PhotoPreviewIntent()
}
