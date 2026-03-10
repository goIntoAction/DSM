package wang.zengye.dsm.ui.download

import wang.zengye.dsm.ui.base.BaseEvent

sealed class DownloadEvent : BaseEvent {
    data class Error(val message: String) : DownloadEvent()
    data class OperationSuccess(val message: String) : DownloadEvent()
}
