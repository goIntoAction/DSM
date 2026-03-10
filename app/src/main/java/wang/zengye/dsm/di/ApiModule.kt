package wang.zengye.dsm.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import wang.zengye.dsm.data.repository.PerformanceHistoryRepository
import javax.inject.Singleton

/**
 * API 层 Hilt Module
 * 注意：所有 API 调用已迁移到 Retrofit 接口，由 NetworkModule 提供
 */
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun providePerformanceHistoryRepository(): PerformanceHistoryRepository = PerformanceHistoryRepository
}