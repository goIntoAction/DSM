package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseIntent

/**
 * 终端设置 Intent
 */
sealed class TerminalIntent : BaseIntent {
    data object LoadSettings : TerminalIntent()
    data class UpdateSettings(val setting: TerminalSetting) : TerminalIntent()
    data class DisconnectSession(val sessionId: String) : TerminalIntent()
    data object ShowEditDialog : TerminalIntent()
    data object HideEditDialog : TerminalIntent()
}
