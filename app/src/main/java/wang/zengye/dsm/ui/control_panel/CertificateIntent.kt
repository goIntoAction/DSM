package wang.zengye.dsm.ui.control_panel

import wang.zengye.dsm.ui.base.BaseIntent

/**
 * 证书 Intent
 */
sealed class CertificateIntent : BaseIntent {
    data object LoadCertificates : CertificateIntent()
    data class SetDefault(val certId: String) : CertificateIntent()
    data class DeleteCertificate(val certId: String) : CertificateIntent()
}
