package wang.zengye.dsm.ui.download

import wang.zengye.dsm.ui.base.BaseIntent

sealed class PeerListIntent : BaseIntent {
    data class SetTask(val taskId: String, val taskName: String) : PeerListIntent()
    data object LoadPeers : PeerListIntent()
    data class SetSortType(val sortType: String) : PeerListIntent()
    data object ToggleAutoRefresh : PeerListIntent()
}
