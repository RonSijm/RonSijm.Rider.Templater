package ronsijm.templater.handlers.file

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.ResultAssertions.assertResultEquals
import ronsijm.templater.services.MockFileOperationsService
import ronsijm.templater.services.ServiceContainer

/**
 * Tests for file operation handlers: rename, move, cursor, cursor_append, create_new
 */
class FileOperationHandlerTest : FileHandlerTestBase() {

    private fun createRenameCommand() = getCommand("rename")
    private fun createMoveCommand() = getCommand("move")
    private fun createCursorCommand() = getCommand("cursor")
    private fun createCursorAppendCommand() = getCommand("cursor_append")
    private fun createCreateNewCommand() = getCommand("create_new")

    @Test
    fun `test Rename calls service`() {
        val command = createRenameCommand()
        val mockFileService = MockFileOperationsService()
        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(listOf("new-name.md"), context)

        assertResultEquals("", result)
        assertEquals(1, mockFileService.renameCalls.size)
        assertEquals("new-name.md", mockFileService.renameCalls[0])
    }

    @Test
    fun `test Move calls service`() {
        val command = createMoveCommand()
        val mockFileService = MockFileOperationsService()
        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(listOf("/new/path"), context)

        assertResultEquals("", result)
        assertEquals(1, mockFileService.moveCalls.size)
        assertEquals("/new/path", mockFileService.moveCalls[0])
    }

    @Test
    fun `test Cursor calls service`() {
        val command = createCursorCommand()
        val mockFileService = MockFileOperationsService()
        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(listOf(5), context)

        assertResultEquals("", result)
        assertEquals(1, mockFileService.setCursorCalls.size)
        assertEquals(5, mockFileService.setCursorCalls[0])
    }

    @Test
    fun `test CursorAppend calls service`() {
        val command = createCursorAppendCommand()
        val mockFileService = MockFileOperationsService()
        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(listOf("appended content"), context)

        assertResultEquals("", result)
        assertEquals(1, mockFileService.cursorAppendCalls.size)
        assertEquals("appended content", mockFileService.cursorAppendCalls[0])
    }

    @Test
    fun `test CreateNew calls service`() {
        val command = createCreateNewCommand()
        val mockFileService = MockFileOperationsService()
        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(listOf("template.md", "new-file.md", true, "/folder"), context)

        assertResultEquals("", result)
        assertEquals(1, mockFileService.createNewCalls.size)
        assertEquals("template.md", mockFileService.createNewCalls[0].template)
        assertEquals("new-file.md", mockFileService.createNewCalls[0].filename)
        assertTrue(mockFileService.createNewCalls[0].openNew)
        assertEquals("/folder", mockFileService.createNewCalls[0].folder)
    }
}

