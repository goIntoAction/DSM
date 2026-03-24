package wang.zengye.dsm.data.model.docker

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Docker 镜像项
 */
@JsonClass(generateAdapter = true)
data class DockerImageItemDto(
    @Json(name = "id") val id: String?,
    @Json(name = "repository") val repository: String?,
    @Json(name = "tags") val tags: List<String>?,
    @Json(name = "size") val size: Long?,
    @Json(name = "created") val created: Long?,
    @Json(name = "is_dsm") val isDsm: Boolean?
) {
    // 兼容旧代码
    val repoTags: List<String>? get() = tags?.map { if (repository != null) "$repository:$it" else it }
}

/**
 * Docker 镜像列表数据
 */
@JsonClass(generateAdapter = true)
data class DockerImageDataDto(
    @Json(name = "images") val images: List<DockerImageItemDto>?,
    @Json(name = "total") val total: Int?,
    @Json(name = "offset") val offset: Int?,
    @Json(name = "limit") val limit: Int?
)

/**
 * Docker 镜像列表响应
 * API: SYNO.Docker.Image
 * Method: list
 */
@JsonClass(generateAdapter = true)
data class DockerImageListDto(
    @Json(name = "data") val data: DockerImageDataDto?,
    @Json(name = "success") val success: Boolean?
) {
    // 兼容旧代码，直接访问 images
    val images: List<DockerImageItemDto>? get() = data?.images
}

/**
 * Docker 删除镜像响应
 * API: SYNO.Docker.Image
 * Method: delete
 */
@JsonClass(generateAdapter = true)
data class DockerDeleteImageDto(
    @Json(name = "success") val success: Boolean?
)

// ============== 容器相关模型 ==============

/**
 * Docker 容器网络信息
 */
@JsonClass(generateAdapter = true)
data class DockerContainerNetwork(
    @Json(name = "ip_address") val ipAddress: String?,
    @Json(name = "gateway") val gateway: String?,
    @Json(name = "mac_address") val macAddress: String?
)

/**
 * Docker 容器网络设置
 */
@JsonClass(generateAdapter = true)
data class DockerContainerNetworkSettings(
    @Json(name = "bridge") val bridge: DockerContainerNetwork?
)

/**
 * Docker 容器项（用于列表）
 */
@JsonClass(generateAdapter = true)
data class DockerContainerItem(
    @Json(name = "name") val name: String?,
    @Json(name = "status") val status: String?,
    @Json(name = "image") val image: String?,
    @Json(name = "created") val created: Long?,
    @Json(name = "network_settings") val networkSettings: DockerContainerNetworkSettings?
)

/**
 * Docker 容器列表数据（单个 API 响应）
 */
@JsonClass(generateAdapter = true)
data class DockerContainerListDataDto(
    @Json(name = "containers") val containers: List<DockerContainerItem>?
)

/**
 * Docker 容器列表单个结果
 */
@JsonClass(generateAdapter = true)
data class DockerContainerResult(
    @Json(name = "success") val success: Boolean?,
    @Json(name = "api") val api: String?,
    @Json(name = "data") val data: DockerContainerListDataDto?
)

/**
 * Docker 容器列表响应（批量请求）
 * API: SYNO.Docker.Container (batch)
 */
@JsonClass(generateAdapter = true)
data class DockerContainerListDto(
    @Json(name = "data") val data: DockerContainerListDataDto?,
    @Json(name = "result") val result: List<DockerContainerResult>?
)

// ============== 容器详情相关模型 ==============

/**
 * 容器端口绑定
 */
@JsonClass(generateAdapter = true)
data class DockerPortBinding(
    @Json(name = "host_port") val hostPort: String?,
    @Json(name = "container_port") val containerPort: String?,
    @Json(name = "type") val type: String?
)

/**
 * 容器卷绑定
 */
@JsonClass(generateAdapter = true)
data class DockerVolumeBinding(
    @Json(name = "host_volume_file") val hostVolumeFile: String?,
    @Json(name = "mount_point") val mountPoint: String?,
    @Json(name = "type") val type: String?
)

/**
 * 容器链接
 */
@JsonClass(generateAdapter = true)
data class DockerContainerLink(
    @Json(name = "link_container") val linkContainer: String?,
    @Json(name = "alias") val alias: String?
)

/**
 * 容器网络信息
 */
@JsonClass(generateAdapter = true)
data class DockerContainerNetworkInfo(
    @Json(name = "name") val name: String?,
    @Json(name = "driver") val driver: String?
)

/**
 * 环境变量
 */
@JsonClass(generateAdapter = true)
data class DockerEnvVariable(
    @Json(name = "key") val key: String?,
    @Json(name = "value") val value: String?
)

/**
 * 快捷方式
 */
@JsonClass(generateAdapter = true)
data class DockerShortcut(
    @Json(name = "enable_shortcut") val enableShortcut: Boolean?,
    @Json(name = "enable_web_page") val enableWebPage: Boolean?,
    @Json(name = "web_page_url") val webPageUrl: String?
)

/**
 * 容器配置（profile）
 */
@JsonClass(generateAdapter = true)
data class DockerContainerProfile(
    @Json(name = "memory_limit") val memoryLimit: Long?,
    @Json(name = "cpu_priority") val cpuPriority: Int?,
    @Json(name = "shortcut") val shortcut: DockerShortcut?,
    @Json(name = "port_bindings") val portBindings: List<DockerPortBinding>?,
    @Json(name = "volume_bindings") val volumeBindings: List<DockerVolumeBinding>?,
    @Json(name = "links") val links: List<DockerContainerLink>?,
    @Json(name = "network") val network: List<DockerContainerNetworkInfo>?,
    @Json(name = "env_variables") val envVariables: List<DockerEnvVariable>?
)

