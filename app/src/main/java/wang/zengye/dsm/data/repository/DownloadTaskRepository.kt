package wang.zengye.dsm.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import wang.zengye.dsm.data.dao.DownloadTaskDao
import wang.zengye.dsm.data.entity.DownloadTaskEntity
import wang.zengye.dsm.data.model.AppDownloadStatus
import wang.zengye.dsm.data.model.AppDownloadTask
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 下载任务 Repository（Room 数据库版本）
 */
@Singleton
class DownloadTaskRepository @Inject constructor(
    private val downloadTaskDao: DownloadTaskDao
) {

    /**
     * 获取所有任务（Flow）
     */
    fun getAllTasksFlow(): Flow<List<AppDownloadTask>> {
        return downloadTaskDao.getAllTasksFlow()
            .map { entities -> entities.map { it.toAppDownloadTask() } }
    }

    /**
     * 获取所有任务
     */
    suspend fun getAllTasks(): List<AppDownloadTask> {
        return downloadTaskDao.getAllTasks()
            .map { it.toAppDownloadTask() }
    }

    /**
     * 根据 ID 获取任务
     */
    suspend fun getTaskById(taskId: String): AppDownloadTask? {
        return downloadTaskDao.getTaskById(taskId)?.toAppDownloadTask()
    }

    /**
     * 插入任务
     */
    suspend fun insertTask(task: AppDownloadTask) {
        downloadTaskDao.insertTask(task.toDownloadTaskEntity())
    }

    /**
     * 批量插入任务
     */
    suspend fun insertTasks(tasks: List<AppDownloadTask>) {
        downloadTaskDao.insertTasks(tasks.map { it.toDownloadTaskEntity() })
    }

    /**
     * 更新任务
     */
    suspend fun updateTask(task: AppDownloadTask) {
        downloadTaskDao.updateTask(task.toDownloadTaskEntity())
    }

    /**
     * 删除任务
     */
    suspend fun deleteTask(task: AppDownloadTask) {
        downloadTaskDao.deleteTask(task.toDownloadTaskEntity())
    }

    /**
     * 根据 ID 删除任务
     */
    suspend fun deleteTaskById(taskId: String) {
        downloadTaskDao.deleteTaskById(taskId)
    }

    /**
     * 删除指定状态的任务
     */
    suspend fun deleteTasksByStatus(statuses: List<AppDownloadStatus>) {
        downloadTaskDao.deleteTasksByStatus(statuses.map { it.name })
    }

    /**
     * 清空所有任务
     */
    suspend fun deleteAllTasks() {
        downloadTaskDao.deleteAllTasks()
    }

    /**
     * 获取任务数量
     */
    suspend fun getTaskCount(): Int {
        return downloadTaskDao.getTaskCount()
    }

    /**
     * 获取指定状态的任务数量
     */
    suspend fun getTaskCountByStatus(status: AppDownloadStatus): Int {
        return downloadTaskDao.getTaskCountByStatus(status.name)
    }

    /**
     * 获取指定状态的任务列表
     */
    suspend fun getTasksByStatus(statuses: List<AppDownloadStatus>): List<AppDownloadTask> {
        return downloadTaskDao.getTasksByStatus(statuses.map { it.name })
            .map { it.toAppDownloadTask() }
    }
}

/**
 * 扩展函数：DownloadTaskEntity -> AppDownloadTask
 */
private fun DownloadTaskEntity.toAppDownloadTask(): AppDownloadTask {
    return AppDownloadTask(
        id = id,
        fileName = fileName,
        remotePath = remotePath,
        downloadUrl = downloadUrl,
        directoryUri = directoryUri,
        totalBytes = totalBytes,
        downloadedBytes = downloadedBytes,
        status = AppDownloadStatus.valueOf(status),
        createTime = createTime,
        finishTime = finishTime,
        errorMessage = errorMessage
    )
}

/**
 * 扩展函数：AppDownloadTask -> DownloadTaskEntity
 */
private fun AppDownloadTask.toDownloadTaskEntity(): DownloadTaskEntity {
    return DownloadTaskEntity(
        id = id,
        fileName = fileName,
        remotePath = remotePath,
        downloadUrl = downloadUrl,
        directoryUri = directoryUri,
        totalBytes = totalBytes,
        downloadedBytes = downloadedBytes,
        status = status.name,
        createTime = createTime,
        finishTime = finishTime,
        errorMessage = errorMessage
    )
}
