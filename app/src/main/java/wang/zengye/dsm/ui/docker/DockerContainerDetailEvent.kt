package wang.zengye.dsm.ui.docker

import wang.zengye.dsm.ui.base.BaseEvent

sealed class DockerContainerDetailEvent : BaseEvent {
    data class ShowError(val message: String) : DockerContainerDetailEvent()
}
