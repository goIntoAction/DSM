package wang.zengye.dsm.ui.storage

import wang.zengye.dsm.ui.base.BaseIntent

sealed class SmartTestIntent : BaseIntent {
    data object LoadSmartInfo : SmartTestIntent()
    data class SelectDisk(val disk: wang.zengye.dsm.ui.storage.SmartDiskInfo?) : SmartTestIntent()
    data object ShowTestDialog : SmartTestIntent()
    data object HideTestDialog : SmartTestIntent()
    data class StartTest(val device: String, val testType: String) : SmartTestIntent()
}
