package wang.zengye.dsm.ui.docker

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * Docker 容器管理 Event
 */
sealed class DockerEvent : BaseEvent {
    data class ShowError(val message: String) : DockerEvent()
    data class ShowSuccess(val message: String) : DockerEvent()
}
