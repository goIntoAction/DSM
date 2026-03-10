package wang.zengye.dsm.ui.iscsi

import wang.zengye.dsm.ui.base.BaseIntent

/**
 * iSCSI 管理 Intent
 */
sealed class IscsiIntent : BaseIntent {
    data object LoadData : IscsiIntent()
    data class SetTab(val tab: Int) : IscsiIntent()

    // LUN 操作
    data class CreateLun(
        val name: String,
        val location: String,
        val size: Long,
        val thinProvision: Boolean
    ) : IscsiIntent()
    data class DeleteLun(val lunId: Int) : IscsiIntent()

    // Target 操作
    data class CreateTarget(
        val name: String,
        val iqn: String?,
        val mappedLunIds: List<Int>
    ) : IscsiIntent()
    data class DeleteTarget(val targetId: Int) : IscsiIntent()
    data class SetTargetEnabled(val targetId: Int, val enabled: Boolean) : IscsiIntent()
    data class MapLunToTarget(val targetId: Int, val lunId: Int) : IscsiIntent()
    data class UnmapLunFromTarget(val targetId: Int, val lunId: Int) : IscsiIntent()

    // UI 状态
    data class ShowCreateLunDialog(val show: Boolean) : IscsiIntent()
    data class ShowCreateTargetDialog(val show: Boolean) : IscsiIntent()
    data class ShowDeleteConfirmDialog(val type: String, val id: Int, val show: Boolean) : IscsiIntent()
}