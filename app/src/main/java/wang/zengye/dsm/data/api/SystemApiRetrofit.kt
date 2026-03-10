package wang.zengye.dsm.data.api

import wang.zengye.dsm.data.model.dashboard.SystemInfoDto
import wang.zengye.dsm.data.model.dashboard.SystemInfoDetailDto
import wang.zengye.dsm.data.model.dashboard.UtilizationDto
import wang.zengye.dsm.data.model.dashboard.StorageDto
import wang.zengye.dsm.data.model.dashboard.NetworkInterfaceDto
import wang.zengye.dsm.data.model.dashboard.DesktopInitdataDto
import wang.zengye.dsm.data.model.dashboard.HostnameDto
import wang.zengye.dsm.data.model.dashboard.ConnectedUsersDataDto
import wang.zengye.dsm.data.model.dashboard.PowerActionDto
import wang.zengye.dsm.data.model.dashboard.SmartTestLogDto
import wang.zengye.dsm.data.model.dashboard.SmartTestActionDto
import wang.zengye.dsm.data.model.dashboard.ProcessListDto
import wang.zengye.dsm.data.model.dashboard.NotificationsDto
import wang.zengye.dsm.data.model.dashboard.SmartHealthDto
import wang.zengye.dsm.data.model.dashboard.LogsDto
import wang.zengye.dsm.data.model.dashboard.LogHistoryDto
import wang.zengye.dsm.data.model.dashboard.ProcessGroupDto
import wang.zengye.dsm.data.model.dashboard.HardwarePowerDto
import wang.zengye.dsm.data.model.update.FirmwareVersionDto
import wang.zengye.dsm.data.model.control_panel.TerminalInfoDto
import wang.zengye.dsm.data.model.control_panel.FileServicesDto
import wang.zengye.dsm.data.model.control_panel.CertificatesDto
import wang.zengye.dsm.data.model.control_panel.ExternalDevicesDto
import wang.zengye.dsm.data.model.control_panel.UserDetailDto
import wang.zengye.dsm.data.model.control_panel.UserGroupsDto
import wang.zengye.dsm.data.model.control_panel.ShareFoldersDto
import wang.zengye.dsm.data.model.control_panel.ShareDetailDto
import wang.zengye.dsm.data.model.control_panel.VolumesDto
import wang.zengye.dsm.data.model.control_panel.UsersDto
import wang.zengye.dsm.data.model.control_panel.GroupsDto
import wang.zengye.dsm.data.model.control_panel.FtpSettingsDto
import wang.zengye.dsm.data.model.control_panel.FirewallDto
import wang.zengye.dsm.data.model.control_panel.TaskSchedulerDto
import wang.zengye.dsm.data.model.control_panel.DdnsRecordsDto
import wang.zengye.dsm.data.model.control_panel.MediaIndexDto
import wang.zengye.dsm.data.model.control_panel.NetworkInfoDto
import wang.zengye.dsm.data.model.control_panel.SecurityScanDto
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * 使用 Retrofit + Moshi 的系统 API 接口
 * API 响应直接转换为强类型对象
 */
interface SystemApiRetrofit {

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getSystemInfo(
        @Field("api") api: String = "SYNO.Core.System",
        @Field("version") version: String = "1",
        @Field("method") method: String = "info"
    ): Response<SystemInfoDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getUtilization(
        @Field("api") api: String = "SYNO.Core.System.Utilization",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get",
        @Field("type") type: String = "current",
        @Field("resource") resource: String = "[\"cpu\",\"memory\",\"network\",\"lun\",\"disk\",\"space\"]"
    ): Response<UtilizationDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getStorageInfo(
        @Field("api") api: String = "SYNO.Storage.CGI.Storage",
        @Field("version") version: String = "1",
        @Field("method") method: String = "load_info"
    ): Response<StorageDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getNetworkInterfaces(
        @Field("api") api: String = "SYNO.Core.Network.Interface",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list"
    ): Response<NetworkInterfaceDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getDesktopInitdata(
        @Field("api") api: String = "SYNO.Core.Desktop.Initdata",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get"
    ): Response<DesktopInitdataDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getHostname(
        @Field("api") api: String = "SYNO.Core.System",
        @Field("version") version: String = "1",
        @Field("method") method: String = "info"
    ): Response<HostnameDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getConnectedUsers(
        @Field("api") api: String = "SYNO.Core.CurrentConnection",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list"
    ): Response<ConnectedUsersDataDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun shutdown(
        @Field("api") api: String = "SYNO.Core.System",
        @Field("version") version: String = "1",
        @Field("method") method: String = "shutdown",
        @Field("force") force: Boolean = false,
        @Field("local") local: Boolean = true
    ): Response<PowerActionDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun reboot(
        @Field("api") api: String = "SYNO.Core.System",
        @Field("version") version: String = "1",
        @Field("method") method: String = "reboot",
        @Field("force") force: Boolean = false,
        @Field("local") local: Boolean = true
    ): Response<PowerActionDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getSmartTestLog(
        @Field("api") api: String = "SYNO.Storage.CGI.SMART",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get_log",
        @Field("device") device: String = ""
    ): Response<SmartTestLogDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun doSmartTest(
        @Field("api") api: String = "SYNO.Storage.CGI.SMART",
        @Field("version") version: String = "1",
        @Field("method") method: String = "do_test",
        @Field("device") device: String,
        @Field("type") type: String
    ): Response<SmartTestActionDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getProcessList(
        @Field("api") api: String = "SYNO.Core.System.Process",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list"
    ): Response<ProcessListDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getNotifications(
        @Field("api") api: String = "SYNO.Core.Notification",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list"
    ): Response<NotificationsDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getSmartHealth(
        @Field("api") api: String = "SYNO.Storage.CGI.SMART",
        @Field("version") version: String = "1",
        @Field("method") method: String = "health"
    ): Response<SmartHealthDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getLatestLogs(
        @Field("api") api: String = "SYNO.Core.SyslogClient.Status",
        @Field("version") version: String = "1",
        @Field("method") method: String = "latestlog_get",
        @Field("start") start: Int = 0,
        @Field("limit") limit: Int = 50
    ): Response<LogsDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getLogs(
        @Field("api") api: String = "SYNO.Core.SyslogClient.Log",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list",
        @Field("start") start: Int = 0,
        @Field("limit") limit: Int = 50,
        @Field("target") target: String = "LOCAL",
        @Field("logtype") logType: String = "system"
    ): Response<LogsDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getLogHistory(
        @Field("api") api: String = "SYNO.Core.SyslogClient.History",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list"
    ): Response<LogHistoryDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getProcessGroup(
        @Field("api") api: String = "SYNO.Core.DSM.ProcessGroup",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list",
        @Field("node") node: String = "xnode-2572"
    ): Response<ProcessGroupDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getHardwarePower(
        @Field("api") api: String = "SYNO.Core.Hardware.BeepControl",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get"
    ): Response<HardwarePowerDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getFirmwareVersion(
        @Field("api") api: String = "SYNO.Core.Upgrade.Server",
        @Field("version") version: String = "2",
        @Field("method") method: String = "check",
        @Field("user_reading") userReading: Boolean = true,
        @Field("need_auto_smallupdate") needAutoSmallUpdate: Boolean = true,
        @Field("need_promotion") needPromotion: Boolean = true
    ): Response<FirmwareVersionDto>

