package wang.zengye.dsm.ui.virtual_machine

import wang.zengye.dsm.ui.base.BaseEvent

/**
 * 虚拟机管理 Event
 */
sealed class VirtualMachineEvent : BaseEvent {
    data class ShowError(val message: String) : VirtualMachineEvent()
    data object OperationSuccess : VirtualMachineEvent()
}
