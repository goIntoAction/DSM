package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseIntent

/**
 * 网络 Intent
 */
sealed class NetworkIntent : BaseIntent {
    data object LoadNetwork : NetworkIntent()
}
