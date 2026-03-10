package wang.zengye.dsm.ui.file

import org.junit.Assert.*
import org.junit.Test

/**
 * FileViewModel 单元测试
 * 
 * 主要测试 State、Intent、Event 的定义和基本逻辑
 * 完整的 ViewModel 测试需要 Hilt 和 Robolectric 支持
 */
class FileViewModelTest {

    // ========== FileItem 测试 ==========

    @Test
    fun fileItem_propertiesAreCorrect() {
        val item = FileItem(
            name = "document.pdf",
            path = "/home/document.pdf",
            isDir = false,
            size = 1024000,
            modified = 1704067200,
            extension = "pdf"
        )
        
        assertEquals("document.pdf", item.name)
        assertEquals("/home/document.pdf", item.path)
        assertFalse(item.isDir)
        assertTrue(item.isFile)
        assertEquals(1024000L, item.size)
        assertEquals("pdf", item.extension)
    }

    @Test
    fun fileItem_isDirectory() {
        val item = FileItem(
            name = "documents",
            path = "/home/documents",
            isDir = true
        )
        
        assertTrue(item.isDir)
        assertFalse(item.isFile)
    }

    @Test
    fun fileItem_defaultValues() {
        val item = FileItem()
        
        assertEquals("", item.name)
        assertEquals("", item.path)
        assertFalse(item.isDir)
        assertEquals(0L, item.size)
        assertEquals("", item.extension)
    }

    // ========== FileOperationState 测试 ==========

    @Test
    fun fileOperationState_defaultValues() {
        val state = FileOperationState()
        
        assertFalse(state.isOperating)
        assertEquals("", state.operationType)
        assertEquals(0f, state.progress, 0.001f)
        assertEquals("", state.message)
    }

    @Test
    fun fileOperationState_copyOperation() {
        val state = FileOperationState(
            isOperating = true,
            operationType = "copy",
            progress = 0.5f,
            message = "Copying files..."
        )
        
        assertTrue(state.isOperating)
        assertEquals("copy", state.operationType)
        assertEquals(0.5f, state.progress, 0.001f)
    }

    // ========== FileUiState 测试 ==========

