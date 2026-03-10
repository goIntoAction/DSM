package wang.zengye.dsm.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wang.zengye.dsm.data.api.DownloadApiRetrofit
import wang.zengye.dsm.data.model.BtFileListDto
import wang.zengye.dsm.data.model.BtPeerListDto
import wang.zengye.dsm.data.model.BtTrackerListDto
import wang.zengye.dsm.data.model.DownloadLocationDto
import wang.zengye.dsm.data.model.DownloadTaskDetailDto
import wang.zengye.dsm.data.model.DownloadTaskListDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository @Inject constructor(
    private val downloadApiRetrofit: DownloadApiRetrofit
) : BaseRepository() {

    // ==================== Moshi 版本 ====================

    /**
     * 获取任务列表 - Moshi 版本
     */
    suspend fun getTaskList(): Result<DownloadTaskListDto> {
        return withContext(Dispatchers.IO) {
            try {
                val api = downloadApiRetrofit ?: return@withContext Result.failure(Exception("DownloadApiRetrofit not available"))
                val response = api.getTaskList()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 暂停任务 - Moshi 版本
     */
    suspend fun pauseTask(id: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val api = downloadApiRetrofit ?: return@withContext Result.failure(Exception("DownloadApiRetrofit not available"))
                val response = api.pauseTask(id = id)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 恢复任务 - Moshi 版本
     */
    suspend fun resumeTask(id: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val api = downloadApiRetrofit ?: return@withContext Result.failure(Exception("DownloadApiRetrofit not available"))
                val response = api.resumeTask(id = id)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 删除任务 - Moshi 版本
     */
    suspend fun deleteTask(id: String, forceComplete: Boolean = false): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val api = downloadApiRetrofit ?: return@withContext Result.failure(Exception("DownloadApiRetrofit not available"))
                val response = api.deleteTask(id = id)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取 BT 文件列表 - Moshi 版本
     */
    suspend fun getBtFileList(taskId: String): Result<BtFileListDto> {
        return withContext(Dispatchers.IO) {
            try {
                val api = downloadApiRetrofit ?: return@withContext Result.failure(Exception("DownloadApiRetrofit not available"))
                val response = api.getBtFileList(id = taskId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 设置 BT 文件选择 - Moshi 版本
     */
    suspend fun setBtFileSelection(taskId: String, fileIndexes: List<Int>, selected: Boolean): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val api = downloadApiRetrofit ?: return@withContext Result.failure(Exception("DownloadApiRetrofit not available"))
                val response = api.setBtFileSelections(
                    id = taskId,
                    fileIndexes = fileIndexes.joinToString(","),
                    selected = selected
                )
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取 BT Tracker 列表 - Moshi 版本
     */
    suspend fun getBtTrackerList(taskId: String): Result<BtTrackerListDto> {
        return withContext(Dispatchers.IO) {
            try {
                val api = downloadApiRetrofit ?: return@withContext Result.failure(Exception("DownloadApiRetrofit not available"))
                val response = api.getBtTrackerList(taskId = taskId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取 BT Peer 列表 - Moshi 版本
     */
    suspend fun getBtPeerList(taskId: String): Result<BtPeerListDto> {
        return withContext(Dispatchers.IO) {
            try {
                val api = downloadApiRetrofit ?: return@withContext Result.failure(Exception("DownloadApiRetrofit not available"))
                val response = api.getBtPeerList(taskId = taskId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取下载位置 - Moshi 版本
     */
    suspend fun getDownloadLocation(): Result<DownloadLocationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val api = downloadApiRetrofit ?: return@withContext Result.failure(Exception("DownloadApiRetrofit not available"))
                val response = api.getDownloadLocation()
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取任务详情 - Moshi 版本
     */
    suspend fun getTaskInfo(taskId: String): Result<DownloadTaskDetailDto> {
        return withContext(Dispatchers.IO) {
            try {
                val api = downloadApiRetrofit ?: return@withContext Result.failure(Exception("DownloadApiRetrofit not available"))
                val response = api.getTaskInfo(id = taskId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 添加 BT Tracker
     */
    suspend fun addBtTracker(taskId: String, trackers: List<String>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val api = downloadApiRetrofit ?: return@withContext Result.failure(Exception("DownloadApiRetrofit not available"))
                val response = api.addBtTracker(taskId = taskId, tracker = trackers.joinToString("\n"))
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 从 URL 创建下载任务
     */
    suspend fun createTaskFromUrl(urls: List<String>, destination: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val api = downloadApiRetrofit ?: return@withContext Result.failure(Exception("DownloadApiRetrofit not available"))
                val response = api.createTaskFromUrl(uri = urls.joinToString("\n"), destination = destination)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 从文件创建下载任务
     */
    suspend fun createTaskFromFile(file: java.io.File, destination: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val api = downloadApiRetrofit ?: return@withContext Result.failure(Exception("DownloadApiRetrofit not available"))
                val response = api.createTaskFromFile(file = file.absolutePath, destination = destination)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
