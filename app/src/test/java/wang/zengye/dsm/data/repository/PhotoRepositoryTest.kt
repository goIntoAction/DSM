package wang.zengye.dsm.data.repository

import org.junit.Assert.*
import org.junit.Test
import wang.zengye.dsm.data.model.*

/**
 * PhotoRepository 单元测试
 * 主要测试 Model 类的定义和基本逻辑
 */
class PhotoRepositoryTest {

    // ========== PhotoItem 测试 ==========

    @Test
    fun photoItem_propertiesAreCorrect() {
        val item = PhotoItem(
            id = 123L,
            filename = "photo.jpg",
            filesize = 1024000L,
            time = 1609459200000L,
            type = "photo"
        )
        
        assertEquals(123L, item.id)
        assertEquals("photo.jpg", item.filename)
        assertEquals(1024000L, item.filesize)
        assertEquals("photo", item.type)
    }

    @Test
    fun photoItem_isVideo_photo() {
        val item = PhotoItem(type = "photo")
        assertFalse(item.isVideo)
    }

    @Test
    fun photoItem_isVideo_video() {
        val item = PhotoItem(type = "video")
        assertTrue(item.isVideo)
    }

    // ========== AlbumItem 测试 ==========

    @Test
    fun albumItem_propertiesAreCorrect() {
        val item = AlbumItem(
            id = 456L,
            name = "Vacation",
            itemCount = 100,
            shared = true,
            createTime = 1609459200000L
        )
        
        assertEquals(456L, item.id)
        assertEquals("Vacation", item.name)
        assertEquals(100, item.itemCount)
        assertTrue(item.shared)
    }

    @Test
    fun albumItem_notShared() {
        val item = AlbumItem(shared = false)
        assertFalse(item.shared)
    }

    // ========== PhotosDto 测试 ==========

    @Test
    fun photosDto_propertiesAreCorrect() {
        val dto = PhotosDto(
            data = PhotosDataDto(list = emptyList())
        )
        
        assertTrue(dto.data.list.isEmpty())
    }

    @Test
    fun photosDto_withPhotos() {
        val photos = listOf(
            PhotoItem(id = 1L, filename = "photo1.jpg"),
            PhotoItem(id = 2L, filename = "photo2.jpg")
        )
        val dto = PhotosDto(
            data = PhotosDataDto(list = photos)
        )
        
        assertEquals(2, dto.data.list.size)
        assertEquals("photo1.jpg", dto.data.list[0].filename)
    }

    // ========== AlbumsDto 测试 ==========

    @Test
    fun albumsDto_propertiesAreCorrect() {
        val dto = AlbumsDto(
            data = AlbumsDataDto(list = emptyList())
        )
        
        assertTrue(dto.data.list.isEmpty())
    }

    @Test
    fun albumsDto_withAlbums() {
        val albums = listOf(
            AlbumItem(id = 1L, name = "Album 1"),
            AlbumItem(id = 2L, name = "Album 2")
        )
        val dto = AlbumsDto(
            data = AlbumsDataDto(list = albums)
        )
        
        assertEquals(2, dto.data.list.size)
        assertEquals("Album 1", dto.data.list[0].name)
    }

    // ========== TimelineDto 测试 ==========

    @Test
    fun timelineDto_propertiesAreCorrect() {
        val dto = TimelineDto(
            data = TimelineDataDto(section = emptyList())
        )
        
        assertTrue(dto.data.section?.isEmpty() ?: true)
    }

    @Test
    fun timelineDto_withSections() {
        val sections = listOf(
            TimelineSection(list = emptyList())
        )
        val dto = TimelineDto(
            data = TimelineDataDto(section = sections)
        )
        
        assertEquals(1, dto.data.section?.size)
    }

    // ========== TimelineItemRaw 测试 ==========

    @Test
    fun timelineItemRaw_propertiesAreCorrect() {
        val item = TimelineItemRaw(
            year = 2024,
            month = 1,
            day = 15,
            itemCount = 10,
            date = "2024-01-15"
        )
        
        assertEquals(2024, item.year)
        assertEquals(1, item.month)
        assertEquals(15, item.day)
        assertEquals(10, item.itemCount)
    }

    // ========== GeocodingDto 测试 ==========

    @Test
    fun geocodingDto_propertiesAreCorrect() {
        val dto = GeocodingDto(
            data = GeocodingDataDto(list = emptyList())
        )
        
        assertTrue(dto.data.list.isEmpty())
    }

    // ========== BasePhotoDto 测试 ==========

    @Test
    fun basePhotoDto_success() {
        val dto = BasePhotoDto(success = true)
        
        assertTrue(dto.success)
        assertNull(dto.error)
    }

    @Test
    fun basePhotoDto_error() {
        val dto = BasePhotoDto(
            success = false,
            error = PhotoError(code = 400, msg = "Invalid request")
        )
        
        assertFalse(dto.success)
        assertEquals(400, dto.error?.code)
        assertEquals("Invalid request", dto.error?.msg)
    }

    // ========== 边界情况测试 ==========

    @Test
    fun photoItem_defaultValues() {
        val item = PhotoItem()
        
        assertEquals(0L, item.id)
        assertEquals("", item.filename)
        assertEquals(0L, item.filesize)
        assertEquals("photo", item.type) // 默认值是 "photo"
        assertFalse(item.isVideo)
    }

    @Test
    fun albumItem_defaultValues() {
        val item = AlbumItem()
        
        assertEquals(0L, item.id)
        assertEquals("", item.name)
        assertEquals(0, item.itemCount)
        assertFalse(item.shared)
    }

    @Test
    fun timelineDto_empty() {
        val dto = TimelineDto(data = TimelineDataDto(section = null))
        
        assertNull(dto.data.section)
    }
}