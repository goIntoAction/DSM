package wang.zengye.dsm.ui.setting

import wang.zengye.dsm.ui.base.BaseIntent
import wang.zengye.dsm.ui.theme.DarkMode

/**
 * 设置 Intent
 */
sealed class SettingIntent : BaseIntent {
    data object LoadSettings : SettingIntent()
    data class SetDarkMode(val mode: DarkMode) : SettingIntent()
    data class SetVibrateOn(val enabled: Boolean) : SettingIntent()
    data class SetDownloadWifiOnly(val enabled: Boolean) : SettingIntent()
    data class SetCheckSsl(val enabled: Boolean) : SettingIntent()
    data class SetLaunchAuth(val enabled: Boolean) : SettingIntent()
    data object Logout : SettingIntent()
}
