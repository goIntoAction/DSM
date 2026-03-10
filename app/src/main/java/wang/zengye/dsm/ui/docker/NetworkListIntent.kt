package wang.zengye.dsm.ui.docker

import wang.zengye.dsm.ui.base.BaseIntent

sealed class NetworkListIntent : BaseIntent {
    data object LoadNetworks : NetworkListIntent()
    data object ShowCreateDialog : NetworkListIntent()
    data object HideCreateDialog : NetworkListIntent()
    data class CreateNetwork(
        val name: String,
        val driver: String = "bridge",
        val subnet: String = "",
        val gateway: String = ""
    ) : NetworkListIntent()
    data class DeleteNetwork(val networkId: String) : NetworkListIntent()
}
