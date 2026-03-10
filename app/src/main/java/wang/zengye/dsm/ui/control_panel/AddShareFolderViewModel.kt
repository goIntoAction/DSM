package wang.zengye.dsm.ui.control_panel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wang.zengye.dsm.R
import wang.zengye.dsm.data.repository.ControlPanelRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import wang.zengye.dsm.util.appString
import javax.inject.Inject
import kotlin.math.pow

data class AddShareFolderUiState(
    override val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    override val error: String? = null,

    // 存储卷列表
    val volumes: List<VolumeInfo> = emptyList(),
    val selectedVolumeIndex: Int = 0,

    // 基本信息
    val name: String = "",
    val description: String = "",
    val oldName: String? = null, // 编辑时的原名称
    val isEdit: Boolean = false,

    // 选项
    val hidden: Boolean = false,
    val hideUnreadable: Boolean = false,
    val enableRecycleBin: Boolean = false,
    val recycleBinAdminOnly: Boolean = false,
    val encryption: Boolean = false,
    val password: String = "",
    val confirmPassword: String = "",
    val enableShareCow: Boolean = false,
    val enableShareCompress: Boolean = false,
    val enableShareQuota: Boolean = false,
    val shareQuota: String = "",
    val shareQuotaUsed: Long? = null,
    val quotaUnitIndex: Int = 1 // 0=TB, 1=GB, 2=MB
) : wang.zengye.dsm.ui.base.BaseState

data class VolumeInfo(
    val volumeId: Int = 0,
    val volumePath: String = "",
    val displayName: String = "",
    val fsType: String = "",
    val sizeFreeByte: Long = 0,
    val description: String = ""
)

