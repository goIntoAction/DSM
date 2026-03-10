package wang.zengye.dsm

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.launch
import wang.zengye.dsm.data.api.DsmApiHelper
import wang.zengye.dsm.util.SettingsManager

@HiltAndroidApp
class DSMApplication : Application(), ImageLoaderFactory {

    companion object {
        lateinit var instance: DSMApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 初始化设置管理器
        SettingsManager.init(this)

        // 同步初始化 DsmApiClient 的 OkHttpClient（Hilt 需要）
        DsmApiHelper.initOkHttpClient()

        // 异步加载会话状态
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            SettingsManager.migrateIfNeeded()
            DsmApiHelper.loadSessionAndInit()
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient { DsmApiHelper.imageClient }
            .crossfade(true)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // 使用 25% 的内存作为图片缓存
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(512L * 1024 * 1024) // 512MB 磁盘缓存
                    .build()
            }
            .build()
    }
}
