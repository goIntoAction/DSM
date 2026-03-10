package wang.zengye.dsm.ui.photos

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wang.zengye.dsm.R
import wang.zengye.dsm.data.api.DsmApiHelper
import wang.zengye.dsm.data.model.AlbumItem
import wang.zengye.dsm.data.model.PhotoItem
import wang.zengye.dsm.data.model.GeocodingDto
import wang.zengye.dsm.data.model.TimelineItemRaw
import wang.zengye.dsm.data.model.TimelineDto
import wang.zengye.dsm.data.repository.PhotoRepository
import wang.zengye.dsm.ui.base.BaseViewModel
import wang.zengye.dsm.ui.base.BaseState
import wang.zengye.dsm.util.appString
import java.util.Calendar
import javax.inject.Inject

data class TimelineGroup(
    val date: String = "",
    val timestamp: Long = 0,
    val count: Int = 0
)

data class PlaceInfo(
    val id: Long = 0,
    val name: String = "",
    val count: Int = 0
)

data class UploadState(
    val isUploading: Boolean = false,
    val progress: Int = 0,
    val currentFile: Int = 0,
    val totalFiles: Int = 0,
    val fileName: String = ""
)

data class PhotosUiState(
    override val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val photos: List<PhotoItem> = emptyList(),
    val albums: List<AlbumItem> = emptyList(),
    val timelineGroups: List<TimelineGroup> = emptyList(),
    val places: List<PlaceInfo> = emptyList(),
    val currentTab: Int = 0,
    val selectedDate: String? = null,
    override val error: String? = null,
    val uploadState: UploadState = UploadState()
) : BaseState

