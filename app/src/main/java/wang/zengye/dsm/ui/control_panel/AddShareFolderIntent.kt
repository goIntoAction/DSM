package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseIntent

/**
 * 添加/编辑共享文件夹 Intent
 */
sealed class AddShareFolderIntent : BaseIntent {
    data object LoadVolumes : AddShareFolderIntent()
    data class LoadShareDetail(val name: String) : AddShareFolderIntent()
    data class UpdateName(val value: String) : AddShareFolderIntent()
    data class UpdateDescription(val value: String) : AddShareFolderIntent()
    data class SelectVolume(val index: Int) : AddShareFolderIntent()
    data object ToggleHidden : AddShareFolderIntent()
    data object ToggleHideUnreadable : AddShareFolderIntent()
    data object ToggleRecycleBin : AddShareFolderIntent()
    data object ToggleRecycleBinAdminOnly : AddShareFolderIntent()
    data object ToggleEncryption : AddShareFolderIntent()
    data class UpdatePassword(val value: String) : AddShareFolderIntent()
    data class UpdateConfirmPassword(val value: String) : AddShareFolderIntent()
    data object ToggleShareCow : AddShareFolderIntent()
    data object ToggleShareCompress : AddShareFolderIntent()
    data object ToggleShareQuota : AddShareFolderIntent()
    data class UpdateShareQuota(val value: String) : AddShareFolderIntent()
    data class UpdateQuotaUnit(val index: Int) : AddShareFolderIntent()
    data object Save : AddShareFolderIntent()
}
