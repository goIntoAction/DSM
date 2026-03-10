package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseIntent

/**
 * 防火墙 Intent
 */
sealed class FirewallIntent : BaseIntent {
    data object LoadFirewall : FirewallIntent()
    data class ToggleFirewall(val enabled: Boolean) : FirewallIntent()
    data class ToggleRule(val ruleId: Int, val enabled: Boolean) : FirewallIntent()
    data class DeleteRule(val ruleId: Int) : FirewallIntent()
    data object ShowAddDialog : FirewallIntent()
    data object HideAddDialog : FirewallIntent()
}
