package wang.zengye.dsm.data.api

/**
 * API 常量定义
 * 集中管理所有 API 相关的字符串常量
 */
object ApiConstants {
    
    // CGI 路径
    object Cgi {
        const val ENTRY = "entry.cgi"
        const val AUTH = "auth.cgi"
        const val QUERY = "query.cgi"
        const val DOWNLOAD = "download.cgi"
        const val FILE_STATION = "filestation.cgi"
    }
    
    // API 名称
    object Api {
        // 认证相关
        const val AUTH = "SYNO.API.Auth"
        const val AUTH_2FA = "SYNO.API.Auth.OTP"
        
        // 系统
        const val CORE_SYSTEM = "SYNO.Core.System"
        const val CORE_SYSTEM_INFO = "SYNO.Core.System.Info"
        const val CORE_SYSTEM_UTILIZATION = "SYNO.Core.System.Utilization"
        const val CORE_SYSTEM_NETWORK = "SYNO.Core.System.Network"
        const val CORE_SYSTEM_STATUS = "SYNO.Core.System.Status"
        const val CORE_SYSTEM_HEALTH = "SYNO.Core.System.Health"
        
        // 存储
        const val STORAGE = "SYNO.Storage.CGI.Storage"
        const val STORAGE_ISCSI = "SYNO.Storage.ISCSI.LUN"
        
        // 文件站
        const val FILE_STATION = "SYNO.FileStation.*"
        const val FILE_LIST = "SYNO.FileStation.List"
        const val FILE_SEARCH = "SYNO.FileStation.Search"
        const val FILE_COPY_MOVE = "SYNO.FileStation.CopyMove"
        const val FILE_DELETE = "SYNO.FileStation.Delete"
        const val FILE_EXTRACT = "SYNO.FileStation.Extract"
        const val FILE_COMPRESS = "SYNO.FileStation.Compress"
        const val FILE_SHARE = "SYNO.FileStation.Sharing"
        const val FILE_VIRTUAL_FOLDER = "SYNO.FileStation.VirtualFolder"
        const val FILE_FAVORITE = "SYNO.FileStation.Favorite"
        const val FILE_THUMB = "SYNO.FileStation.Thumb"
        const val FILE_UPLOAD = "SYNO.FileStation.Upload"
        const val FILE_DOWNLOAD = "SYNO.FileStation.Download"
        
        // 下载站
        const val DOWNLOAD_STATION = "SYNO.DownloadStation2.*"
        const val DOWNLOAD_TASK = "SYNO.DownloadStation2.Task"
        const val DOWNLOAD_STATISTIC = "SYNO.DownloadStation2.Task.Statistic"
        const val DOWNLOAD_BT_SEARCH = "SYNO.DownloadStation2.BTSearch"
        const val DOWNLOAD_RSS = "SYNO.DownloadStation2.RSS.Site"
        const val DOWNLOAD_BT = "SYNO.DownloadStation2.Task.BT"        
        // Audio Station
        const val AUDIO_STATION = "SYNO.AudioStation.*"
        const val AUDIO_PLAYLIST = "SYNO.AudioStation.Playlist"
        const val AUDIO_SONG = "SYNO.AudioStation.Song"
        const val AUDIO_ALBUM = "SYNO.AudioStation.Album"
        const val AUDIO_ARTIST = "SYNO.AudioStation.Artist"
        const val AUDIO_GENRE = "SYNO.AudioStation.Genre"
        const val AUDIO_FOLDER = "SYNO.AudioStation.Folder"
        const val AUDIO_RADIO = "SYNO.AudioStation.Radio"
        const val AUDIO_REMOTE_PLAYER = "SYNO.AudioStation.RemotePlayer"
        const val AUDIO_STREAM = "SYNO.AudioStation.Stream"
        
        // Photo Station
        const val PHOTO_STATION = "SYNO.PhotoStation.*"
        const val PHOTO = "SYNO.Photo"
        const val PHOTO_THUMB = "SYNO.Photo.Thumb"
        const val PHOTO_ALBUM = "SYNO.Photo.Album"
        const val PHOTO_FOLDER = "SYNO.Photo.Folder"
        const val PHOTO_BROWSE = "SYNO.Photo.Browse"
        
        // 控制面板
        const val CORE_USER = "SYNO.Core.User"
        const val CORE_GROUP = "SYNO.Core.Group"
        const val CORE_SHARE = "SYNO.Core.Share"
        const val CORE_PACKAGE = "SYNO.Core.Package"
        const val CORE_PACKAGE_INSTALL = "SYNO.Core.Package.Installation"
        const val CORE_BACKUP = "SYNO.Core.Backup"
        const val CORE_NETWORK = "SYNO.Core.Network"
        const val CORE_NETWORK_DNS = "SYNO.Core.Network.DNS"
        const val CORE_NETWORK_DHCP = "SYNO.Core.Network.DHCP"
        const val CORE_NETWORK_ROUTER = "SYNO.Core.Network.Router"
        const val CORE_NETWORK_VPN = "SYNO.Core.Network.VPN"
        const val CORE_SECURITY = "SYNO.Core.Security"
        const val CORE_SECURITY_FIREWALL = "SYNO.Core.Security.Firewall"
        const val CORE_SECURITY_AUTO_BLOCK = "SYNO.Core.Security.AutoBlock"
        const val CORE_SECURITY_CERTIFICATE = "SYNO.Core.Security.Certificate"
        const val CORE_SECURITY_2FA = "SYNO.Core.Security.OTP"
        
