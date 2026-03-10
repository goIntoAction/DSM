package wang.zengye.dsm.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import wang.zengye.dsm.data.model.*

/**
 * FileRepository 单元测试
 * 主要测试 Model 类的定义和基本逻辑
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FileRepositoryTest {

    @Before
    fun setup() {
        // Setup if needed
    }

    @After
    fun tearDown() {
        // Cleanup if needed
    }

    // ========== ShareListDto 测试 ==========

    @Test
    fun shareListDto_propertiesAreCorrect() {
        val dto = ShareListDto(
            success = true,
            data = ShareListDataDto(shares = emptyList())
        )
        
        assertTrue(dto.success)
        assertTrue(dto.data?.shares?.isEmpty() ?: false)
    }

    @Test
    fun shareListDto_error() {
        val dto = ShareListDto(
            success = false,
            error = ApiError(code = 500, errors = listOf("Server error"))
        )
        
        assertFalse(dto.success)
        assertEquals(500, dto.error?.code)
    }

    // ========== FileListDto 测试 ==========

    @Test
    fun fileListDto_propertiesAreCorrect() {
        val dto = FileListDto(
            success = true,
            data = FileListDataDto(files = emptyList(), total = 0)
        )
        
        assertTrue(dto.success)
        assertEquals(0, dto.data?.total)
        assertTrue(dto.data?.files?.isEmpty() ?: false)
    }

    @Test
    fun fileListDto_error() {
        val dto = FileListDto(
            success = false,
            error = ApiError(code = 400, errors = listOf("Invalid path"))
        )
        
        assertFalse(dto.success)
        assertEquals(400, dto.error?.code)
    }

    @Test
    fun fileListDto_withFiles() {
        val files = listOf(
            FileInfoDto(name = "file1.txt", path = "/home/file1.txt"),
            FileInfoDto(name = "file2.txt", path = "/home/file2.txt")
        )
        val dto = FileListDto(
            success = true,
            data = FileListDataDto(files = files, total = 2)
        )
        
        assertEquals(2, dto.data?.total)
        assertEquals(2, dto.data?.files?.size)
        assertEquals("file1.txt", dto.data?.files?.first()?.name)
    }

    // ========== FileInfoDto 测试 ==========

    @Test
    fun fileInfoDto_propertiesAreCorrect() {
        val info = FileInfoDto(
            name = "document.pdf",
            path = "/home/document.pdf",
            isdir = false
        )
        
        assertEquals("document.pdf", info.name)
        assertEquals("/home/document.pdf", info.path)
        assertFalse(info.isdir)
    }

    @Test
    fun fileInfoDto_directory() {
        val info = FileInfoDto(
            name = "documents",
            path = "/home/documents",
            isdir = true
        )
        
        assertTrue(info.isdir)
    }

    @Test
    fun fileInfoDto_hidden() {
        val info = FileInfoDto(
            name = ".hidden",
            path = "/home/.hidden",
            hidden = true
        )
        
        assertTrue(info.hidden)
    }

    @Test
    fun fileInfoDto_defaultValues() {
        val info = FileInfoDto()
        
        assertEquals("", info.name)
        assertEquals("", info.path)
        assertFalse(info.isdir)
        assertFalse(info.hidden)
    }

    // ========== FileTaskDto 测试 ==========

    @Test
    fun fileTaskDto_propertiesAreCorrect() {
        val dto = FileTaskDto(
            success = true,
            data = FileTaskDataDto(taskid = "task-123")
        )
        
        assertTrue(dto.success)
        assertEquals("task-123", dto.data?.taskid)
    }

    @Test
    fun fileTaskDto_error() {
        val dto = FileTaskDto(
            success = false,
            error = ApiError(code = 400, errors = listOf("Invalid request"))
        )
        
        assertFalse(dto.success)
        assertNull(dto.data)
    }

    // ========== FileTaskStatusDto 测试 ==========

    @Test
    fun fileTaskStatusDto_propertiesAreCorrect() {
        val dto = FileTaskStatusDto(
            success = true,
            data = FileTaskStatusDataDto(
                progress = 0.5,
                finished = false,
                failed = false
            )
        )
        
        assertTrue(dto.success)
        assertEquals(0.5, dto.data?.progress ?: 0.0, 0.001)
        assertFalse(dto.data?.finished ?: true)
        assertFalse(dto.data?.failed ?: true)
    }

    @Test
    fun fileTaskStatusDto_finished() {
        val dto = FileTaskStatusDto(
            success = true,
            data = FileTaskStatusDataDto(
                progress = 1.0,
                finished = true,
                failed = false
            )
        )
        
        assertTrue(dto.data?.finished ?: false)
        assertEquals(1.0, dto.data?.progress ?: 0.0, 0.001)
    }

    @Test
    fun fileTaskStatusDto_failed() {
        val dto = FileTaskStatusDto(
            success = true,
            data = FileTaskStatusDataDto(
                progress = 0.3,
                finished = false,
                failed = true
            )
        )
        
        assertTrue(dto.data?.failed ?: false)
    }

    // ========== SearchStartDto 测试 ==========

    @Test
    fun searchStartDto_propertiesAreCorrect() {
        val dto = SearchStartDto(
            success = true,
            data = FileTaskDataDto(taskid = "search-123")
        )
        
        assertTrue(dto.success)
        assertEquals("search-123", dto.data?.taskid)
    }

    // ========== SearchListDto 测试 ==========

    @Test
    fun searchListDto_propertiesAreCorrect() {
        val dto = SearchListDto(
            success = true,
            data = SearchListDataDto(
                status = "searching",
                total = 0,
                items = emptyList()
            )
        )
        
        assertTrue(dto.success)
        assertEquals("searching", dto.data?.status)
    }

    @Test
    fun searchListDto_withResults() {
        val items = listOf(
            SearchItemDto(file = FileInfoDto(name = "found.txt", path = "/home/found.txt"))
        )
        val dto = SearchListDto(
            success = true,
            data = SearchListDataDto(
                status = "finished",
                total = 1,
                items = items
            )
        )
        
        assertEquals("finished", dto.data?.status)
        assertEquals(1, dto.data?.total)
        assertEquals("found.txt", dto.data?.items?.first()?.file?.name)
    }

    // ========== FileAdditionalDto 测试 ==========

    @Test
    fun fileAdditionalDto_propertiesAreCorrect() {
        val additional = FileAdditionalDto(
            size = 1024,
            realPath = "/volume1/home/file.txt",
            mountPointType = "cifs"
        )
        
        assertEquals(1024L, additional.size)
        assertEquals("/volume1/home/file.txt", additional.realPath)
        assertEquals("cifs", additional.mountPointType)
    }

    @Test
    fun fileAdditionalDto_withTime() {
        val additional = FileAdditionalDto(
            size = 2048,
            time = FileTimeDto(
                mTime = 1609459200000L,
                cTime = 1609459200000L
            )
        )
        
        assertEquals(1609459200000L, additional.time?.mTime)
        assertEquals(1609459200000L, additional.time?.cTime)
    }

    // ========== FileTimeDto 测试 ==========

    @Test
    fun fileTimeDto_propertiesAreCorrect() {
        val time = FileTimeDto(
            mTime = 1609459200000L,
            cTime = 1609372800000L,
            crTime = 1609286400000L,
            aTime = 1609545600000L
        )
        
        assertEquals(1609459200000L, time.mTime)
        assertEquals(1609372800000L, time.cTime)
        assertEquals(1609286400000L, time.crTime)
        assertEquals(1609545600000L, time.aTime)
    }

    // ========== ApiError 测试 ==========

    @Test
    fun apiError_propertiesAreCorrect() {
        val error = ApiError(
            code = 400,
            errors = listOf("Bad request", "Invalid parameter")
        )
        
        assertEquals(400, error.code)
        assertEquals(2, error.errors?.size)
        assertEquals("Bad request", error.errors?.first())
    }

    @Test
    fun apiError_noErrors() {
        val error = ApiError(code = 500)
        
        assertEquals(500, error.code)
        assertNull(error.errors)
    }

    // ========== 边界情况测试 ==========

    @Test
    fun fileInfoDto_withAdditional() {
        val info = FileInfoDto(
            name = "file.txt",
            path = "/home/file.txt",
            additional = FileAdditionalDto(
                size = 1024,
                realPath = "/volume1/home/file.txt"
            )
        )
        
        assertEquals(1024L, info.additional?.size)
        assertEquals("/volume1/home/file.txt", info.additional?.realPath)
    }

    @Test
    fun fileListDto_emptyData() {
        val dto = FileListDto(success = true, data = null)
        
        assertTrue(dto.success)
        assertNull(dto.data)
    }
}