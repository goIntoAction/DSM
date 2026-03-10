package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseIntent

/**
 * DDNS Intent
 */
sealed class DdnsIntent : BaseIntent {
    data object LoadRecords : DdnsIntent()
    data object ShowAddDialog : DdnsIntent()
    data object HideAddDialog : DdnsIntent()
    data class AddRecord(
        val provider: String,
        val hostname: String,
        val username: String,
        val password: String
    ) : DdnsIntent()
    data class DeleteRecord(val id: Int) : DdnsIntent()
    data class ShowEditDialog(val record: DdnsRecord) : DdnsIntent()
    data object HideEditDialog : DdnsIntent()
    data class UpdateRecord(
        val id: Int,
        val provider: String,
        val hostname: String,
        val username: String,
        val password: String?
    ) : DdnsIntent()
}
