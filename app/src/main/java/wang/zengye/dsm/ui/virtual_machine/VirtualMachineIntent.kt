package wang.zengye.dsm.ui.virtual_machine

import wang.zengye.dsm.ui.base.BaseIntent

/**
 * 虚拟机管理 Intent
 */
sealed class VirtualMachineIntent : BaseIntent {
    data object LoadVms : VirtualMachineIntent()
    data class StartVm(val vm: VmItem) : VirtualMachineIntent()
    data class StopVm(val vm: VmItem, val force: Boolean = false) : VirtualMachineIntent()
    data class RestartVm(val vm: VmItem) : VirtualMachineIntent()
}
