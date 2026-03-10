package wang.zengye.dsm.data.api

import wang.zengye.dsm.data.model.BtFileListDto
import wang.zengye.dsm.data.model.BtPeerListDto
import wang.zengye.dsm.data.model.BtTrackerListDto
import wang.zengye.dsm.data.model.DownloadLocationDto
import wang.zengye.dsm.data.model.DownloadTaskDetailDto
import wang.zengye.dsm.data.model.DownloadTaskListDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * 使用 Retrofit + Moshi 的 Download API 接口
 */
interface DownloadApiRetrofit {

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getTaskList(
        @Field("api") api: String = "SYNO.DownloadStation2.Task",
        @Field("version") version: String = "2",
        @Field("method") method: String = "list",
        @Field("additional") additional: String = "[\"detail\",\"transfer\"]"
    ): Response<DownloadTaskListDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getTaskInfo(
        @Field("api") api: String = "SYNO.DownloadStation2.Task",
        @Field("version") version: String = "2",
        @Field("method") method: String = "getinfo",
        @Field("id") id: String
    ): Response<DownloadTaskDetailDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun pauseTask(
        @Field("api") api: String = "SYNO.DownloadStation2.Task",
        @Field("version") version: String = "2",
        @Field("method") method: String = "pause",
        @Field("id") id: String
    ): Response<Unit>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun resumeTask(
        @Field("api") api: String = "SYNO.DownloadStation2.Task",
        @Field("version") version: String = "2",
        @Field("method") method: String = "resume",
        @Field("id") id: String
    ): Response<Unit>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun deleteTask(
        @Field("api") api: String = "SYNO.DownloadStation2.Task",
        @Field("version") version: String = "2",
        @Field("method") method: String = "delete",
        @Field("id") id: String,
        @Field("force_complete") forceComplete: Boolean = false
    ): Response<Unit>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getBtFileList(
        @Field("api") api: String = "SYNO.DownloadStation2.Task.BT.File",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list",
        @Field("id") id: String
    ): Response<BtFileListDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun setBtFileSelections(
        @Field("api") api: String = "SYNO.DownloadStation2.Task.BT.File",
        @Field("version") version: String = "1",
        @Field("method") method: String = "set_files",
        @Field("id") id: String,
        @Field("file_indexes") fileIndexes: String,
        @Field("selected") selected: Boolean
    ): Response<Unit>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getBtTrackerList(
        @Field("api") api: String = "SYNO.DownloadStation2.Task.BT.Tracker",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list",
        @Field("id") taskId: String
    ): Response<BtTrackerListDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getBtPeerList(
        @Field("api") api: String = "SYNO.DownloadStation2.Task.BT.Peer",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list",
        @Field("id") taskId: String
    ): Response<BtPeerListDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getDownloadLocation(
        @Field("api") api: String = "SYNO.DownloadStation2.Settings.Location",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get"
    ): Response<DownloadLocationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getTaskInfoDetail(
        @Field("api") api: String = "SYNO.DownloadStation2.Task",
        @Field("version") version: String = "2",
        @Field("method") method: String = "getinfo",
        @Field("id") id: String,
        @Field("additional") additional: String = "[\"detail\",\"transfer\",\"file\"]"
    ): Response<DownloadTaskDetailDto>

    // ==================== BT Tracker ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun addBtTracker(
        @Field("api") api: String = "SYNO.DownloadStation2.Task.BT.Tracker",
        @Field("version") version: String = "1",
        @Field("method") method: String = "add",
        @Field("id") taskId: String,
        @Field("tracker") tracker: String
    ): Response<Unit>

    // ==================== 创建任务 ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun createTaskFromUrl(
        @Field("api") api: String = "SYNO.DownloadStation2.Task",
        @Field("version") version: String = "2",
        @Field("method") method: String = "create",
        @Field("uri") uri: String,
        @Field("destination") destination: String
    ): Response<Unit>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun createTaskFromFile(
        @Field("api") api: String = "SYNO.DownloadStation2.Task",
        @Field("version") version: String = "2",
        @Field("method") method: String = "create",
        @Field("file") file: String,
        @Field("destination") destination: String
    ): Response<Unit>

    @Multipart
    @POST("entry.cgi")
    suspend fun createTaskFromTorrentFile(
        @Part("api") api: RequestBody,
        @Part("version") version: RequestBody,
        @Part("method") method: RequestBody,
        @Part("type") type: RequestBody,
        @Part("file") file: RequestBody,
        @Part("destination") destination: RequestBody,
        @Part("create_list") createList: RequestBody,
        @Part torrentFile: MultipartBody.Part
    ): Response<Unit>
}
