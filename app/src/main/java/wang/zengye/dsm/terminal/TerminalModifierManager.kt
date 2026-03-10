package wang.zengye.dsm.terminal

import org.connectbot.terminal.ModifierManager

/**
 * 终端修饰键管理器
 * 实现 ModifierManager 接口以支持 Ctrl/Alt/Shift 修饰键状态管理
 * 参考 ConnectBot 的 TerminalKeyListener 实现
 */
class TerminalModifierManager : ModifierManager {

    // 修饰键状态位
    private var ourMetaState: Int = 0

    companion object {
        private const val OUR_CTRL_ON = 0x01
        private const val OUR_CTRL_LOCK = 0x02
        private const val OUR_ALT_ON = 0x04
        private const val OUR_ALT_LOCK = 0x08
        private const val OUR_SHIFT_ON = 0x10
        private const val OUR_SHIFT_LOCK = 0x20

        private const val OUR_TRANSIENT = OUR_CTRL_ON or OUR_ALT_ON or OUR_SHIFT_ON
        private const val OUR_CTRL_MASK = OUR_CTRL_ON or OUR_CTRL_LOCK
        private const val OUR_ALT_MASK = OUR_ALT_ON or OUR_ALT_LOCK
        private const val OUR_SHIFT_MASK = OUR_SHIFT_ON or OUR_SHIFT_LOCK
    }

    /**
     * 检查 Ctrl 修饰键是否激活（临时或锁定）
     */
    override fun isCtrlActive(): Boolean = (ourMetaState and OUR_CTRL_MASK) != 0

    /**
     * 检查 Alt 修饰键是否激活（临时或锁定）
     */
    override fun isAltActive(): Boolean = (ourMetaState and OUR_ALT_MASK) != 0

    /**
     * 检查 Shift 修饰键是否激活（临时或锁定）
     */
    override fun isShiftActive(): Boolean = (ourMetaState and OUR_SHIFT_MASK) != 0

    /**
     * 清除临时修饰键状态（按键后调用）
     */
    override fun clearTransients() {
        ourMetaState = ourMetaState and OUR_TRANSIENT.inv()
    }

    // ── 公开 API：用于 UI 按钮控制修饰键状态 ───────────────────────────────────

    /**
     * 切换 Ctrl 键状态
     * @param sticky 是否为粘性模式（按一次激活，再一次锁定）
     */
    fun toggleCtrl(sticky: Boolean = false) {
        if ((ourMetaState and OUR_CTRL_LOCK) != 0) {
            // 已锁定 -> 关闭
            ourMetaState = ourMetaState and OUR_CTRL_LOCK.inv()
        } else if ((ourMetaState and OUR_CTRL_ON) != 0) {
            // 已激活 -> 锁定（如果支持粘性）或关闭
            if (sticky) {
                ourMetaState = ourMetaState and OUR_CTRL_ON.inv()
                ourMetaState = ourMetaState or OUR_CTRL_LOCK
            } else {
                ourMetaState = ourMetaState and OUR_CTRL_ON.inv()
            }
        } else {
            // 未激活 -> 激活
            ourMetaState = ourMetaState or OUR_CTRL_ON
        }
    }

    /**
     * 切换 Alt 键状态
     */
    fun toggleAlt(sticky: Boolean = false) {
        if ((ourMetaState and OUR_ALT_LOCK) != 0) {
            ourMetaState = ourMetaState and OUR_ALT_LOCK.inv()
        } else if ((ourMetaState and OUR_ALT_ON) != 0) {
            if (sticky) {
                ourMetaState = ourMetaState and OUR_ALT_ON.inv()
                ourMetaState = ourMetaState or OUR_ALT_LOCK
            } else {
                ourMetaState = ourMetaState and OUR_ALT_ON.inv()
            }
        } else {
            ourMetaState = ourMetaState or OUR_ALT_ON
        }
    }

    /**
     * 切换 Shift 键状态
     */
    fun toggleShift(sticky: Boolean = false) {
        if ((ourMetaState and OUR_SHIFT_LOCK) != 0) {
            ourMetaState = ourMetaState and OUR_SHIFT_LOCK.inv()
        } else if ((ourMetaState and OUR_SHIFT_ON) != 0) {
            if (sticky) {
                ourMetaState = ourMetaState and OUR_SHIFT_ON.inv()
                ourMetaState = ourMetaState or OUR_SHIFT_LOCK
            } else {
                ourMetaState = ourMetaState and OUR_SHIFT_ON.inv()
            }
        } else {
            ourMetaState = ourMetaState or OUR_SHIFT_ON
        }
    }

    /**
     * 获取当前修饰键状态（用于 UI 显示）
     */
    fun getModifierState(): ModifierState = ModifierState(
        ctrlState = when {
            (ourMetaState and OUR_CTRL_LOCK) != 0 -> ModifierLevel.LOCKED
            (ourMetaState and OUR_CTRL_ON) != 0 -> ModifierLevel.TRANSIENT
            else -> ModifierLevel.OFF
        },
        altState = when {
            (ourMetaState and OUR_ALT_LOCK) != 0 -> ModifierLevel.LOCKED
            (ourMetaState and OUR_ALT_ON) != 0 -> ModifierLevel.TRANSIENT
            else -> ModifierLevel.OFF
        },
        shiftState = when {
            (ourMetaState and OUR_SHIFT_LOCK) != 0 -> ModifierLevel.LOCKED
            (ourMetaState and OUR_SHIFT_ON) != 0 -> ModifierLevel.TRANSIENT
            else -> ModifierLevel.OFF
        }
    )
}

/**
 * 修饰键状态
 */
data class ModifierState(
    val ctrlState: ModifierLevel,
    val altState: ModifierLevel,
    val shiftState: ModifierLevel
)

/**
 * 修饰键级别
 */
enum class ModifierLevel {
    OFF,        // 关闭
    TRANSIENT,  // 临时激活（按一次生效）
    LOCKED      // 锁定（持续激活）
}
