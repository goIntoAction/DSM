package wang.zengye.dsm.ui.terminal

sealed class TerminalIntent : wang.zengye.dsm.ui.base.BaseIntent {
    data object ShowConnectionDialog : TerminalIntent()
    data object HideConnectionDialog : TerminalIntent()
    data object ShowDisconnectDialog : TerminalIntent()
    data object HideDisconnectDialog : TerminalIntent()
    data class Connect(
        val host: String,
        val port: Int,
        val username: String,
        val password: String
    ) : TerminalIntent()
    data object Disconnect : TerminalIntent()
    data class Resize(val cols: Int, val rows: Int) : TerminalIntent()
}
