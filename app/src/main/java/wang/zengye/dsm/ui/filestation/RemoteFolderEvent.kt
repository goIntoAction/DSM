package wang.zengye.dsm.ui.filestation

import wang.zengye.dsm.ui.base.BaseEvent

sealed class RemoteFolderEvent : BaseEvent {
    data class Error(val message: String) : RemoteFolderEvent()
    data class DisconnectSuccess(val folderName: String) : RemoteFolderEvent()
    data object AddSuccess : RemoteFolderEvent()
    data class UnmountSuccess(val folderName: String) : RemoteFolderEvent()
}
