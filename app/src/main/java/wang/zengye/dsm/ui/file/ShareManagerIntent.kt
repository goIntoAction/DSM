package wang.zengye.dsm.ui.file

import wang.zengye.dsm.ui.base.BaseIntent

sealed class ShareManagerIntent : BaseIntent {
    data object LoadShares : ShareManagerIntent()
    data class ShowDeleteDialog(val item: ShareLinkItem) : ShareManagerIntent()
    data object HideDialogs : ShareManagerIntent()
    data object DeleteShare : ShareManagerIntent()
}
