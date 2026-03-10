package wang.zengye.dsm.ui.docker

import wang.zengye.dsm.ui.base.BaseIntent

/**
 * Docker 容器管理 Intent
 */
sealed class DockerIntent : BaseIntent {
    data object LoadContainers : DockerIntent()
    data object Refresh : DockerIntent()
    data class StartContainer(val name: String) : DockerIntent()
    data class StopContainer(val name: String) : DockerIntent()
    data class RestartContainer(val name: String) : DockerIntent()
    data object ClearOperationMessage : DockerIntent()
}
