package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * 证书 Event
 */
sealed class CertificateEvent : BaseEvent {
    data class ShowError(val message: String) : CertificateEvent()
    data object DeleteSuccess : CertificateEvent()
    data object SetDefaultSuccess : CertificateEvent()
}
