package wang.zengye.dsm.ui.docker

import wang.zengye.dsm.ui.base.BaseIntent

sealed class ImageListIntent : BaseIntent {
    data object LoadImages : ImageListIntent()
    data class ShowDeleteDialog(val image: DockerImageItem) : ImageListIntent()
    data object HideDialog : ImageListIntent()
    data object DeleteImage : ImageListIntent()
}
