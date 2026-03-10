package wang.zengye.dsm.terminal

import android.util.Log
import java.util.regex.Pattern

/**
 * 终端仿真器
 * 处理 ANSI 转义序列和终端状态
 */
class TerminalEmulator(
    private val cols: Int = 80,
    private val rows: Int = 24
) {
    companion object {
        private const val TAG = "TerminalEmulator"
        
        // ANSI 转义序列正则
        private val ANSI_ESCAPE = Pattern.compile("\u001B\\[[\\d;]*[a-zA-Z]")
        private val ANSI_COLOR = Pattern.compile("\u001B\\[([\\d;]*)m")
        private val ANSI_CURSOR = Pattern.compile("\u001B\\[([\\d]*)([ABCDEFGHJKSTf])")
        private val ANSI_CLEAR = Pattern.compile("\u001B\\[([\\d]*)([JK])")
        
        // 标准 ANSI 颜色
        private val COLORS = mapOf(
            30 to "#000000", // Black
            31 to "#CD0000", // Red
            32 to "#00CD00", // Green
            33 to "#CDCD00", // Yellow
            34 to "#0000EE", // Blue
            35 to "#CD00CD", // Magenta
            36 to "#00CDCD", // Cyan
            37 to "#E5E5E5", // White
            // 亮色
            90 to "#4C4C4C",  // Bright Black (Gray)
            91 to "#FF0000",  // Bright Red
            92 to "#00FF00",  // Bright Green
            93 to "#FFFF00",  // Bright Yellow
            94 to "#5C5CFF",  // Bright Blue
            95 to "#FF00FF",  // Bright Magenta
            96 to "#00FFFF",  // Bright Cyan
            97 to "#FFFFFF"   // Bright White
        )
    }
    
    // 终端缓冲区
    private val buffer = StringBuilder()
    
    // 光标位置
    private var cursorX = 0
    private var cursorY = 0
    
    // 当前样式
    private var currentFgColor: String? = null
    private var currentBgColor: String? = null
    private var isBold = false
    private var isUnderline = false
    private var isItalic = false
    
    /**
     * 处理输入数据
     * @return 处理后的文本（带样式标记）
     */
    fun process(data: String): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        var remaining = data
        
        while (remaining.isNotEmpty()) {
            when {
                // 处理回车
                remaining.startsWith("\r") -> {
                    cursorX = 0
                    remaining = remaining.substring(1)
                }
                // 处理换行
                remaining.startsWith("\n") -> {
                    cursorY++
                    if (cursorY >= rows) {
                        scrollUp()
                        cursorY = rows - 1
                    }
                    remaining = remaining.substring(1)
                }
                // 处理退格
                remaining.startsWith("\b") -> {
                    if (cursorX > 0) cursorX--
                    remaining = remaining.substring(1)
                }
                // 处理制表符
                remaining.startsWith("\t") -> {
                    cursorX = ((cursorX / 8) + 1) * 8
                    if (cursorX >= cols) cursorX = cols - 1
                    remaining = remaining.substring(1)
                }
                // 处理 ANSI 转义序列
                remaining.startsWith("\u001B") -> {
                    remaining = processEscapeSequence(remaining)
                }
                // 普通字符
                else -> {
                    val char = remaining[0]
                    appendChar(char)
                    remaining = remaining.substring(1)
                }
            }
        }
        
        return lines
    }
    
    /**
     * 处理 ANSI 转义序列
     */
    private fun processEscapeSequence(data: String): String {
        // CSI 序列 (ESC [)
        if (data.startsWith("\u001B[")) {
            // 颜色序列
            val colorMatcher = ANSI_COLOR.matcher(data)
            if (colorMatcher.lookingAt()) {
                val params = colorMatcher.group(1) ?: ""
                processColorSequence(params)
                return data.substring(colorMatcher.end())
            }
            
            // 光标移动
            val cursorMatcher = ANSI_CURSOR.matcher(data)
            if (cursorMatcher.lookingAt()) {
                val n = (cursorMatcher.group(1) ?: "1").toIntOrNull() ?: 1
                val cmd = cursorMatcher.group(2)
                processCursorCommand(cmd, n)
                return data.substring(cursorMatcher.end())
            }
            
            // 清屏
            val clearMatcher = ANSI_CLEAR.matcher(data)
            if (clearMatcher.lookingAt()) {
                val n = (clearMatcher.group(1) ?: "0").toIntOrNull() ?: 0
                val cmd = clearMatcher.group(2)
                processClearCommand(cmd, n)
                return data.substring(clearMatcher.end())
            }
            
            // 通用 CSI 序列 - 跳过
            val generalMatcher = ANSI_ESCAPE.matcher(data)
            if (generalMatcher.lookingAt()) {
                return data.substring(generalMatcher.end())
            }
        }
        
        // 其他转义序列 - 跳过
        if (data.length >= 2) {
            return data.substring(2)
        }
        return ""
    }
    
    /**
     * 处理颜色序列
     */
    private fun processColorSequence(params: String) {
        if (params.isEmpty() || params == "0") {
            // 重置所有样式
            currentFgColor = null
            currentBgColor = null
            isBold = false
            isUnderline = false
            isItalic = false
            return
        }
        
        val codes = params.split(";").mapNotNull { it.toIntOrNull() }
        var i = 0
        while (i < codes.size) {
            when (val code = codes[i]) {
                0 -> {
                    currentFgColor = null
                    currentBgColor = null
                    isBold = false
                    isUnderline = false
                    isItalic = false
                }
                1 -> isBold = true
                3 -> isItalic = true
                4 -> isUnderline = true
                22 -> isBold = false
                23 -> isItalic = false
                24 -> isUnderline = false
                in 30..37 -> currentFgColor = COLORS[code]
                in 90..97 -> currentFgColor = COLORS[code]
                38 -> {
                    // 扩展前景色
                    if (i + 2 < codes.size && codes[i + 1] == 5) {
                        // 256 色
                        currentFgColor = get256Color(codes[i + 2])
                        i += 2
                    } else if (i + 4 < codes.size && codes[i + 1] == 2) {
                        // RGB 色
                        currentFgColor = getRgbColor(codes[i + 2], codes[i + 3], codes[i + 4])
                        i += 4
                    }
                }
                39 -> currentFgColor = null
                in 40..47 -> currentBgColor = COLORS[code - 10]
                in 100..107 -> currentBgColor = COLORS[code]
                48 -> {
                    // 扩展背景色
                    if (i + 2 < codes.size && codes[i + 1] == 5) {
                        currentBgColor = get256Color(codes[i + 2])
                        i += 2
                    } else if (i + 4 < codes.size && codes[i + 1] == 2) {
                        currentBgColor = getRgbColor(codes[i + 2], codes[i + 3], codes[i + 4])
                        i += 4
                    }
                }
                49 -> currentBgColor = null
            }
            i++
        }
    }
    
    /**
     * 处理光标命令
     */
    private fun processCursorCommand(cmd: String, n: Int) {
        when (cmd) {
            "A" -> cursorY = maxOf(0, cursorY - n)
            "B" -> cursorY = minOf(rows - 1, cursorY + n)
            "C" -> cursorX = minOf(cols - 1, cursorX + n)
            "D" -> cursorX = maxOf(0, cursorX - n)
            "H", "f" -> {
                cursorX = 0
                cursorY = 0
            }
        }
    }
    
    /**
     * 处理清屏命令
     */
    private fun processClearCommand(cmd: String, n: Int) {
        when (cmd) {
            "J" -> {
                // 清屏
                when (n) {
                    0 -> clearFromCursorToEnd()
                    1 -> clearFromStartToCursor()
                    2 -> clearScreen()
                }
            }
            "K" -> {
                // 清行
                when (n) {
                    0 -> clearLineFromCursorToEnd()
                    1 -> clearLineFromStartToCursor()
                    2 -> clearLine()
                }
            }
        }
    }
    
    private fun appendChar(char: Char) {
        if (cursorX >= cols) {
            cursorX = 0
            cursorY++
            if (cursorY >= rows) {
                scrollUp()
                cursorY = rows - 1
            }
        }
        
        // 简化处理：直接追加到缓冲区
        buffer.append(char)
        cursorX++
    }
    
    private fun scrollUp() {
        // 简化处理
    }
    
    private fun clearScreen() {
        buffer.clear()
    }
    
    private fun clearFromCursorToEnd() {}
    private fun clearFromStartToCursor() {}
    private fun clearLine() {}
    private fun clearLineFromCursorToEnd() {}
    private fun clearLineFromStartToCursor() {}
    
    /**
     * 获取 256 色值
     */
    private fun get256Color(index: Int): String {
        if (index < 16) {
            return COLORS[index + if (index < 8) 30 else 82] ?: "#FFFFFF"
        } else if (index < 232) {
            val n = index - 16
            val r = (n / 36) * 51
            val g = ((n % 36) / 6) * 51
            val b = (n % 6) * 51
            return "#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}"
        } else {
            val gray = (index - 232) * 10 + 8
            return "#${gray.toString(16).padStart(2, '0').repeat(3)}"
        }
    }
    
    /**
     * 获取 RGB 颜色值
     */
    private fun getRgbColor(r: Int, g: Int, b: Int): String {
        return "#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}"
    }
    
    /**
     * 获取缓冲区内容
     */
    fun getBuffer(): String = buffer.toString()
    
    /**
     * 清空缓冲区
     */
    fun clear() {
        buffer.clear()
        cursorX = 0
        cursorY = 0
    }
    
    /**
     * 获取当前样式
     */
    fun getCurrentStyle(): TerminalStyle {
        return TerminalStyle(
            fgColor = currentFgColor,
            bgColor = currentBgColor,
            bold = isBold,
            underline = isUnderline,
            italic = isItalic
        )
    }
}

/**
 * 终端样式
 */
data class TerminalStyle(
    val fgColor: String? = null,
    val bgColor: String? = null,
    val bold: Boolean = false,
    val underline: Boolean = false,
    val italic: Boolean = false
)

/**
 * 终端行
 */
data class TerminalLine(
    val text: String,
    val style: TerminalStyle = TerminalStyle()
)
