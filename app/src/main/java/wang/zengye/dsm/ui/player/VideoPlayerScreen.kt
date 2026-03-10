package wang.zengye.dsm.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import wang.zengye.dsm.ui.player.controls.PlayerControls

/**
 * 视频播放器界面
 * 使用 mpvKt 风格的控制界面
 * 支持全屏沉浸式模式和横竖屏切换
 */
@Composable
fun VideoPlayerScreen(
    videoUrl: String,
    videoTitle: String = "",
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // 保存原始配置
    var originalRequestedOrientation by remember {
        mutableStateOf(activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
    }

    // 进入全屏沉浸式模式
    DisposableEffect(Unit) {
        activity?.let {
            originalRequestedOrientation = it.requestedOrientation

            // 允许内容绘制在系统栏后面
            WindowCompat.setDecorFitsSystemWindows(it.window, false)

            // 设置布局无限制
            it.window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )

            // 保持屏幕常亮
            it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            // 使用 WindowInsetsControllerCompat 隐藏系统栏
            val windowInsetsController = WindowCompat.getInsetsController(it.window, it.window.decorView)
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            // 设置刘海屏模式
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                it.window.attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }

            // 允许所有方向
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }

        onDispose {
            activity?.let {
                it.requestedOrientation = originalRequestedOrientation
                it.window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                it.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

                val windowInsetsController = WindowCompat.getInsetsController(it.window, it.window.decorView)
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())

                WindowCompat.setDecorFitsSystemWindows(it.window, true)
            }
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        PlayerControls(
            videoUrl = videoUrl,
            videoTitle = videoTitle,
            onBack = onBack
        )
    }
}
