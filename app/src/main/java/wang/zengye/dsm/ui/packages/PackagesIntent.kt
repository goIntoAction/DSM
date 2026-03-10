package wang.zengye.dsm.ui.packages

import wang.zengye.dsm.ui.base.BaseIntent

sealed class PackagesIntent : BaseIntent {
    data class SelectTab(val index: Int) : PackagesIntent()
    data object Refresh : PackagesIntent()
    data class SetFilter(val filter: String) : PackagesIntent()
    data class StartPackage(val packageId: String) : PackagesIntent()
    data class StopPackage(val packageId: String) : PackagesIntent()
    data class InstallPackage(val packageName: String) : PackagesIntent()
    data class UninstallPackage(val packageId: String, val removeData: Boolean = false) : PackagesIntent()
    data object ClearOperationMessage : PackagesIntent()
}
