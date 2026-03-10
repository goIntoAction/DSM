package wang.zengye.dsm.ui.packages

import wang.zengye.dsm.ui.base.BaseEvent

sealed class PackagesEvent : BaseEvent {
    data class ShowError(val message: String) : PackagesEvent()
    data class ShowSuccess(val message: String) : PackagesEvent()
    data class InstallSuccess(val packageName: String) : PackagesEvent()
    data class UninstallSuccess(val packageId: String) : PackagesEvent()
}
