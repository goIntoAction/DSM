package wang.zengye.dsm.ui.docker

import wang.zengye.dsm.ui.base.BaseIntent

sealed class DockerContainerDetailIntent : BaseIntent {
    data class LoadDetail(val containerName: String) : DockerContainerDetailIntent()
    data class LoadLogs(val date: String) : DockerContainerDetailIntent()
    data object Refresh : DockerContainerDetailIntent()
}
