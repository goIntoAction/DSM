package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseIntent

/**
 * 外部设备 Intent
 */
sealed class ExternalDevicesIntent : BaseIntent {
    data object LoadDevices : ExternalDevicesIntent()
    data class EjectDevice(val deviceId: String) : ExternalDevicesIntent()
    data class FormatDevice(val deviceId: String) : ExternalDevicesIntent()
}