        // Docker
        const val DOCKER_CONTAINER = "SYNO.Docker.Container"
        const val DOCKER_IMAGE = "SYNO.Docker.Image"
        const val DOCKER_NETWORK = "SYNO.Docker.Network"
        const val DOCKER_VOLUME = "SYNO.Docker.Volume"
        const val DOCKER_REGISTRY = "SYNO.Docker.Registry"
        
        // 虚拟机
        const val VIRTUALIZATION = "SYNO.Virtualization.*"
        const val VIRTUALIZATION_VM = "SYNO.Virtualization.VirtualMachine"
        const val VIRTUALIZATION_STORAGE = "SYNO.Virtualization.Storage"
        const val VIRTUALIZATION_NETWORK = "SYNO.Virtualization.Network"
        const val VIRTUALIZATION_BACKUP = "SYNO.Virtualization.Backup"
        
        // 日志中心
        const val LOG_CENTER = "SYNO.LogCenter.*"
        const val LOG_CENTER_LOG = "SYNO.LogCenter.Log"
        const val LOG_CENTER_SETTING = "SYNO.LogCenter.Setting"
        
        // Surveillance Station
        const val SURVEILLANCE = "SYNO.SurveillanceStation.*"
        const val SURVEILLANCE_CAMERA = "SYNO.SurveillanceStation.Camera"
        const val SURVEILLANCE_EVENT = "SYNO.SurveillanceStation.Event"
        const val SURVEILLANCE_RECORDING = "SYNO.SurveillanceStation.Recording"
        
        // Active Backup
        const val ACTIVE_BACKUP = "SYNO.ActiveBackup.*"
        const val ACTIVE_BACKUP_TASK = "SYNO.ActiveBackup.Task"
        const val ACTIVE_BACKUP_RESTORE = "SYNO.ActiveBackup.Restore"
        
        // 快照
        const val SNAPSHOT = "SYNO.Core.Share.Snapshot"
        
        // 批量请求
        const val ENTRY_REQUEST = "SYNO.Entry.Request"
        
        // 服务发现
        const val API_INFO = "SYNO.API.Info"
        
        // 文件服务
        const val FILE_SERV_SMB = "SYNO.Core.FileServ.SMB"
        const val FILE_SERV_NFS = "SYNO.Core.FileServ.NFS"
        const val FILE_SERV_FTP = "SYNO.Core.FileServ.FTP"
        const val FILE_SERV_WEBDAV = "SYNO.Core.FileServ.WebDAV"
        
        // DDNS
        const val DDNS = "SYNO.Core.DDNS"
        const val DDNS_RECORD = "SYNO.Core.DDNS.Record"
        
        // DSM 设置
        const val DSM_SETTING = "SYNO.Core.DSM"
        const val DSM_UPDATE = "SYNO.Core.DSM.Update"
    }
    
    // 方法名称
    object Method {
        const val GET = "get"
        const val LIST = "list"
        const val CREATE = "create"
        const val DELETE = "delete"
        const val UPDATE = "update"
        const val START = "start"
        const val STOP = "stop"
        const val RESTART = "restart"
        const val LOGIN = "login"
        const val LOGOUT = "logout"
        const val INFO = "info"
        const val QUERY = "query"
        const val DOWNLOAD = "download"
        const val UPLOAD = "upload"
        const val COPY = "copy"
        const val MOVE = "move"
        const val RENAME = "rename"
        const val EXTRACT = "extract"
        const val COMPRESS = "compress"
        const val SEARCH = "search"
        const val SHARE = "share"
        const val ENCRYPT = "encrypt"
        const val DECRYPT = "decrypt"
        const val STATUS = "status"
        const val CHECK = "check"
        const val REQUEST = "request"
        const val GET_STATUS = "getstatus"
        const val GET_INFO = "getinfo"
        const val SET = "set"
        const val GET_CONFIG = "getconfig"
        const val SET_CONFIG = "setconfig"
        const val CLEAR = "clear"
        const val GET_ITEM = "getitem"
        const val SET_ITEM = "setitem"
        const val DEL_ITEM = "delitem"
    }
    
    // 默认版本号
    object Version {
        const val V1 = "1"
        const val V2 = "2"
        const val V3 = "3"
        const val V4 = "4"
        const val V5 = "5"
        const val V6 = "6"
        const val V7 = "7"
        
        // 特定 API 版本
        const val AUTH = "3"
        const val FILE_LIST = "2"
        const val FILE_SHARE = "3"
        const val DOWNLOAD_TASK = "3"
        const val AUDIO_SONG = "3"
        const val PHOTO = "1"
    }
}
