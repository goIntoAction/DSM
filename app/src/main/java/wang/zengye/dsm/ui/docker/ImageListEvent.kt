package wang.zengye.dsm.ui.docker

import wang.zengye.dsm.ui.base.BaseEvent

sealed class ImageListEvent : BaseEvent {
    data class Error(val message: String) : ImageListEvent()
    data class DeleteSuccess(val imageName: String) : ImageListEvent()
}
