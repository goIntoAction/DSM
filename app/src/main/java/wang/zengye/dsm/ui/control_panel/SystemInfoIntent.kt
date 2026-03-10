package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseIntent

/**
 * 系统信息 Intent
 */
sealed class SystemInfoIntent : BaseIntent {
    data object LoadSystemInfo : SystemInfoIntent()
}
