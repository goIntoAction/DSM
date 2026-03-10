package wang.zengye.dsm.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import wang.zengye.dsm.data.api.DsmApiHelper
import wang.zengye.dsm.data.api.SystemApiRetrofit
import wang.zengye.dsm.data.api.DockerApiRetrofit
import wang.zengye.dsm.data.api.DownloadApiRetrofit
import wang.zengye.dsm.data.api.PackageApiRetrofit
import wang.zengye.dsm.data.api.AuthApiRetrofit
import wang.zengye.dsm.data.api.FileApiRetrofit
import wang.zengye.dsm.data.api.PhotoApiRetrofit
import wang.zengye.dsm.data.api.ControlPanelApiRetrofit
import wang.zengye.dsm.data.api.VirtualMachineApiRetrofit
import wang.zengye.dsm.data.api.IscsiApiRetrofit
import wang.zengye.dsm.data.model.dashboard.RemainLifeAdapter
import javax.inject.Singleton

/**
 * Network 层 Hilt Module
 * 提供 Retrofit + Moshi 实例
 *
 * 拦截器架构：
 * - SessionInterceptor: 添加会话相关 headers（Cookie、Accept、Origin等）
 * - DynamicBaseUrlInterceptor: 动态替换 baseUrl（从 SettingsManager 读取）
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(RemainLifeAdapter())
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    /**
     * 提供 OkHttpClient
     * 直接复用 DsmApiClient.okHttpClient，统一 SSL 和拦截器配置
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        // 确保已初始化（DSMApplication 也会调用，这里双重保障）
        DsmApiHelper.initOkHttpClient()
        return DsmApiHelper.okHttpClient
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://localhost/webapi/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideSystemApiRetrofit(retrofit: Retrofit): SystemApiRetrofit {
        return retrofit.create(SystemApiRetrofit::class.java)
    }

    @Provides
    @Singleton
    fun provideDockerApiRetrofit(retrofit: Retrofit): DockerApiRetrofit {
        return retrofit.create(DockerApiRetrofit::class.java)
    }

    @Provides
    @Singleton
    fun provideDownloadApiRetrofit(retrofit: Retrofit): DownloadApiRetrofit {
        return retrofit.create(DownloadApiRetrofit::class.java)
    }

    @Provides
    @Singleton
    fun providePackageApiRetrofit(retrofit: Retrofit): PackageApiRetrofit {
        return retrofit.create(PackageApiRetrofit::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthApiRetrofit(retrofit: Retrofit): AuthApiRetrofit {
        return retrofit.create(AuthApiRetrofit::class.java)
    }

    @Provides
    @Singleton
    fun provideFileApiRetrofit(retrofit: Retrofit): FileApiRetrofit {
        return retrofit.create(FileApiRetrofit::class.java)
    }

    @Provides
    @Singleton
    fun providePhotoApiRetrofit(retrofit: Retrofit): PhotoApiRetrofit {
        return retrofit.create(PhotoApiRetrofit::class.java)
    }

    @Provides
    @Singleton
    fun provideControlPanelApiRetrofit(retrofit: Retrofit): ControlPanelApiRetrofit {
        return retrofit.create(ControlPanelApiRetrofit::class.java)
    }

    @Provides
    @Singleton
    fun provideVirtualMachineApiRetrofit(retrofit: Retrofit): VirtualMachineApiRetrofit {
        return retrofit.create(VirtualMachineApiRetrofit::class.java)
    }

    @Provides
    @Singleton
    fun provideIscsiApiRetrofit(retrofit: Retrofit): IscsiApiRetrofit {
        return retrofit.create(IscsiApiRetrofit::class.java)
    }
}