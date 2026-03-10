package wang.zengye.dsm.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import wang.zengye.dsm.data.dao.DownloadTaskDao
import wang.zengye.dsm.data.database.AppDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * 提供 AppDatabase
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    /**
     * 提供 DownloadTaskDao
     */
    @Provides
    @Singleton
    fun provideDownloadTaskDao(database: AppDatabase): DownloadTaskDao {
        return database.downloadTaskDao()
    }
}
