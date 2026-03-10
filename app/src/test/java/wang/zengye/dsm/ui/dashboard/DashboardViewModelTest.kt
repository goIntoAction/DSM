package wang.zengye.dsm.ui.dashboard

import org.junit.Assert.*
import org.junit.Test

/**
 * DashboardViewModel 单元测试
 * 
 * 主要测试 State、Intent、Event 的定义和基本逻辑
 */
class DashboardViewModelTest {

    // ========== SystemInfoUi 测试 ==========

    @Test
    fun systemInfoUi_defaultValues() {
        val info = SystemInfoUi()
        
        assertEquals("", info.model)
        assertEquals("", info.hostname)
        assertEquals("", info.uptime)
        assertEquals(0, info.temperature)
        assertFalse(info.temperatureWarning)
        assertEquals("", info.dsmVersion)
    }

    @Test
    fun systemInfoUi_withValues() {
        val info = SystemInfoUi(
            model = "DS920+",
            hostname = "NAS",
            uptime = "10 days",
            temperature = 45,
            temperatureWarning = false,
            dsmVersion = "7.2-64570"
        )
        
        assertEquals("DS920+", info.model)
        assertEquals("NAS", info.hostname)
        assertEquals("10 days", info.uptime)
        assertEquals(45, info.temperature)
        assertFalse(info.temperatureWarning)
        assertEquals("7.2-64570", info.dsmVersion)
    }

    @Test
    fun systemInfoUi_temperatureWarning() {
        val info = SystemInfoUi(
            temperature = 65,
            temperatureWarning = true
        )
        
        assertEquals(65, info.temperature)
        assertTrue(info.temperatureWarning)
    }

    // ========== UtilizationInfo 测试 ==========

    @Test
    fun utilizationInfo_defaultValues() {
        val info = UtilizationInfo()
        
        assertEquals(0, info.cpuUsage)
        assertEquals(0, info.memoryUsage)
        assertEquals(0L, info.memoryTotal)
        assertEquals(0L, info.memoryUsed)
        assertTrue(info.cpuHistory.isEmpty())
        assertTrue(info.memoryHistory.isEmpty())
    }

    @Test
    fun utilizationInfo_withValues() {
        val info = UtilizationInfo(
            cpuUsage = 25,
            memoryUsage = 60,
            memoryTotal = 16L * 1024 * 1024 * 1024, // 16GB
            memoryUsed = 10L * 1024 * 1024 * 1024   // 10GB
        )
        
        assertEquals(25, info.cpuUsage)
        assertEquals(60, info.memoryUsage)
        assertEquals(16L * 1024 * 1024 * 1024, info.memoryTotal)
        assertEquals(10L * 1024 * 1024 * 1024, info.memoryUsed)
    }

    @Test
    fun utilizationInfo_addHistoryPoint() {
        val info = UtilizationInfo()
        
        val updated = info.addHistoryPoint(30, 50)
        
        assertEquals(1, updated.cpuHistory.size)
        assertEquals(1, updated.memoryHistory.size)
        assertEquals(30, updated.cpuHistory[0])
        assertEquals(50, updated.memoryHistory[0])
    }

    @Test
    fun utilizationInfo_historyMaxSize() {
        var info = UtilizationInfo()
        
        // 添加超过最大数量的历史记录
        repeat(25) { i ->
            info = info.addHistoryPoint(i, i)
        }
        
        // 应该只保留最后 20 个
        assertEquals(20, info.cpuHistory.size)
        assertEquals(20, info.memoryHistory.size)
    }

    // ========== VolumeInfoUi 测试 ==========

    @Test
    fun volumeInfoUi_defaultValues() {
        val volume = VolumeInfoUi()
        
        assertEquals("", volume.id)
        assertEquals("", volume.name)
        assertEquals(0L, volume.totalSize)
        assertEquals(0L, volume.usedSize)
        assertEquals("", volume.status)
        assertEquals(0f, volume.usage, 0.001f)
    }

    @Test
    fun volumeInfoUi_usageCalculation() {
        val volume = VolumeInfoUi(
            totalSize = 1000L,
            usedSize = 500L
        )
        
        assertEquals(0.5f, volume.usage, 0.001f)
    }

    @Test
    fun volumeInfoUi_usageFromPercent() {
        val volume = VolumeInfoUi(
            totalSize = 0, // 无法计算时使用 usagePercent
            usagePercent = 75
        )
        
        assertEquals(0.75f, volume.usage, 0.001f)
    }

    @Test
    fun volumeInfoUi_fullVolume() {
        val volume = VolumeInfoUi(
            totalSize = 1000L,
            usedSize = 1000L
        )
        
        assertEquals(1.0f, volume.usage, 0.001f)
    }

    @Test
    fun volumeInfoUi_emptyVolume() {
        val volume = VolumeInfoUi(
            totalSize = 1000L,
            usedSize = 0L
        )
        
        assertEquals(0f, volume.usage, 0.001f)
    }

    // ========== DiskInfoUi 测试 ==========

    @Test
    fun diskInfoUi_defaultValues() {
        val disk = DiskInfoUi()
        
        assertEquals("", disk.id)
        assertEquals("", disk.name)
        assertEquals("", disk.model)
        assertEquals(0L, disk.totalSize)
        assertEquals(0, disk.temperature)
        assertEquals("", disk.status)
    }

    @Test
    fun diskInfoUi_withValues() {
        val disk = DiskInfoUi(
            id = "disk1",
            name = "Drive 1",
            model = "WD Red 4TB",
            serial = "WD-WCC4EXXXXX",
            totalSize = 4L * 1024 * 1024 * 1024 * 1024,
            temperature = 35,
            status = "normal",
            health = "good",
            smartStatus = "passed"
        )
        
        assertEquals("disk1", disk.id)
        assertEquals("WD Red 4TB", disk.model)
        assertEquals(35, disk.temperature)
        assertEquals("normal", disk.status)
    }

