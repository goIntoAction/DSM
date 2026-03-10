package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseIntent

sealed class StorageIntent : BaseIntent {
    data object LoadStorage : StorageIntent()
}
