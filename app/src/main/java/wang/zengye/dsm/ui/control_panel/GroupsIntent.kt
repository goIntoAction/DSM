package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseIntent

/**
 * 群组管理 Intent
 */
sealed class GroupsIntent : BaseIntent {
    data object LoadGroups : GroupsIntent()
    data class DeleteGroup(val name: String) : GroupsIntent()
    data object ShowAddDialog : GroupsIntent()
    data object HideAddDialog : GroupsIntent()
}