// 迁移状态：[已完成]
// 说明：已完全迁移到 Moshi API
@HiltViewModel
class AddShareFolderViewModel @Inject constructor(
    private val controlPanelRepository: ControlPanelRepository
) : BaseViewModel<AddShareFolderUiState, AddShareFolderIntent, AddShareFolderEvent>() {

    companion object {
        val QUOTA_UNITS = listOf("TB", "GB", "MB")
    }

    private val _state = MutableStateFlow(AddShareFolderUiState())
    override val state: StateFlow<AddShareFolderUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AddShareFolderEvent>(extraBufferCapacity = 1)
    override val events = _events.asSharedFlow()

    override suspend fun processIntent(intent: AddShareFolderIntent) {
        when (intent) {
            is AddShareFolderIntent.LoadVolumes -> loadVolumes()
            is AddShareFolderIntent.LoadShareDetail -> loadShareDetail(intent.name)
            is AddShareFolderIntent.UpdateName -> updateName(intent.value)
            is AddShareFolderIntent.UpdateDescription -> updateDescription(intent.value)
            is AddShareFolderIntent.SelectVolume -> selectVolume(intent.index)
            is AddShareFolderIntent.ToggleHidden -> toggleHidden()
            is AddShareFolderIntent.ToggleHideUnreadable -> toggleHideUnreadable()
            is AddShareFolderIntent.ToggleRecycleBin -> toggleRecycleBin()
            is AddShareFolderIntent.ToggleRecycleBinAdminOnly -> toggleRecycleBinAdminOnly()
            is AddShareFolderIntent.ToggleEncryption -> toggleEncryption()
            is AddShareFolderIntent.UpdatePassword -> updatePassword(intent.value)
            is AddShareFolderIntent.UpdateConfirmPassword -> updateConfirmPassword(intent.value)
            is AddShareFolderIntent.ToggleShareCow -> toggleShareCow()
            is AddShareFolderIntent.ToggleShareCompress -> toggleShareCompress()
            is AddShareFolderIntent.ToggleShareQuota -> toggleShareQuota()
            is AddShareFolderIntent.UpdateShareQuota -> updateShareQuota(intent.value)
            is AddShareFolderIntent.UpdateQuotaUnit -> updateQuotaUnit(intent.index)
            is AddShareFolderIntent.Save -> validateAndSave()
        }
    }

    private suspend fun loadVolumes() {
        _state.update { it.copy(isLoading = true, error = null) }

        controlPanelRepository.getVolumes()
            .onSuccess { response ->
                val volumes = response.data?.volumes?.map { volume ->
                    VolumeInfo(
                        volumeId = volume.volumeId ?: 0,
                        volumePath = volume.volumePath ?: "",
                        displayName = volume.displayName ?: "",
                        fsType = volume.fsType ?: "",
                        sizeFreeByte = volume.sizeFreeByte ?: 0,
                        description = volume.description ?: ""
                    )
                } ?: emptyList()

                _state.update {
                    it.copy(
                        volumes = volumes,
                        isLoading = false
                    )
                }
            }
            .onFailure { exception ->
                _state.update {
                    it.copy(error = exception.message, isLoading = false)
                }
            }
    }

    private suspend fun loadShareDetail(name: String) {
        _state.update { it.copy(isLoading = true, error = null, isEdit = true, oldName = name) }

        controlPanelRepository.getShareDetail(name)
            .onSuccess { response ->
                val share = response.data?.shareInfo

                if (share != null) {
                    val quotaValue = share.quotaValue ?: 0
                    val (quotaStr, unitIndex) = when {
                        quotaValue <= 0 -> "" to 1
                        quotaValue < 1024 -> quotaValue.toString() to 2
                        quotaValue < 1024 * 1024 -> (quotaValue / 1024).toString() to 1
                        else -> (quotaValue / (1024 * 1024)).toString() to 0
                    }

                    // 查找对应的存储卷
                    val volPath = share.volPath ?: ""
                    val volumeIndex = _state.value.volumes.indexOfFirst { it.volumePath == volPath }

                    _state.update { state ->
                        state.copy(
                            name = share.name ?: "",
                            description = share.desc ?: "",
                            hidden = share.hidden ?: false,
                            hideUnreadable = share.hideUnreadable ?: false,
                            enableRecycleBin = share.enableRecycleBin ?: false,
                            recycleBinAdminOnly = share.recycleBinAdminOnly ?: false,
                            encryption = share.encryption == 1,
                            enableShareCow = share.enableShareCow ?: false,
                            enableShareCompress = share.enableShareCompress ?: false,
                            enableShareQuota = quotaValue > 0,
                            shareQuota = quotaStr,
                            shareQuotaUsed = share.shareQuotaUsed,
                            quotaUnitIndex = unitIndex,
                            selectedVolumeIndex = if (volumeIndex >= 0) volumeIndex else state.selectedVolumeIndex,
                            isLoading = false
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false) }
                }
            }
            .onFailure { exception ->
                _state.update {
                    it.copy(error = exception.message, isLoading = false)
                }
            }
    }

    private fun updateName(value: String) {
        _state.update { it.copy(name = value) }
    }

    private fun updateDescription(value: String) {
        _state.update { it.copy(description = value) }
    }

    private fun selectVolume(index: Int) {
        _state.update { it.copy(selectedVolumeIndex = index) }
    }

    private fun toggleHidden() {
        _state.update { it.copy(hidden = !it.hidden) }
    }

    private fun toggleHideUnreadable() {
        _state.update { it.copy(hideUnreadable = !it.hideUnreadable) }
    }

    private fun toggleRecycleBin() {
        _state.update { it.copy(enableRecycleBin = !it.enableRecycleBin) }
    }

    private fun toggleRecycleBinAdminOnly() {
        _state.update { it.copy(recycleBinAdminOnly = !it.recycleBinAdminOnly) }
    }

    private fun toggleEncryption() {
        _state.update { it.copy(encryption = !it.encryption) }
    }

    private fun updatePassword(value: String) {
        _state.update { it.copy(password = value) }
    }

    private fun updateConfirmPassword(value: String) {
        _state.update { it.copy(confirmPassword = value) }
    }

    private fun toggleShareCow() {
        val currentState = _state.value
        if (currentState.volumes.getOrNull(currentState.selectedVolumeIndex)?.fsType == "btrfs") {
            _state.update { it.copy(enableShareCow = !it.enableShareCow) }
        }
    }

    private fun toggleShareCompress() {
        val currentState = _state.value
        if (currentState.volumes.getOrNull(currentState.selectedVolumeIndex)?.fsType == "btrfs") {
            _state.update { it.copy(enableShareCompress = !it.enableShareCompress) }
        }
    }

    private fun toggleShareQuota() {
        val currentState = _state.value
        if (currentState.volumes.getOrNull(currentState.selectedVolumeIndex)?.fsType == "btrfs") {
            _state.update { it.copy(enableShareQuota = !it.enableShareQuota) }
        }
    }

    private fun updateShareQuota(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _state.update { it.copy(shareQuota = value) }
        }
    }

    private fun updateQuotaUnit(index: Int) {
        _state.update { it.copy(quotaUnitIndex = index) }
    }

    private suspend fun validateAndSave() {
        val currentState = _state.value

        // 验证
        if (currentState.name.isBlank()) {
            _events.emit(AddShareFolderEvent.ShowError(appString(R.string.share_name_empty)))
            return
        }

        if (currentState.encryption) {
            if (currentState.password.length < 8) {
                _events.emit(AddShareFolderEvent.ShowError(appString(R.string.share_key_min_length)))
                return
            }
            if (currentState.password != currentState.confirmPassword) {
                _events.emit(AddShareFolderEvent.ShowError(appString(R.string.share_key_mismatch)))
                return
            }
        }

        if (currentState.enableShareQuota && currentState.shareQuota.isBlank()) {
            _events.emit(AddShareFolderEvent.ShowError(appString(R.string.share_quota_empty)))
            return
        }

        // 计算配额值 (单位: MB)
        val quotaValue = if (currentState.enableShareQuota) {
            val quotaNum = currentState.shareQuota.toLongOrNull() ?: 0
            // 转换为 MB: TB=1024*1024, GB=1024, MB=1
            val multiplier = 1024.0.pow(2 - currentState.quotaUnitIndex).toLong()
            quotaNum * multiplier
        } else {
            0L
        }

        val volume = currentState.volumes.getOrNull(currentState.selectedVolumeIndex)

        _state.update { it.copy(isSaving = true) }

        controlPanelRepository.createShare(
            name = currentState.name,
            volPath = volume?.volumePath ?: "/volume1",
            description = currentState.description,
            hidden = currentState.hidden,
            hideUnreadable = currentState.hideUnreadable,
            enableRecycleBin = currentState.enableRecycleBin,
            recycleBinAdminOnly = currentState.recycleBinAdminOnly,
            encryption = currentState.encryption,
            password = currentState.password,
            enableShareCow = currentState.enableShareCow,
            enableShareCompress = currentState.enableShareCompress,
            shareQuota = quotaValue,
            oldName = currentState.oldName
        )
            .onSuccess {
                _events.emit(AddShareFolderEvent.SaveSuccess)
            }
            .onFailure { exception ->
                _events.emit(AddShareFolderEvent.ShowError(exception.message ?: "Unknown error"))
            }

        _state.update { it.copy(isSaving = false) }
    }
}
