package wang.zengye.dsm.ui.control_panel

import android.util.Log
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wang.zengye.dsm.data.repository.ControlPanelRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import javax.inject.Inject

data class Certificate(
    val id: String = "",
    val name: String = "",
    val issuer: String = "",
    val subject: String = "",
    val validFrom: Long = 0,
    val validTo: Long = 0,
    val isDefault: Boolean = false,
    val isExpired: Boolean = false,
    val services: List<String> = emptyList()
)

data class CertificateUiState(
    override val isLoading: Boolean = false,
    val certificates: List<Certificate> = emptyList(),
    override val error: String? = null
) : wang.zengye.dsm.ui.base.BaseState

@HiltViewModel
class CertificateViewModel @Inject constructor(
    private val controlPanelRepository: ControlPanelRepository
) : BaseViewModel<CertificateUiState, CertificateIntent, CertificateEvent>() {

    companion object {
        private const val TAG = "CertificateViewModel"
    }

    private val _state = MutableStateFlow(CertificateUiState())
    override val state: StateFlow<CertificateUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<CertificateEvent>(extraBufferCapacity = 1)
    override val events = _events.asSharedFlow()

    init {
        sendIntent(CertificateIntent.LoadCertificates)
    }

    override suspend fun processIntent(intent: CertificateIntent) {
        when (intent) {
            is CertificateIntent.LoadCertificates -> loadCertificates()
            is CertificateIntent.SetDefault -> setDefault(intent.certId)
            is CertificateIntent.DeleteCertificate -> deleteCertificate(intent.certId)
        }
    }

    private suspend fun loadCertificates() {
        _state.update { it.copy(isLoading = true, error = null) }

        controlPanelRepository.getCertificates()
            .onSuccess { response ->
                val certs = response.data?.certificates?.map { certData ->
                    val issuer = certData.issuer?.commonName ?: ""
                    val subject = certData.subject?.commonName ?: ""
                    val validTo = parseDateString(certData.validTill)
                    val validFrom = parseDateString(certData.validFrom)
                    val isExpired = validTo < System.currentTimeMillis() / 1000
                    val services = certData.services?.mapNotNull { service ->
                        service.displayName ?: service.displayNameI18n
                    } ?: emptyList()

                    Certificate(
                        id = certData.id ?: "",
                        name = certData.desc ?: "",
                        issuer = issuer,
                        subject = subject,
                        validFrom = validFrom,
                        validTo = validTo,
                        isDefault = certData.isDefault ?: false,
                        isExpired = isExpired,
                        services = services
                    )
                } ?: emptyList()

                _state.update {
                    it.copy(
                        certificates = certs.sortedByDescending { it.isDefault },
                        isLoading = false
                    )
                }
            }
            .onFailure { exception ->
                _state.update { it.copy(error = exception.message, isLoading = false) }
            }
    }

    private fun parseDateString(dateStr: String?): Long {
        if (dateStr == null) return 0
        return try {
            val normalizedStr = dateStr.replaceFirst("MMM  d".toRegex(), "MMM d")
            val format = java.text.SimpleDateFormat("MMM d HH:mm:ss yyyy zzz", java.util.Locale.ENGLISH)
            format.parse(normalizedStr)?.time?.div(1000) ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing date: $dateStr", e)
            0
        }
    }

    private suspend fun setDefault(certId: String) {
        controlPanelRepository.setCertificateDefault(certId)
        _events.emit(CertificateEvent.SetDefaultSuccess)
        loadCertificates()
    }

    private suspend fun deleteCertificate(certId: String) {
        controlPanelRepository.deleteCertificate(certId)
            .onSuccess {
                _events.emit(CertificateEvent.DeleteSuccess)
                loadCertificates()
            }
            .onFailure { _events.emit(CertificateEvent.ShowError(it.message ?: "删除失败")) }
    }
}