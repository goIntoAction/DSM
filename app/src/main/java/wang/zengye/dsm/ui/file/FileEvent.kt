package wang.zengye.dsm.ui.file

import wang.zengye.dsm.ui.base.BaseEvent

sealed class FileEvent : BaseEvent {
    data class ShowError(val message: String) : FileEvent()
    data class ShowSuccess(val message: String) : FileEvent()
    data class OperationSuccess(val operationType: String) : FileEvent()
    data class ShareLinkCreated(val url: String) : FileEvent()

    // 导航事件
    data class NavigateToImageViewer(val path: String) : FileEvent()
    data class NavigateToVideoPlayer(val url: String, val name: String) : FileEvent()
    data class NavigateToAudioPlayer(val url: String, val name: String) : FileEvent()
    data class NavigateToPdfViewer(val url: String, val name: String) : FileEvent()
    data class NavigateToTextEditor(val url: String, val name: String) : FileEvent()
    data class ShowDownloadDialog(val path: String, val name: String) : FileEvent()
}
