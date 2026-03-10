package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseIntent

/**
 * 安全扫描 Intent
 */
sealed class SecurityScanIntent : BaseIntent {
    data object LoadScan : SecurityScanIntent()
}
