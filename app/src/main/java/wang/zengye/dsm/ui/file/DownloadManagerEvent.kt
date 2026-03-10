package wang.zengye.dsm.ui.file

import wang.zengye.dsm.ui.base.BaseEvent

sealed class DownloadManagerEvent : BaseEvent {
    data class ShowError(val message: String) : DownloadManagerEvent()
    data class ShowMessage(val message: String) : DownloadManagerEvent()
    data object NeedSetDownloadDirectory : DownloadManagerEvent()
    /** 用户拒绝了通知权限，提示可能的风险（下载可能被系统中断） */
    data object ShowNotificationPermissionDeniedWarning : DownloadManagerEvent()
    /** 权限检查通过，可以跳转到下载管理页面 */
    data object NavigateToDownloadManager : DownloadManagerEvent()
}