    // ==================== Control Panel Moshi API ====================

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getTerminalInfo(
        @Field("api") api: String = "SYNO.Core.Terminal",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get"
    ): Response<TerminalInfoDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getFileServices(
        @Field("api") api: String = "SYNO.Core.FileServ.SMB",
        @Field("version") version: String = "3",
        @Field("method") method: String = "get",
        @Field("type") type: String = "all"
    ): Response<FileServicesDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getCertificates(
        @Field("api") api: String = "SYNO.Core.Certificate",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list"
    ): Response<CertificatesDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getExternalDevices(
        @Field("api") api: String = "SYNO.Core.ExternalDevice",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list"
    ): Response<ExternalDevicesDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getSystemInfoDetail(
        @Field("api") api: String = "SYNO.Core.System",
        @Field("version") version: String = "1",
        @Field("method") method: String = "info"
    ): Response<SystemInfoDetailDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getUserDetail(
        @Field("api") api: String = "SYNO.Core.User",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get",
        @Field("name") name: String,
        @Field("additional") additional: String = "[\"description\",\"email\",\"expired\",\"passwd_expire\"]"
    ): Response<UserDetailDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getUserGroups(
        @Field("api") api: String = "SYNO.Core.User",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get",
        @Field("name") name: String
    ): Response<UserGroupsDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getShares(
        @Field("api") api: String = "SYNO.Core.FileServ.Share",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list"
    ): Response<ShareFoldersDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getShareDetail(
        @Field("api") api: String = "SYNO.Core.FileServ.Share",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get",
        @Field("name") name: String
    ): Response<ShareDetailDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getVolumes(
        @Field("api") api: String = "SYNO.Storage.CGI.Storage",
        @Field("version") version: String = "1",
        @Field("method") method: String = "load_info"
    ): Response<VolumesDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getUsers(
        @Field("api") api: String = "SYNO.Core.User",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list"
    ): Response<UsersDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getGroups(
        @Field("api") api: String = "SYNO.Core.User",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get",
        @Field("offset") offset: Int = 0,
        @Field("limit") limit: Int = -1
    ): Response<GroupsDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getFtpSettings(
        @Field("api") api: String = "SYNO.Core.FileServ.FTP",
        @Field("version") version: String = "3",
        @Field("method") method: String = "get"
    ): Response<FtpSettingsDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getFirewall(
        @Field("api") api: String = "SYNO.Core.Security.Firewall",
        @Field("version") version: String = "1",
        @Field("method") method: String = "rule_list"
    ): Response<FirewallDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getTaskScheduler(
        @Field("api") api: String = "SYNO.Core.TaskScheduler",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list"
    ): Response<TaskSchedulerDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getDdnsRecords(
        @Field("api") api: String = "SYNO.Core.DDNS.Record",
        @Field("version") version: String = "1",
        @Field("method") method: String = "list"
    ): Response<DdnsRecordsDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getMediaIndexStatus(
        @Field("api") api: String = "SYNO.Core.MediaIndexingService",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get"
    ): Response<MediaIndexDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getNetworkInfo(
        @Field("api") api: String = "SYNO.Core.Network",
        @Field("version") version: String = "1",
        @Field("method") method: String = "get"
    ): Response<NetworkInfoDto>

    @FormUrlEncoded
    @POST("entry.cgi")
    suspend fun getSecurityScan(
        @Field("api") api: String = "SYNO.Core.SecurityScan",
        @Field("version") version: String = "1",
        @Field("method") method: String = "check"
    ): Response<SecurityScanDto>
}