/**
 * 容器详情（details）
 */
@JsonClass(generateAdapter = true)
data class DockerContainerDetails(
    @Json(name = "status") val status: String?,
    @Json(name = "up_time") val upTime: Long?,
    @Json(name = "exe_cmd") val exeCmd: String?,
    @Json(name = "memoryPercent") val memoryPercent: Double?
)

/**
 * 容器进程
 */
@JsonClass(generateAdapter = true)
data class DockerContainerProcess(
    @Json(name = "pid") val pid: Int?,
    @Json(name = "cpu") val cpu: Double?,
    @Json(name = "memory") val memory: Long?,
    @Json(name = "command") val command: String?
)

/**
 * 容器日志
 */
@JsonClass(generateAdapter = true)
data class DockerContainerLog(
    @Json(name = "stream") val stream: String?,
    @Json(name = "created") val created: String?,
    @Json(name = "text") val text: String?
)

/**
 * 容器详情响应
 * API: SYNO.Docker.Container
 * Method: get
 */
@JsonClass(generateAdapter = true)
data class DockerContainerDetailDto(
    @Json(name = "data") val data: DockerContainerDetailDataDto?
)

/**
 * 容器详情数据
 */
@JsonClass(generateAdapter = true)
data class DockerContainerDetailDataDto(
    @Json(name = "details") val details: DockerContainerDetails?,
    @Json(name = "profile") val profile: DockerContainerProfile?,
    @Json(name = "processes") val processes: List<DockerContainerProcess>?
)

// ============== 容器操作响应 ==============

/**
 * 容器操作响应（start/stop/restart 等）
 */
@JsonClass(generateAdapter = true)
data class DockerContainerOperationDto(
    @Json(name = "success") val success: Boolean?
)

// ============== 容器日志相关模型 ==============

/**
 * 容器日志响应
 * API: SYNO.Docker.Container.Log
 * Method: get
 */
@JsonClass(generateAdapter = true)
data class DockerContainerLogDto(
    @Json(name = "data") val data: DockerContainerLogDataDto?
)

@JsonClass(generateAdapter = true)
data class DockerContainerLogDataDto(
    @Json(name = "logs") val logs: List<DockerContainerLog>?
)

/**
 * 容器日志列表响应
 * API: SYNO.Docker.Container.Log
 * Method: list
 */
@JsonClass(generateAdapter = true)
data class DockerContainerLogListDto(
    @Json(name = "data") val data: DockerContainerLogListDataDto?
)

@JsonClass(generateAdapter = true)
data class DockerContainerLogListDataDto(
    @Json(name = "dates") val dates: List<String>?
)

// ============== 网络相关模型 ==============

/**
 * 网络中的容器
 */
@JsonClass(generateAdapter = true)
data class DockerNetworkContainer(
    @Json(name = "name") val name: String?,
    @Json(name = "endpoint_id") val endpointId: String?,
    @Json(name = "mac_address") val macAddress: String?
)

/**
 * 网络 IPAM 配置
 */
@JsonClass(generateAdapter = true)
data class DockerNetworkIpamConfig(
    @Json(name = "subnet") val subnet: String?,
    @Json(name = "gateway") val gateway: String?
)

/**
 * 网络 IPAM
 */
@JsonClass(generateAdapter = true)
data class DockerNetworkIpam(
    @Json(name = "config") val config: DockerNetworkIpamConfig?
)

/**
 * Docker 网络项
 */
@JsonClass(generateAdapter = true)
data class DockerNetworkItem(
    @Json(name = "id") val id: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "driver") val driver: String?,
    @Json(name = "scope") val scope: String?,
    @Json(name = "ipam") val ipam: DockerNetworkIpam?,
    @Json(name = "created") val created: String?,
    @Json(name = "enable_ipv6") val enableIpv6: Boolean?,
    @Json(name = "internal") val internal: Boolean?,
    @Json(name = "attachable") val attachable: Boolean?,
    @Json(name = "containers") val containers: Map<String, DockerNetworkContainer>?
)

/**
 * Docker 网络列表响应
 * API: SYNO.Docker.Network
 * Method: list
 */
@JsonClass(generateAdapter = true)
data class DockerNetworkListDto(
    @Json(name = "data") val data: DockerNetworkListDataDto?
)

@JsonClass(generateAdapter = true)
data class DockerNetworkListDataDto(
    @Json(name = "networks") val networks: List<DockerNetworkItem>?
)

/**
 * 网络操作响应（create/delete）
 */
@JsonClass(generateAdapter = true)
data class DockerNetworkOperationDto(
    @Json(name = "success") val success: Boolean?
)

// ============== 注册表相关模型 ==============

/**
 * 注册表项
 */
@JsonClass(generateAdapter = true)
data class DockerRegistryItem(
    @Json(name = "id") val id: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "url") val url: String?,
    @Json(name = "username") val username: String?,
    @Json(name = "enable") val enable: Boolean?
)

/**
 * 注册表列表响应
 * API: SYNO.Docker.Registry
 * Method: get
 */
@JsonClass(generateAdapter = true)
data class DockerRegistryListDto(
    @Json(name = "data") val data: DockerRegistryListDataDto?
)

@JsonClass(generateAdapter = true)
data class DockerRegistryListDataDto(
    @Json(name = "registries") val registries: List<DockerRegistryItem>?
)
