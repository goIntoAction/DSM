package wang.zengye.dsm.ui.base

/**
 * 所有 UI 状态的基接口
 * 提供通用的加载状态和错误信息属性
 */
interface BaseState {
    val isLoading: Boolean
    val error: String?
}