    // ========== NetworkInfo 测试 ==========

    @Test
    fun networkInfo_defaultValues() {
        val network = NetworkInfo()
        
        assertEquals("", network.device)
        assertEquals("", network.name)
        assertEquals(0L, network.rxSpeed)
        assertEquals(0L, network.txSpeed)
        assertEquals("", network.ip)
        assertTrue(network.rxHistory.isEmpty())
        assertTrue(network.txHistory.isEmpty())
    }

    @Test
    fun networkInfo_withValues() {
        val network = NetworkInfo(
            device = "eth0",
            name = "LAN 1",
            rxSpeed = 1024000,
            txSpeed = 512000,
            ip = "192.168.1.100"
        )
        
        assertEquals("eth0", network.device)
        assertEquals("LAN 1", network.name)
        assertEquals(1024000L, network.rxSpeed)
        assertEquals(512000L, network.txSpeed)
        assertEquals("192.168.1.100", network.ip)
    }

    @Test
    fun networkInfo_addHistoryPoint() {
        val network = NetworkInfo()
        
        val updated = network.addHistoryPoint(1000, 500)
        
        assertEquals(1, updated.rxHistory.size)
        assertEquals(1, updated.txHistory.size)
        assertEquals(1000L, updated.rxHistory[0])
        assertEquals(500L, updated.txHistory[0])
    }

    @Test
    fun networkInfo_historyMaxSize() {
        var network = NetworkInfo()
        
        repeat(25) { i ->
            network = network.addHistoryPoint(i.toLong(), i.toLong())
        }
        
        assertEquals(20, network.rxHistory.size)
        assertEquals(20, network.txHistory.size)
    }

    // ========== DashboardUiState 测试 ==========

    @Test
    fun dashboardUiState_defaultValues() {
        val state = DashboardUiState()
        
        assertTrue(state.isLoading)
        assertNull(state.error)
        assertTrue(state.volumes.isEmpty())
        assertTrue(state.disks.isEmpty())
        assertTrue(state.networks.isEmpty())
        assertEquals(0, state.connectedUsers)
        assertEquals(10, state.refreshDuration)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun dashboardUiState_withSystemInfo() {
        val state = DashboardUiState(
            systemInfo = SystemInfoUi(
                model = "DS920+",
                hostname = "MyNAS"
            )
        )
        
        assertEquals("DS920+", state.systemInfo.model)
        assertEquals("MyNAS", state.systemInfo.hostname)
    }

    @Test
    fun dashboardUiState_withVolumes() {
        val volumes = listOf(
            VolumeInfoUi(name = "Volume 1", totalSize = 1000, usedSize = 500),
            VolumeInfoUi(name = "Volume 2", totalSize = 2000, usedSize = 1000)
        )
        val state = DashboardUiState(volumes = volumes)
        
        assertEquals(2, state.volumes.size)
        assertEquals("Volume 1", state.volumes[0].name)
    }

    @Test
    fun dashboardUiState_withError() {
        val state = DashboardUiState(
            isLoading = false,
            error = "Connection failed"
        )
        
        assertFalse(state.isLoading)
        assertEquals("Connection failed", state.error)
    }

    @Test
    fun dashboardUiState_refreshing() {
        val state = DashboardUiState(isRefreshing = true)
        
        assertTrue(state.isRefreshing)
    }

    // ========== DashboardIntent 测试 ==========

    @Test
    fun dashboardIntent_types() {
        val loadData = DashboardIntent.LoadData
        assertTrue(loadData is DashboardIntent)

        val refresh = DashboardIntent.Refresh
        assertTrue(refresh is DashboardIntent)

        val shutdown = DashboardIntent.Shutdown
        assertTrue(shutdown is DashboardIntent)

        val reboot = DashboardIntent.Reboot
        assertTrue(reboot is DashboardIntent)
    }

    // ========== DashboardEvent 测试 ==========

    @Test
    fun dashboardEvent_types() {
        val error = DashboardEvent.Error("Error message")
        assertEquals("Error message", error.message)

        val shutdownSuccess = DashboardEvent.ShutdownSuccess
        assertTrue(shutdownSuccess is DashboardEvent)

        val rebootSuccess = DashboardEvent.RebootSuccess
        assertTrue(rebootSuccess is DashboardEvent)
    }

    // ========== 边界情况测试 ==========

    @Test
    fun utilizationInfo_cpuUsageRange() {
        // CPU 使用率应该在 0-100 范围
        val info1 = UtilizationInfo(cpuUsage = 0)
        assertEquals(0, info1.cpuUsage)

        val info2 = UtilizationInfo(cpuUsage = 100)
        assertEquals(100, info2.cpuUsage)

        val info3 = UtilizationInfo(cpuUsage = 50)
        assertEquals(50, info3.cpuUsage)
    }

    @Test
    fun volumeInfoUi_usagePercentClamped() {
        // usagePercent 应该被限制在 0-100 范围
        val volume = VolumeInfoUi(usagePercent = 150)
        assertEquals(1.0f, volume.usage, 0.001f) // 应该被限制为 1.0
    }

    @Test
    fun networkInfo_speedValues() {
        val network = NetworkInfo(
            rxSpeed = Long.MAX_VALUE,
            txSpeed = Long.MAX_VALUE
        )
        
        assertEquals(Long.MAX_VALUE, network.rxSpeed)
        assertEquals(Long.MAX_VALUE, network.txSpeed)
    }
}