    @Test
    fun fileUiState_defaultValues() {
        val state = FileUiState()
        
        assertEquals("/", state.currentPath)
        assertTrue(state.files.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("name", state.sortBy)
        assertTrue(state.sortAsc)
        assertTrue(state.selectedItems.isEmpty())
        assertEquals("list", state.viewMode)
    }

    @Test
    fun fileUiState_withFiles() {
        val files = listOf(
            FileItem(name = "file1.txt", path = "/file1.txt"),
            FileItem(name = "file2.txt", path = "/file2.txt")
        )
        val state = FileUiState(
            currentPath = "/home",
            files = files,
            isLoading = false
        )
        
        assertEquals("/home", state.currentPath)
        assertEquals(2, state.files.size)
        assertEquals("file1.txt", state.files[0].name)
    }

    @Test
    fun fileUiState_withSelection() {
        val state = FileUiState(
            selectedItems = setOf("/file1.txt", "/file2.txt")
        )
        
        assertEquals(2, state.selectedItems.size)
        assertTrue(state.selectedItems.contains("/file1.txt"))
    }

    @Test
    fun fileUiState_gridViewMode() {
        val state = FileUiState(viewMode = "grid")
        
        assertEquals("grid", state.viewMode)
    }

    @Test
    fun fileUiState_searchMode() {
        val state = FileUiState(
            isSearchMode = true,
            searchQuery = "test",
            searchResults = listOf(
                FileItem(name = "test.txt", path = "/test.txt")
            )
        )
        
        assertTrue(state.isSearchMode)
        assertEquals("test", state.searchQuery)
        assertEquals(1, state.searchResults.size)
    }

    // ========== FileIntent 测试 ==========

    @Test
    fun fileIntent_types() {
        // 验证所有 Intent 类型
        val loadFiles = FileIntent.LoadFiles("/home")
        assertTrue(loadFiles is FileIntent)
        assertEquals("/home", loadFiles.path)

        val navigateTo = FileIntent.NavigateTo("/documents")
        assertTrue(navigateTo is FileIntent)
        
        val navigateUp = FileIntent.NavigateUp
        assertTrue(navigateUp is FileIntent)
        
        val toggleSelection = FileIntent.ToggleSelection("/file.txt")
        assertTrue(toggleSelection is FileIntent)
        
        val clearSelection = FileIntent.ClearSelection
        assertTrue(clearSelection is FileIntent)
        
        val toggleViewMode = FileIntent.ToggleViewMode
        assertTrue(toggleViewMode is FileIntent)
        
        val sortFiles = FileIntent.SortFiles("size")
        assertTrue(sortFiles is FileIntent)
    }

    @Test
    fun fileIntent_fileOperations() {
        val createFolder = FileIntent.CreateFolder("NewFolder")
        assertEquals("NewFolder", createFolder.name)

        val rename = FileIntent.Rename("/old.txt", "new.txt")
        assertEquals("/old.txt", rename.path)
        assertEquals("new.txt", rename.newName)

        val delete = FileIntent.Delete(listOf("/file1.txt", "/file2.txt"))
        assertEquals(2, delete.paths.size)

        val copy = FileIntent.Copy(listOf("/file.txt"), "/backup")
        assertEquals("/backup", copy.destPath)

        val move = FileIntent.Move(listOf("/file.txt"), "/archive")
        assertEquals("/archive", move.destPath)

        val compress = FileIntent.Compress(listOf("/file.txt"), "/archive.zip", "password")
        assertEquals("password", compress.password)

        val extract = FileIntent.Extract("/archive.zip", "/extracted", "password")
        assertEquals("/archive.zip", extract.filePath)
        assertEquals("/extracted", extract.destPath)
    }

    @Test
    fun fileIntent_search() {
        val search = FileIntent.Search("query")
        assertEquals("query", search.query)

        val clearSearch = FileIntent.ClearSearch
        assertTrue(clearSearch is FileIntent)
    }

    @Test
    fun fileIntent_otherOperations() {
        val addToFavorite = FileIntent.AddToFavorite("/file.txt", "MyFile")
        assertEquals("/file.txt", addToFavorite.path)
        assertEquals("MyFile", addToFavorite.name)

        val createShareLink = FileIntent.CreateShareLink("/file.txt")
        assertEquals("/file.txt", createShareLink.path)
    }

    // ========== FileEvent 测试 ==========

    @Test
    fun fileEvent_types() {
        val showError = FileEvent.ShowError("Error message")
        assertEquals("Error message", showError.message)

        val showSuccess = FileEvent.ShowSuccess("Success message")
        assertEquals("Success message", showSuccess.message)

        val operationSuccess = FileEvent.OperationSuccess("delete")
        assertEquals("delete", operationSuccess.operationType)

        val shareLinkCreated = FileEvent.ShareLinkCreated("https://example.com/share")
        assertEquals("https://example.com/share", shareLinkCreated.url)
    }

    // ========== 边界情况测试 ==========

    @Test
    fun fileItem_withSpecialCharacters() {
        val item = FileItem(
            name = "文件 (1).pdf",
            path = "/home/文件 (1).pdf",
            extension = "pdf"
        )
        
        assertEquals("文件 (1).pdf", item.name)
        assertEquals("pdf", item.extension)
    }

    @Test
    fun fileUiState_withError() {
        val state = FileUiState(
            error = "Network error",
            isLoading = false
        )
        
        assertEquals("Network error", state.error)
    }

    @Test
    fun fileOperationState_progressRange() {
        // 测试进度值边界
        val state1 = FileOperationState(progress = 0f)
        assertEquals(0f, state1.progress, 0.001f)

        val state2 = FileOperationState(progress = 1f)
        assertEquals(1f, state2.progress, 0.001f)

        val state3 = FileOperationState(progress = 0.5f)
        assertEquals(0.5f, state3.progress, 0.001f)
    }

    @Test
    fun fileUiState_sortOptions() {
        val state1 = FileUiState(sortBy = "name", sortAsc = true)
        assertEquals("name", state1.sortBy)
        assertTrue(state1.sortAsc)

        val state2 = FileUiState(sortBy = "size", sortAsc = false)
        assertEquals("size", state2.sortBy)
        assertFalse(state2.sortAsc)

        val state3 = FileUiState(sortBy = "modified", sortAsc = true)
        assertEquals("modified", state3.sortBy)
    }
}
