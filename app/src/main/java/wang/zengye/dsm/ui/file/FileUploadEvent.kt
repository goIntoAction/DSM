package wang.zengye.dsm.ui.file

import wang.zengye.dsm.ui.base.BaseEvent

sealed class FileUploadEvent : BaseEvent {
    data class ShowError(val message: String) : FileUploadEvent()
    data object UploadCompleted : FileUploadEvent()
}
