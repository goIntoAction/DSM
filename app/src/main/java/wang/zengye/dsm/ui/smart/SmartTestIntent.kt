package wang.zengye.dsm.ui.smart

import wang.zengye.dsm.ui.base.BaseIntent

sealed class SmartTestIntent : BaseIntent {
    data object LoadData : SmartTestIntent()
    data object Refresh : SmartTestIntent()
    data class StartSmartTest(val diskId: String) : SmartTestIntent()
    data class SelectDiskById(val diskId: String) : SmartTestIntent()
}
