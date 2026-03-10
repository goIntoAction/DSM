package wang.zengye.dsm.util

import org.junit.Assert.*
import org.junit.Test

/**
 * Extensions 单元测试
 * 
 * 注意：部分扩展函数依赖 Android Context（如 base64Encode、formatUptime），
 * 这些需要在 Android Instrumented Test 中测试
 */
class ExtensionsTest {

    // ========== formatSize 测试 ==========

    @Test
    fun formatSize_with0Bytes_returns0B() {
        assertEquals("0 B", formatSize(0))
    }

    @Test
    fun formatSize_with1023Bytes_returnsBytes() {
        assertEquals("1023 B", formatSize(1023))
    }

    @Test
    fun formatSize_with1024Bytes_returns1KB() {
        assertEquals("1 KB", formatSize(1024))
    }

    @Test
    fun formatSize_with1MB() {
        assertEquals("1 MB", formatSize(1024 * 1024))
    }

    @Test
    fun formatSize_with1GB() {
        assertEquals("1 GB", formatSize(1024L * 1024 * 1024))
    }

    @Test
    fun formatSize_withLargeTBValue() {
        val tb = 1024L * 1024 * 1024 * 1024
        assertEquals("1 TB", formatSize(tb))
    }

    @Test
    fun formatSize_withDecimalValue() {
        // 1536 bytes = 1.5 KB
        assertEquals("1.5 KB", formatSize(1536))
    }

    // ========== formatDuration 测试 ==========

    @Test
    fun formatDuration_with0Seconds_returns0000() {
        assertEquals("00:00", formatDuration(0))
    }

    @Test
    fun formatDuration_withNegative_returns0000() {
        assertEquals("00:00", formatDuration(-1))
    }

    @Test
    fun formatDuration_withSecondsOnly() {
        assertEquals("00:45", formatDuration(45))
    }

    @Test
    fun formatDuration_withMinutesAndSeconds() {
        assertEquals("02:05", formatDuration(125))  // 2分5秒
    }

    @Test
    fun formatDuration_withHours() {
        assertEquals("01:01:01", formatDuration(3661))  // 1小时1分1秒
    }

    @Test
    fun formatDuration_withLargeHours() {
        assertEquals("10:00:00", formatDuration(36000))  // 10小时
    }

    // ========== String extensions 测试 ==========

    @Test
    fun isBlank_withNull_returnsTrue() {
        val str: String? = null
        assertTrue(str.isBlank)
    }

    @Test
    fun isBlank_withEmpty_returnsTrue() {
        val str: String? = ""
        assertTrue(str.isBlank)
    }

    @Test
    fun isBlank_withWhitespace_returnsTrue() {
        val str: String? = "   "
        assertTrue(str.isBlank)
    }

    @Test
    fun isBlank_withContent_returnsFalse() {
        val str: String? = "hello"
        assertFalse(str.isBlank)
    }

    @Test
    fun isNotBlank_withNull_returnsFalse() {
        val str: String? = null
        assertFalse(str.isNotBlank)
    }

    @Test
    fun isNotBlank_withContent_returnsTrue() {
        val str: String? = "hello"
        assertTrue(str.isNotBlank)
    }

    // ========== formatDateTime 测试 ==========

    @Test
    fun formatDateTime_withEpoch_returns1970() {
        val result = formatDateTime(0)
        assertTrue(result.startsWith("1970-01-01"))
    }

    @Test
    fun formatDateTime_withValidTimestamp_returnsFormattedString() {
        val result = formatDateTime(1709510400) // 2024-03-04 00:00:00 UTC
        assertTrue(result.contains("-"))
        assertTrue(result.contains(":"))
    }

    // ========== formatDate 测试 ==========

    @Test
    fun formatDate_withEpoch_returns1970() {
        val result = formatDate(0)
        assertEquals("1970-01-01", result)
    }

    @Test
    fun formatDate_returnsDateOnly() {
        val result = formatDate(1709510400)
        assertFalse(result.contains(":"))
    }

    // ========== toDate 测试 ==========

    @Test
    fun intToDate_returnsCorrectDate() {
        val timestamp = 1709510400
        val date = timestamp.toDate()
        assertEquals(timestamp * 1000L, date.time)
    }

    @Test
    fun longToDate_returnsCorrectDate() {
        val timestamp = 1709510400L
        val date = timestamp.toDate()
        assertEquals(timestamp * 1000L, date.time)
    }
    
    // ========== 边界情况测试 ==========

    @Test
    fun formatSize_withVeryLargeValue() {
        val pb = 1024L * 1024 * 1024 * 1024 * 1024 // 1 PB
        val result = formatSize(pb)
        assertTrue(result.contains("PB"))
    }

    @Test
    fun formatDuration_boundaryValues() {
        assertEquals("00:59", formatDuration(59))
        assertEquals("01:00", formatDuration(60))
        assertEquals("00:00", formatDuration(0))
    }

    @Test
    fun formatDateTime_formatIsCorrect() {
        val result = formatDateTime(1709510400)
        // 验证格式为 yyyy-MM-dd HH:mm:ss
        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")))
    }
}
