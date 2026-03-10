package wang.zengye.dsm.ui.setting

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wang.zengye.dsm.ui.base.BaseViewModel
import wang.zengye.dsm.ui.theme.DarkMode
import wang.zengye.dsm.util.SettingsManager
import javax.inject.Inject

data class SettingUiState(
    override val isLoading: Boolean = false,
    val vibrateOn: Boolean = true,
    val downloadWifiOnly: Boolean = true,
    val checkSsl: Boolean = true,
    val launchAuth: Boolean = false,
    val appVersion: String = "1.0.0",
    override val error: String? = null
) : wang.zengye.dsm.ui.base.BaseState

@HiltViewModel
class SettingViewModel @Inject constructor() : BaseViewModel<SettingUiState, SettingIntent, SettingEvent>() {
    
    private val _state = MutableStateFlow(SettingUiState())
    override val state: StateFlow<SettingUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SettingEvent>(extraBufferCapacity = 1)
    override val events = _events.asSharedFlow()
    
    init {
        sendIntent(SettingIntent.LoadSettings)
    }
    
    override suspend fun processIntent(intent: SettingIntent) {
        when (intent) {
            is SettingIntent.LoadSettings -> loadSettings()
            is SettingIntent.SetDarkMode -> setDarkMode(intent.mode)
            is SettingIntent.SetVibrateOn -> setVibrateOn(intent.enabled)
            is SettingIntent.SetDownloadWifiOnly -> setDownloadWifiOnly(intent.enabled)
            is SettingIntent.SetCheckSsl -> setCheckSsl(intent.enabled)
            is SettingIntent.SetLaunchAuth -> setLaunchAuth(intent.enabled)
            is SettingIntent.Logout -> logout()
        }
    }
    
    private fun loadSettings() {
        // 并行collect多个Flow，使用viewModelScope是正确的做法
        viewModelScope.launch {
            SettingsManager.vibrateOn.collect { vibrateOn ->
                _state.update { it.copy(vibrateOn = vibrateOn) }
            }
        }

        viewModelScope.launch {
            SettingsManager.downloadWifiOnly.collect { downloadWifiOnly ->
                _state.update { it.copy(downloadWifiOnly = downloadWifiOnly) }
            }
        }

        viewModelScope.launch {
            SettingsManager.checkSsl.collect { checkSsl ->
                _state.update { it.copy(checkSsl = checkSsl) }
            }
        }

        viewModelScope.launch {
            SettingsManager.launchAuth.collect { launchAuth ->
                _state.update { it.copy(launchAuth = launchAuth) }
            }
        }
    }
    
    private suspend fun setDarkMode(mode: DarkMode) {
        SettingsManager.setDarkMode(mode)
    }
    
    private suspend fun setVibrateOn(enabled: Boolean) {
        SettingsManager.setVibrateOn(enabled)
    }
    
    private suspend fun setDownloadWifiOnly(enabled: Boolean) {
        SettingsManager.setDownloadWifiOnly(enabled)
    }
    
    private suspend fun setCheckSsl(enabled: Boolean) {
        SettingsManager.setCheckSsl(enabled)
    }
    
    private suspend fun setLaunchAuth(enabled: Boolean) {
        SettingsManager.setLaunchAuth(enabled)
    }
    
    private suspend fun logout() {
        SettingsManager.clearSession()
        _events.emit(SettingEvent.LogoutSuccess)
    }
}