package wang.zengye.dsm.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import wang.zengye.dsm.data.entity.DownloadTaskEntity

/**
 * 下载任务 DAO
 */
@Dao
interface DownloadTaskDao {

    /**
     * 获取所有任务（按创建时间倒序）
     */
    @Query("SELECT * FROM download_tasks ORDER BY createTime DESC")
    fun getAllTasksFlow(): Flow<List<DownloadTaskEntity>>

    /**
     * 获取所有任务（一次性）
     */
    @Query("SELECT * FROM download_tasks ORDER BY createTime DESC")
    suspend fun getAllTasks(): List<DownloadTaskEntity>

    /**
     * 根据 ID 获取任务
     */
    @Query("SELECT * FROM download_tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): DownloadTaskEntity?

    /**
     * 根据状态获取任务
     */
    @Query("SELECT * FROM download_tasks WHERE status = :status ORDER BY createTime DESC")
    fun getTasksByStatusFlow(status: String): Flow<List<DownloadTaskEntity>>

    /**
     * 根据状态列表获取任务（一次性）
     */
    @Query("SELECT * FROM download_tasks WHERE status IN (:statuses) ORDER BY createTime DESC")
    suspend fun getTasksByStatus(statuses: List<String>): List<DownloadTaskEntity>

    /**
     * 插入任务
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: DownloadTaskEntity)

    /**
     * 批量插入任务
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<DownloadTaskEntity>)

    /**
     * 更新任务
     */
    @Update
    suspend fun updateTask(task: DownloadTaskEntity)

    /**
     * 删除任务
     */
    @Delete
    suspend fun deleteTask(task: DownloadTaskEntity)

    /**
     * 根据 ID 删除任务
     */
    @Query("DELETE FROM download_tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    /**
     * 删除指定状态的任务
     */
    @Query("DELETE FROM download_tasks WHERE status IN (:statuses)")
    suspend fun deleteTasksByStatus(statuses: List<String>)

    /**
     * 清空所有任务
     */
    @Query("DELETE FROM download_tasks")
    suspend fun deleteAllTasks()

    /**
     * 获取任务数量
     */
    @Query("SELECT COUNT(*) FROM download_tasks")
    suspend fun getTaskCount(): Int

    /**
     * 获取指定状态的任务数量
     */
    @Query("SELECT COUNT(*) FROM download_tasks WHERE status = :status")
    suspend fun getTaskCountByStatus(status: String): Int
}