@HiltViewModel
class PhotosViewModel @Inject constructor(
    private val photoRepository: PhotoRepository,
    @ApplicationContext private val context: Context
) : BaseViewModel<PhotosUiState, PhotosIntent, PhotosEvent>() {

    private val _state = MutableStateFlow(PhotosUiState())
    override val state: StateFlow<PhotosUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PhotosEvent>(
        extraBufferCapacity = 10
    )
    override val events = _events.asSharedFlow()

    private var uploadCancelled = false

    init {
        sendIntent(PhotosIntent.SetTab(0))
    }

    override suspend fun processIntent(intent: PhotosIntent) {
        when (intent) {
            is PhotosIntent.SetTab -> setTab(intent.tab)
            is PhotosIntent.Refresh -> refresh()
            is PhotosIntent.LoadPhotos -> loadPhotos()
            is PhotosIntent.LoadAlbums -> loadAlbums()
            is PhotosIntent.LoadPlaces -> loadPlaces()
            is PhotosIntent.LoadPhotosByDate -> loadPhotosByDate(intent.timestamp, intent.dateStr)
            is PhotosIntent.ClearSelectedDate -> clearSelectedDate()
            is PhotosIntent.UploadPhotos -> uploadPhotos(intent.uris, intent.folderId)
            is PhotosIntent.UploadProgress -> updateUploadProgress(intent.progress, intent.current, intent.total)
            is PhotosIntent.CancelUpload -> cancelUpload()
            is PhotosIntent.CreateAlbum -> createAlbum(intent.name)
        }
    }

    private suspend fun loadPhotos() {
        _state.update { it.copy(isLoading = true, error = null) }
        fetchPhotos()
    }

    private suspend fun refresh() {
        _state.update { it.copy(isRefreshing = true, error = null) }
        when (_state.value.currentTab) {
            0 -> fetchTimeline()
            1 -> fetchPhotos()
            2 -> fetchAlbums()
            3 -> fetchPlaces()
        }
    }

    private suspend fun setTab(tab: Int) {
        _state.update { it.copy(currentTab = tab) }
        when (tab) {
            0 -> if (_state.value.timelineGroups.isEmpty()) loadTimeline()
            1 -> if (_state.value.photos.isEmpty()) loadPhotos()
            2 -> if (_state.value.albums.isEmpty()) loadAlbums()
            3 -> if (_state.value.places.isEmpty()) loadPlaces()
        }
    }

    private suspend fun loadTimeline() {
        _state.update { it.copy(isLoading = true, error = null) }
        fetchTimeline()
    }

    /**
     * 按日期加载照片（点击时间线后调用）
     */
    private suspend fun loadPhotosByDate(timestamp: Long, dateStr: String) {
        _state.update { it.copy(isLoading = true, error = null, selectedDate = dateStr, currentTab = 1) }

        // 计算当天的开始和结束时间戳
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis / 1000

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endTime = calendar.timeInMillis / 1000

        photoRepository.getPhotos(startTime = startTime, endTime = endTime, limit = 500)
            .onSuccess { photos ->
                val sortedPhotos = photos.sortedByDescending { it.time }
                _state.update { it.copy(photos = sortedPhotos, isLoading = false, isRefreshing = false) }
            }
            .onFailure { error ->
                _state.update { it.copy(error = error.message, isLoading = false, isRefreshing = false) }
                _events.emit(PhotosEvent.ShowError(error.message ?: "加载失败"))
            }
    }

    /**
     * 清除日期选择，返回时间线
     */
    private fun clearSelectedDate() {
        _state.update { it.copy(selectedDate = null, currentTab = 0) }
    }

    private suspend fun loadPlaces() {
        _state.update { it.copy(isLoading = true, error = null) }
        fetchPlaces()
    }

    private suspend fun fetchPhotos() {
        photoRepository.getPhotos(limit = 500)
            .onSuccess { photos ->
                val sortedPhotos = photos.sortedByDescending { it.time }
                _state.update {
                    it.copy(
                        photos = sortedPhotos,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            }
            .onFailure { error ->
                _state.update { it.copy(error = error.message, isLoading = false, isRefreshing = false) }
                _events.emit(PhotosEvent.ShowError(error.message ?: "加载失败"))
            }
    }

    private suspend fun loadAlbums() {
        _state.update { it.copy(isLoading = true, error = null) }
        fetchAlbums()
    }

    private suspend fun fetchAlbums() {
        photoRepository.getAlbums()
            .onSuccess { albums ->
                val sortedAlbums = albums.sortedByDescending { it.createTime }
                _state.update {
                    it.copy(
                        albums = sortedAlbums,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            }
            .onFailure { error ->
                _state.update { it.copy(error = error.message, isLoading = false, isRefreshing = false) }
                _events.emit(PhotosEvent.ShowError(error.message ?: "加载失败"))
            }
    }

    private suspend fun fetchTimeline() {
        photoRepository.getTimeline(groupUnit = "day")
            .onSuccess { response ->
                val groups = parseTimeline(response)
                _state.update { it.copy(timelineGroups = groups, isLoading = false, isRefreshing = false) }
            }
            .onFailure { error ->
                _state.update { it.copy(error = error.message, isLoading = false, isRefreshing = false) }
                _events.emit(PhotosEvent.ShowError(error.message ?: "加载失败"))
            }
    }

    private fun parseTimeline(response: TimelineDto): List<TimelineGroup> {
        val groups = mutableListOf<TimelineGroup>()
        val data = response.data

        // 优先使用 section 格式
        data.section?.forEach { section ->
            section.list.forEach { item ->
                val timelineGroup = parseTimelineItem(item)
                if (timelineGroup != null) {
                    groups.add(timelineGroup)
                }
            }
        }

        // 兼容旧格式: data.list[]
        if (groups.isEmpty()) {
            data.list?.forEach { item ->
                val timelineGroup = parseTimelineItemLegacy(item)
                if (timelineGroup != null) {
                    groups.add(timelineGroup)
                }
            }
        }

        return groups.sortedByDescending { it.timestamp }
    }

    private fun parseTimelineItem(item: TimelineItemRaw): TimelineGroup? {
        val day = item.day
        val month = item.month
        val year = item.year
        val count = item.itemCount

        if (count > 0 && day > 0 && month > 0 && year > 0) {
            // 创建日期字符串
            val dateStr = "${year}年${month}月${day}日"
            // 创建时间戳用于排序 (年月日转为时间戳)
            val calendar = Calendar.getInstance()
            calendar.set(year, month - 1, day) // month 是 0-based
            val timestamp = calendar.timeInMillis

            return TimelineGroup(date = dateStr, timestamp = timestamp, count = count)
        }
        return null
    }

    private fun parseTimelineItemLegacy(item: TimelineItemRaw): TimelineGroup? {
        val day = item.date
        val timestamp = item.startTime
        val count = item.count

        if (count > 0 && day.isNotEmpty()) {
            return TimelineGroup(date = day, timestamp = timestamp, count = count)
        }
        return null
    }

    private suspend fun fetchPlaces() {
        photoRepository.getGeocoding()
            .onSuccess { response ->
                val places = parsePlaces(response)
                _state.update { it.copy(places = places, isLoading = false, isRefreshing = false) }
            }
            .onFailure { error ->
                _state.update { it.copy(error = error.message, isLoading = false, isRefreshing = false) }
                _events.emit(PhotosEvent.ShowError(error.message ?: "加载失败"))
            }
    }

    private fun parsePlaces(response: GeocodingDto): List<PlaceInfo> {
        return response.data.list.map { item ->
            PlaceInfo(
                id = item.id,
                name = item.name.ifEmpty { item.placeName },
                count = if (item.itemCount > 0) item.itemCount else item.count
            )
        }.sortedByDescending { it.count }
    }

    // ==================== 上传相关 ====================

    private fun updateUploadProgress(progress: Int, current: Int, total: Int) {
        _state.update { it.copy(uploadState = it.uploadState.copy(progress = progress, currentFile = current, totalFiles = total)) }
    }

    private fun cancelUpload() {
        uploadCancelled = true
        _state.update { it.copy(uploadState = UploadState()) }
    }

    private suspend fun uploadPhotos(uris: List<Uri>, folderId: Long) {
        if (uris.isEmpty()) return

        uploadCancelled = false
        _state.update { it.copy(uploadState = UploadState(isUploading = true, totalFiles = uris.size, currentFile = 0, progress = 0)) }
        
        // 获取文件名列表
        val fileNames = uris.map { uri ->
            getFileNameFromUri(context, uri) ?: "photo_${System.currentTimeMillis()}.jpg"
        }
        
        // 使用 UploadService 进行后台保活上传
        wang.zengye.dsm.service.UploadService.startPhotoUploadMultiple(context, uris, fileNames, folderId)
        
        // 监听上传进度
        var lastCompletedCount = 0
        val monitoringJob = viewModelScope.launch {
            wang.zengye.dsm.service.UploadService.tasks.collect { tasks ->
                if (uploadCancelled) return@collect
                
                val taskList = tasks.values.toList()
                val activeTask = taskList.find { it.isUploading }
                val completedCount = taskList.count { it.isCompleted }
                val failedCount = taskList.count { it.isFailed }
                
                if (activeTask != null) {
                    _state.update { 
                        it.copy(
                            uploadState = it.uploadState.copy(
                                progress = activeTask.progress,
                                currentFile = taskList.indexOf(activeTask) + 1,
                                fileName = activeTask.fileName
                            )
                        )
                    }
                }
                
                // 检查是否所有任务完成
                if (taskList.size >= uris.size) {
                    val allDone = taskList.all { it.isCompleted || it.isFailed || it.status == wang.zengye.dsm.service.UploadStatus.CANCELLED }
                    if (allDone) {
                        val successCount = taskList.count { it.isCompleted }
                        val failCount = taskList.count { it.isFailed }
                        
                        _state.update { it.copy(uploadState = UploadState()) }
                        _events.emit(PhotosEvent.UploadComplete(successCount, failCount))
                        
                        // 刷新照片列表
                        if (successCount > 0) {
                            sendIntent(PhotosIntent.Refresh)
                        }
                    }
                }
            }
        }
        
        // 等待上传完成或取消
        try {
            while (true) {
                delay(500)
                val tasks = wang.zengye.dsm.service.UploadService.tasks.value.values.toList()
                if (tasks.size >= uris.size) {
                    val allDone = tasks.all { it.isCompleted || it.isFailed || it.status == wang.zengye.dsm.service.UploadStatus.CANCELLED }
                    if (allDone) break
                }
                if (uploadCancelled) break
            }
        } finally {
            monitoringJob.cancel()
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name ?: uri.path?.substringAfterLast('/')
    }

    private suspend fun createAlbum(name: String) {
        photoRepository.createAlbum(name)
            .onSuccess { response ->
                val albumId = response.data?.id ?: 0
                _events.emit(PhotosEvent.AlbumCreated(albumId, name))
                // 刷新相册列表
                fetchAlbums()
            }
            .onFailure { error ->
                _events.emit(PhotosEvent.ShowError(error.message ?: appString(R.string.photos_create_album_failed)))
            }
    }
}
