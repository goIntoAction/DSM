package wang.zengye.dsm.ui.player

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import wang.zengye.dsm.R

/**
 * MPV 播放器视图容器
 * 用于从 XML 布局加载 InnerMPVView
 */
class MPVPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // 内部实际的 MPV 视图
    private var innerView: InnerMPVView? = null

    init {
        // 从 XML 加载布局
        LayoutInflater.from(context).inflate(R.layout.player_view, this, true)
        innerView = findViewById(R.id.player_view)
    }

    /**
     * 初始化 MPV
     */
    fun initialize(configDir: String, cacheDir: String) {
        innerView?.initialize(configDir, cacheDir)
    }

    /**
     * 播放视频
     */
    fun playUrl(url: String) {
        innerView?.playUrl(url)
    }

    /**
     * 销毁播放器
     */
    fun destroy() {
        innerView?.destroy()
    }
}
