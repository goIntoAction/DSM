package wang.zengye.dsm.ui.player

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import `is`.xyz.mpv.BaseMPVView
import `is`.xyz.mpv.MPVLib

/**
 * 内部 MPV 视图实现 - 继承 BaseMPVView
 * 通过 XML 布局加载，确保 AttributeSet 正确
 */
class InnerMPVView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : BaseMPVView(context, attrs ?: EmptyAttributeSet) {

    companion object {
        private const val TAG = "InnerMPVView"
        
        // 空的 AttributeSet 实现
        private object EmptyAttributeSet : AttributeSet {
            override fun getAttributeCount(): Int = 0
            override fun getAttributeName(index: Int): String? = null
            override fun getAttributeValue(index: Int): String? = null
            override fun getAttributeValue(namespace: String?, name: String?): String? = null
            override fun getAttributeNameResource(index: Int): Int = 0
            override fun getAttributeListValue(index: Int, options: Array<out String>?, defaultValue: Int): Int = defaultValue
            override fun getAttributeBooleanValue(index: Int, defaultValue: Boolean): Boolean = defaultValue
            override fun getAttributeResourceValue(index: Int, defaultValue: Int): Int = defaultValue
            override fun getAttributeIntValue(index: Int, defaultValue: Int): Int = defaultValue
            override fun getAttributeUnsignedIntValue(index: Int, defaultValue: Int): Int = defaultValue
            override fun getAttributeFloatValue(index: Int, defaultValue: Float): Float = defaultValue
            override fun getAttributeListValue(namespace: String?, attribute: String?, options: Array<out String>?, defaultValue: Int): Int = defaultValue
            override fun getAttributeBooleanValue(namespace: String?, attribute: String?, defaultValue: Boolean): Boolean = defaultValue
            override fun getAttributeResourceValue(namespace: String?, attribute: String?, defaultValue: Int): Int = defaultValue
            override fun getAttributeIntValue(namespace: String?, attribute: String?, defaultValue: Int): Int = defaultValue
            override fun getAttributeUnsignedIntValue(namespace: String?, attribute: String?, defaultValue: Int): Int = defaultValue
            override fun getAttributeFloatValue(namespace: String?, attribute: String?, defaultValue: Float): Float = defaultValue
            override fun getIdAttribute(): String? = null
            override fun getIdAttributeResourceValue(defaultValue: Int): Int = defaultValue
            override fun getClassAttribute(): String? = null
            override fun getStyleAttribute(): Int = 0
            override fun getPositionDescription(): String = ""
        }
    }

    override fun initOptions() {
        Log.d(TAG, "initOptions")
        // 视频输出
        MPVLib.setOptionString("vo", "gpu")
        // GPU 缩放
        MPVLib.setOptionString("scale", "spline64")
        MPVLib.setOptionString("dscale", "mitchell")
        MPVLib.setOptionString("cscale", "spline64")
        // 音频输出
        MPVLib.setOptionString("ao", "audiotrack,opensles,")
        // 缓存设置
        MPVLib.setOptionString("cache", "yes")
        MPVLib.setOptionString("demuxer-max-bytes", "512MiB")
        MPVLib.setOptionString("demuxer-max-back-bytes", "256MiB")
        // 网络设置
        MPVLib.setOptionString("stream-lavf-o", "timeout=3000000,reconnect=1,reconnect_streamed=1")
        // 字幕
        MPVLib.setOptionString("sub-auto", "fuzzy")
        MPVLib.setOptionString("sub-codepage", "utf8:gbk")
        // 硬件解码
        MPVLib.setOptionString("hwdec", "mediacodec-copy")
        MPVLib.setOptionString("hwdec-codecs", "h264,hevc,vp8,vp9,av1,mpeg2video,mpeg4")
    }

    override fun postInitOptions() {
        Log.d(TAG, "postInitOptions")
    }

    override fun observeProperties() {
        Log.d(TAG, "observeProperties")
        MPVLib.observeProperty("time-pos", MPVLib.mpvFormat.MPV_FORMAT_INT64)
        MPVLib.observeProperty("duration", MPVLib.mpvFormat.MPV_FORMAT_INT64)
        MPVLib.observeProperty("pause", MPVLib.mpvFormat.MPV_FORMAT_FLAG)
        MPVLib.observeProperty("speed", MPVLib.mpvFormat.MPV_FORMAT_DOUBLE)
        MPVLib.observeProperty("paused-for-cache", MPVLib.mpvFormat.MPV_FORMAT_FLAG)
        MPVLib.observeProperty("volume", MPVLib.mpvFormat.MPV_FORMAT_INT64)
        MPVLib.observeProperty("track-list", MPVLib.mpvFormat.MPV_FORMAT_NODE)
        MPVLib.observeProperty("chapter-list", MPVLib.mpvFormat.MPV_FORMAT_NODE)
        MPVLib.observeProperty("demuxer-cache-time", MPVLib.mpvFormat.MPV_FORMAT_DOUBLE)
        MPVLib.observeProperty("playtime-remaining", MPVLib.mpvFormat.MPV_FORMAT_DOUBLE)
        MPVLib.observeProperty("media-title", MPVLib.mpvFormat.MPV_FORMAT_STRING)
    }

    /**
     * 播放视频文件
     */
    fun playUrl(url: String) {
        Log.d(TAG, "playUrl: $url")
        playFile(url)
    }
}
