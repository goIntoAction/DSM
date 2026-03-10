package wang.zengye.dsm.ui.filestation

import wang.zengye.dsm.ui.base.BaseIntent

sealed class RemoteFolderIntent : BaseIntent {
    data object LoadRemoteFolders : RemoteFolderIntent()
    data class SetTab(val tab: Int) : RemoteFolderIntent()
    data class ShowDisconnectDialog(val folder: RemoteFolder) : RemoteFolderIntent()
    data object HideDisconnectDialog : RemoteFolderIntent()
    data class Disconnect(val folder: RemoteFolder) : RemoteFolderIntent()
    data object ShowAddDialog : RemoteFolderIntent()
    data object HideAddDialog : RemoteFolderIntent()
    data class SetNewFolderServer(val server: String) : RemoteFolderIntent()
    data class SetNewFolderPath(val path: String) : RemoteFolderIntent()
    data class SetNewFolderUsername(val username: String) : RemoteFolderIntent()
    data class SetNewFolderPassword(val password: String) : RemoteFolderIntent()
    data object AddRemoteFolder : RemoteFolderIntent()
    data class UnmountFolder(val folder: RemoteFolder) : RemoteFolderIntent()
}
