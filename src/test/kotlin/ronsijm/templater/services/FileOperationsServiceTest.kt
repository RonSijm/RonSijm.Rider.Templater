package ronsijm.templater.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class FileOperationsServiceTest {

    @Test
    fun `test MockFileOperationsService exists returns true for existing files`() {
        val service = MockFileOperationsService()
        service.addFile("test.txt")

        assertTrue(service.exists("test.txt"))
        assertEquals(1, service.existsCalls.size)
        assertEquals("test.txt", service.existsCalls[0])
    }

    @Test
    fun `test MockFileOperationsService exists returns false for non-existing files`() {
        val service = MockFileOperationsService()

        assertFalse(service.exists("nonexistent.txt"))
        assertEquals(1, service.existsCalls.size)
    }

    @Test
    fun `test MockFileOperationsService rename tracks calls`() {
        val service = MockFileOperationsService()

        service.rename("new.txt")

        assertEquals(1, service.renameCalls.size)
        assertEquals("new.txt", service.renameCalls[0])
    }

    @Test
    fun `test MockFileOperationsService move tracks calls`() {
        val service = MockFileOperationsService()

        service.move("target/")

        assertEquals(1, service.moveCalls.size)
        assertEquals("target/", service.moveCalls[0])
    }

    @Test
    fun `test MockFileOperationsService createNew creates file`() {
        val service = MockFileOperationsService()

        val result = service.createNew("content", "new.txt", false, null)

        assertEquals("new.txt", result)
        assertTrue(service.exists("new.txt"))
        assertEquals("content", service.getFileContent("new.txt"))
        assertEquals(1, service.createNewCalls.size)
        assertEquals("content", service.createNewCalls[0].template)
        assertEquals("new.txt", service.createNewCalls[0].filename)
        assertFalse(service.createNewCalls[0].openNew)
        assertNull(service.createNewCalls[0].folder)
    }

    @Test
    fun `test MockFileOperationsService createNew with folder`() {
        val service = MockFileOperationsService()

        val result = service.createNew("test", "file.txt", true, "folder")

        assertEquals("folder/file.txt", result)
        assertTrue(service.exists("folder/file.txt"))
        assertEquals("test", service.getFileContent("folder/file.txt"))
        assertEquals("folder", service.createNewCalls[0].folder)
        assertTrue(service.createNewCalls[0].openNew)
    }

    @Test
    fun `test MockFileOperationsService include returns file content`() {
        val service = MockFileOperationsService()
        service.addFile("include.txt", "included content")

        val result = service.include("include.txt")

        assertEquals("included content", result)
        assertEquals(1, service.includeCalls.size)
        assertEquals("include.txt", service.includeCalls[0])
    }

    @Test
    fun `test MockFileOperationsService include returns null for non-existing file`() {
        val service = MockFileOperationsService()

        val result = service.include("nonexistent.txt")

        assertNull(result)
        assertEquals(1, service.includeCalls.size)
    }

    @Test
    fun `test MockFileOperationsService findFile finds file by name`() {
        val service = MockFileOperationsService()
        service.addFile("path/to/file.txt")
        service.addFile("other/file2.txt")

        val result = service.findFile("file.txt")

        assertEquals("path/to/file.txt", result)
        assertEquals(1, service.findFileCalls.size)
        assertEquals("file.txt", service.findFileCalls[0])
    }

    @Test
    fun `test MockFileOperationsService findFile returns null when not found`() {
        val service = MockFileOperationsService()
        service.addFile("path/to/other.txt")

        val result = service.findFile("notfound.txt")

        assertNull(result)
        assertEquals(1, service.findFileCalls.size)
    }

    @Test
    fun `test MockFileOperationsService tracks multiple operations`() {
        val service = MockFileOperationsService()
        service.addFile("file1.txt", "content1")
        service.addFile("file2.txt", "content2")

        service.exists("file1.txt")
        service.exists("file2.txt")
        service.include("file1.txt")
        service.rename("renamed.txt")

        assertEquals(2, service.existsCalls.size)
        assertEquals(1, service.includeCalls.size)
        assertEquals(1, service.renameCalls.size)
    }

    @Test
    fun `test MockFileOperationsService setCursor tracks calls`() {
        val service = MockFileOperationsService()

        service.setCursor(5)
        service.setCursor(null)

        assertEquals(2, service.setCursorCalls.size)
        assertEquals(5, service.setCursorCalls[0])
        assertNull(service.setCursorCalls[1])
    }

    @Test
    fun `test MockFileOperationsService cursorAppend tracks calls`() {
        val service = MockFileOperationsService()

        service.cursorAppend("appended text")

        assertEquals(1, service.cursorAppendCalls.size)
        assertEquals("appended text", service.cursorAppendCalls[0])
    }

    @Test
    fun `test MockFileOperationsService getSelection returns configured value`() {
        val service = MockFileOperationsService(selection = "selected text")

        val result = service.getSelection()

        assertEquals("selected text", result)
        assertEquals(1, service.getSelectionCalls.size)
    }

    @Test
    fun `test MockFileOperationsService getTags returns configured value`() {
        val service = MockFileOperationsService(tags = listOf("tag1", "tag2"))

        val result = service.getTags()

        assertEquals(listOf("tag1", "tag2"), result)
        assertEquals(1, service.getTagsCalls.size)
    }
}

