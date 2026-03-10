package wang.zengye.dsm.data.model

import org.junit.Assert.*
import org.junit.Test

class PackageInfoTest {

    // ========== 基本属性测试 ==========

    @Test
    fun packageInfo_hasCorrectId() {
        val info = createPackageInfo(id = "synology-drive")
        assertEquals("synology-drive", info.id)
    }

    @Test
    fun packageInfo_hasCorrectName() {
        val info = createPackageInfo(name = "Synology Drive")
        assertEquals("Synology Drive", info.name)
    }

    @Test
    fun packageInfo_hasCorrectDisplayName() {
        val info = createPackageInfo(displayName = "Synology Drive Server")
        assertEquals("Synology Drive Server", info.displayName)
    }

    @Test
    fun packageInfo_hasCorrectVersion() {
        val info = createPackageInfo(version = "3.0.5")
        assertEquals("3.0.5", info.version)
    }

    @Test
    fun packageInfo_hasCorrectDescription() {
        val info = createPackageInfo(description = "File sync service")
        assertEquals("File sync service", info.description)
    }

    @Test
    fun packageInfo_hasCorrectUrl() {
        val info = createPackageInfo(url = "https://example.com")
        assertEquals("https://example.com", info.url)
    }

    @Test
    fun packageInfo_hasCorrectThumbnailUrl() {
        val info = createPackageInfo(thumbnailUrl = "https://example.com/icon.png")
        assertEquals("https://example.com/icon.png", info.thumbnailUrl)
    }

    // ========== isRunning 测试 ==========

    @Test
    fun isRunning_whenStatusIsRunning_returnsTrue() {
        val info = createPackageInfo(status = "running")
        assertTrue(info.isRunning)
    }

    @Test
    fun isRunning_whenStatusIsStopped_returnsFalse() {
        val info = createPackageInfo(status = "stopped")
        assertFalse(info.isRunning)
    }

    @Test
    fun isRunning_whenStatusIsUnknown_returnsFalse() {
        val info = createPackageInfo(status = "unknown")
        assertFalse(info.isRunning)
    }

    // ========== isStopped 测试 ==========

    @Test
    fun isStopped_whenStatusIsStopped_returnsTrue() {
        val info = createPackageInfo(status = "stopped")
        assertTrue(info.isStopped)
    }

    @Test
    fun isStopped_whenStatusIsRunning_returnsFalse() {
        val info = createPackageInfo(status = "running")
        assertFalse(info.isStopped)
    }

    @Test
    fun isStopped_whenStatusIsUnknown_returnsFalse() {
        val info = createPackageInfo(status = "unknown")
        assertFalse(info.isStopped)
    }

    // ========== statusText 测试 ==========

    @Test
    fun statusText_whenRunning_returnsChineseText() {
        val info = createPackageInfo(status = "running")
        assertEquals("运行中", info.statusText)
    }

    @Test
    fun statusText_whenStopped_returnsChineseText() {
        val info = createPackageInfo(status = "stopped")
        assertEquals("已停止", info.statusText)
    }

    @Test
    fun statusText_whenUnknown_returnsOriginalStatus() {
        val info = createPackageInfo(status = "installing")
        assertEquals("installing", info.statusText)
    }

    // ========== launchable 测试 ==========

    @Test
    fun launchable_whenTrue_returnsTrue() {
        val info = createPackageInfo(launchable = true)
        assertTrue(info.launchable)
    }

    @Test
    fun launchable_whenFalse_returnsFalse() {
        val info = createPackageInfo(launchable = false)
        assertFalse(info.launchable)
    }

    // ========== installed 测试 ==========

    @Test
    fun installed_whenTrue_returnsTrue() {
        val info = createPackageInfo(installed = true)
        assertTrue(info.installed)
    }

    @Test
    fun installed_whenFalse_returnsFalse() {
        val info = createPackageInfo(installed = false)
        assertFalse(info.installed)
    }

    // ========== 边界情况测试 ==========

    @Test
    fun packageInfo_withEmptyStrings() {
        val info = PackageInfo(
            id = "",
            name = "",
            displayName = "",
            version = "",
            description = "",
            status = "",
            url = "",
            launchable = false,
            installed = false,
            thumbnailUrl = ""
        )
        
        assertEquals("", info.id)
        assertEquals("", info.name)
        assertFalse(info.isRunning)
        assertFalse(info.isStopped)
        assertEquals("", info.statusText)
    }

    @Test
    fun packageInfo_withNullEquivalentValues() {
        val info = createPackageInfo(
            status = "",
            name = "",
            displayName = ""
        )
        
        assertEquals("", info.name)
        assertEquals("", info.statusText)
    }

    // ========== 辅助方法 ==========

    private fun createPackageInfo(
        id: String = "test-package",
        name: String = "Test Package",
        displayName: String = "Test Package Display",
        version: String = "1.0.0",
        description: String = "Test description",
        status: String = "running",
        url: String = "https://test.com",
        launchable: Boolean = true,
        installed: Boolean = true,
        thumbnailUrl: String = "https://test.com/icon.png"
    ) = PackageInfo(
        id = id,
        name = name,
        displayName = displayName,
        version = version,
        description = description,
        status = status,
        url = url,
        launchable = launchable,
        installed = installed,
        thumbnailUrl = thumbnailUrl
    )
}
