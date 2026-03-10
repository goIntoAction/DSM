package wang.zengye.dsm.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import wang.zengye.dsm.data.dao.DownloadTaskDao
import wang.zengye.dsm.data.entity.DownloadTaskEntity

/**
 * App 数据库
 */
@Database(
    entities = [DownloadTaskEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun downloadTaskDao(): DownloadTaskDao

    companion object {
        private const val DATABASE_NAME = "dsm_app_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * 用于测试的清理方法
         */
        fun clearInstance() {
            INSTANCE = null
        }
    }
}
