package wang.zengye.dsm.ui.file

import wang.zengye.dsm.data.model.PhotoItem

/**
 * 媒体文件项（用于视频/音频播放器）
 */
data class FileMediaItem(
    val path: String,
    val name: String
)

/**
 * 图片文件项（用于图片查看器）
 */
data class FileImageItem(
    val path: String,
    val name: String
)

/**
 * 媒体列表管理器
 * 用于在文件浏览和播放器之间共享媒体列表
 */
object MediaListManager {
    private var _videoList: List<FileMediaItem> = emptyList()
    private var _audioList: List<FileMediaItem> = emptyList()
    private var _imageList: List<FileImageItem> = emptyList()
    private var _photoList: List<PhotoItem> = emptyList()
    
    val videoList: List<FileMediaItem> get() = _videoList
    val audioList: List<FileMediaItem> get() = _audioList
    val imageList: List<FileImageItem> get() = _imageList
    val photoList: List<PhotoItem> get() = _photoList
    
    fun setVideoList(list: List<FileMediaItem>) {
        _videoList = list
    }
    
    fun setAudioList(list: List<FileMediaItem>) {
        _audioList = list
    }
    
    fun setImageList(list: List<FileImageItem>) {
        _imageList = list
    }
    
    fun setPhotoList(list: List<PhotoItem>) {
        _photoList = list
    }
    
    fun clear() {
        _videoList = emptyList()
        _audioList = emptyList()
        _imageList = emptyList()
        _photoList = emptyList()
    }
}
