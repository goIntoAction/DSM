package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseIntent

/**
 * 电源管理 Intent
 */
sealed class PowerIntent : BaseIntent {
    data object LoadPowerSettings : PowerIntent()
    data class SetAutoPowerOn(val enabled: Boolean) : PowerIntent()
    data class SetBeepOnAlert(val enabled: Boolean) : PowerIntent()
    data object Shutdown : PowerIntent()
    data object Reboot : PowerIntent()
}
