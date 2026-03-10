package wang.zengye.dsm.ui.update

import wang.zengye.dsm.ui.base.BaseIntent

sealed class UpdateIntent : BaseIntent {
    data object LoadCurrentVersion : UpdateIntent()
    data object CheckUpdate : UpdateIntent()
}
