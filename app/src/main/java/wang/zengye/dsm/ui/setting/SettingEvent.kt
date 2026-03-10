package wang.zengye.dsm.ui.setting

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * 设置 Event
 */
sealed class SettingEvent : BaseEvent {
    data object LogoutSuccess : SettingEvent()
}
