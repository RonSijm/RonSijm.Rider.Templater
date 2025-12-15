package ronsijm.templater.modules.file

import ronsijm.templater.parser.FrontmatterParser
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.handlers.Command
import ronsijm.templater.handlers.generated.HandlerRegistry
import ronsijm.templater.services.MockFileOperationsService
import ronsijm.templater.services.ServiceContainer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class FileCommandsTest {

    private fun getCommand(name: String): Command {
        return HandlerRegistry.commandsByModule["file"]?.get(name)
            ?: throw IllegalArgumentException("Command file.$name not found")
    }

    private fun createContext(
        fileName: String = "test.md",
        filePath: String = "/path/to/test.md",
        fileContent: String? = null,
        services: ServiceContainer = ServiceContainer()
    ): TemplateContext {
        return TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = fileName,
            filePath = filePath,
            fileContent = fileContent,
            services = services
        )
    }

    private fun createTitleCommand(): Command = getCommand("title")
    private fun createNameCommand(): Command = getCommand("name")
    private fun createContentCommand(): Command = getCommand("content")
    private fun createPathCommand(): Command = getCommand("path")
    private fun createFolderCommand(): Command = getCommand("folder")
    private fun createSelectionCommand(): Command = getCommand("selection")
    private fun createTagsCommand(): Command = getCommand("tags")
    private fun createCreationDateCommand(): Command = getCommand("creation_date")
    private fun createLastModifiedDateCommand(): Command = getCommand("last_modified_date")
    private fun createRenameCommand(): Command = getCommand("rename")
    private fun createMoveCommand(): Command = getCommand("move")
    private fun createCursorCommand(): Command = getCommand("cursor")
    private fun createCursorAppendCommand(): Command = getCommand("cursor_append")
    private fun createExistsCommand(): Command = getCommand("exists")
    private fun createFindTFileCommand(): Command = getCommand("find_tfile")
    private fun createIncludeCommand(): Command = getCommand("include")
    private fun createCreateNewCommand(): Command = getCommand("create_new")

    @Test
    fun `test TitleCommand with extension`() {
        val command = createTitleCommand()
        val context = createContext(fileName = "my-document.md")

        val result = command.execute(emptyList(), context)

        assertEquals("my-document", result)
    }

    @Test
    fun `test TitleCommand without extension`() {
        val command = createTitleCommand()
        val context = createContext(fileName = "my-document")

        val result = command.execute(emptyList(), context)

        assertEquals("my-document", result)
    }

    @Test
    fun `test TitleCommand with multiple dots`() {
        val command = createTitleCommand()
        val context = createContext(fileName = "my.document.test.md")

        val result = command.execute(emptyList(), context)

        assertEquals("my.document.test", result)
    }

    @Test
    fun `test NameCommand returns full filename`() {
        val command = createNameCommand()
        val context = createContext(fileName = "my-document.md")

        val result = command.execute(emptyList(), context)

        assertEquals("my-document.md", result)
    }

    @Test
    fun `test ContentCommand with content`() {
        val command = createContentCommand()
        val content = "# My Document\n\nThis is the content."
        val context = createContext(fileContent = content)

        val result = command.execute(emptyList(), context)

        assertEquals(content, result)
    }

    @Test
    fun `test ContentCommand without content returns empty`() {
        val command = createContentCommand()
        val context = createContext(fileContent = null)

        val result = command.execute(emptyList(), context)

        assertEquals("", result)
    }

    @Test
    fun `test PathCommand returns full path`() {
        val command = createPathCommand()
        val context = createContext(filePath = "/path/to/my-document.md")

        val result = command.execute(emptyList(), context)

        assertEquals("/path/to/my-document.md", result)
    }

    @Test
    fun `test PathCommand with relative parameter`() {
        val command = createPathCommand()
        val context = createContext(filePath = "/path/to/my-document.md")

        val result = command.execute(listOf(true), context)

        // PathCommand returns the same path regardless of relative parameter
        assertEquals("/path/to/my-document.md", result)
    }

    @Test
    fun `test FolderCommand returns folder name only`() {
        val command = createFolderCommand()
        val context = createContext(filePath = "/path/to/my-document.md")

        val result = command.execute(emptyList(), context)

        // Default behavior: return just the folder name (last part)
        assertEquals("to", result)
    }

    @Test
    fun `test FolderCommand with relative parameter returns full path`() {
        val command = createFolderCommand()
        val context = createContext(filePath = "/path/to/my-document.md")

        val result = command.execute(listOf(true), context)

        // With relative=true, return full folder path
        assertEquals("/path/to", result)
    }

    @Test
    fun `test FolderCommand with root path`() {
        val command = createFolderCommand()
        val context = createContext(filePath = "/my-document.md")

        val result = command.execute(emptyList(), context)

        // Root path returns empty string
        assertEquals("", result)
    }

    @Test
    fun `test SelectionCommand with selection`() {
        val command = createSelectionCommand()
        val mockFileService = MockFileOperationsService(selection = "selected text")
        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(emptyList(), context)

        assertEquals("selected text", result)
        assertEquals(1, mockFileService.getSelectionCalls.size)
    }

    @Test
    fun `test SelectionCommand without selection returns empty`() {
        val command = createSelectionCommand()
        val context = createContext()

        val result = command.execute(emptyList(), context)

        assertEquals("", result)
    }

    @Test
    fun `test TagsCommand extracts hashtags from content`() {
        val command = createTagsCommand()
        val content = "# My Document\n\nThis has #tag1 and #tag2 in it."
        val context = createContext(fileContent = content)

        val result = command.execute(emptyList(), context)

        assertTrue(result!!.contains("tag1"))
        assertTrue(result.contains("tag2"))
    }

    @Test
    fun `test TagsCommand with service`() {
        val command = createTagsCommand()
        val mockFileService = MockFileOperationsService(tags = listOf("custom-tag1", "custom-tag2"))
        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(emptyList(), context)

        assertEquals("custom-tag1, custom-tag2", result)
        assertEquals(1, mockFileService.getTagsCalls.size)
    }

    @Test
    fun `test CreationDateCommand with default format`() {
        val command = createCreationDateCommand()
        val context = createContext()

        val result = command.execute(emptyList(), context)

        assertNotNull(result)
        // Should match yyyy-MM-dd HH:mm format
        assertTrue(result!!.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")))
    }

    @Test
    fun `test CreationDateCommand with custom format`() {
        val command = createCreationDateCommand()
        val context = createContext()

        val result = command.execute(listOf("dd/MM/yyyy"), context)

        assertNotNull(result)
        // Should match dd/MM/yyyy format
        assertTrue(result!!.matches(Regex("\\d{2}/\\d{2}/\\d{4}")))
    }

    @Test
    fun `test LastModifiedDateCommand with default format`() {
        val command = createLastModifiedDateCommand()
        val context = createContext()

        val result = command.execute(emptyList(), context)

        assertNotNull(result)
        // Should match yyyy-MM-dd HH:mm format
        assertTrue(result!!.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")))
    }

    @Test
    fun `test LastModifiedDateCommand with custom format`() {
        val command = createLastModifiedDateCommand()
        val context = createContext()

        val result = command.execute(listOf("MMMM dd, yyyy"), context)

        assertNotNull(result)
        // Should contain month name
        assertTrue(result!!.matches(Regex("[A-Za-z]+ \\d{2}, \\d{4}")))
    }

    @Test
    fun `test RenameCommand calls service`() {
        val command = createRenameCommand()
        val mockFileService = MockFileOperationsService()
        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(listOf("new-name.md"), context)

        assertEquals("", result) // Execution commands return empty string
        assertEquals(1, mockFileService.renameCalls.size)
        assertEquals("new-name.md", mockFileService.renameCalls[0])
    }

    @Test
    fun `test MoveCommand calls service`() {
        val command = createMoveCommand()
        val mockFileService = MockFileOperationsService()
        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(listOf("/new/path"), context)

        assertEquals("", result) // Execution commands return empty string
        assertEquals(1, mockFileService.moveCalls.size)
        assertEquals("/new/path", mockFileService.moveCalls[0])
    }

    @Test
    fun `test CursorCommand calls service`() {
        val command = createCursorCommand()
        val mockFileService = MockFileOperationsService()
        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(listOf(5), context)

        assertEquals("", result) // Execution commands return empty string
        assertEquals(1, mockFileService.setCursorCalls.size)
        assertEquals(5, mockFileService.setCursorCalls[0])
    }

    @Test
    fun `test CursorAppendCommand calls service`() {
        val command = createCursorAppendCommand()
        val mockFileService = MockFileOperationsService()
        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(listOf("appended content"), context)

        assertEquals("", result) // Execution commands return empty string
        assertEquals(1, mockFileService.cursorAppendCalls.size)
        assertEquals("appended content", mockFileService.cursorAppendCalls[0])
    }

    @Test
    fun `test ExistsCommand returns true when file exists`() {
        val command = createExistsCommand()
        val mockFileService = MockFileOperationsService()
        mockFileService.addFile("/existing/file.md")
        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(listOf("/existing/file.md"), context)

        assertEquals("true", result)
    }

    @Test
    fun `test ExistsCommand returns false when file does not exist`() {
        val command = createExistsCommand()
        val mockFileService = MockFileOperationsService()
        mockFileService.addFile("/existing/file.md")
        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(listOf("/nonexistent/file.md"), context)

        assertEquals("false", result)
    }

    @Test
    fun `test FindTFileCommand returns empty string`() {
        val command = createFindTFileCommand()
        val mockFileService = MockFileOperationsService()
        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(listOf("filename.md"), context)

        // FindTFileCommand is not fully implemented yet
        assertEquals("", result)
    }

    @Test
    fun `test IncludeCommand calls service`() {
        val command = createIncludeCommand()
        val mockFileService = MockFileOperationsService()
        mockFileService.addFile("[[other_file]]", "Included content from [[other_file]]")
        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(listOf("[[other_file]]"), context)

        assertEquals("Included content from [[other_file]]", result)
    }

    @Test
    fun `test CreateNewCommand calls service`() {
        val command = createCreateNewCommand()
        val mockFileService = MockFileOperationsService()
        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(listOf("template.md", "new-file.md", true, "/folder"), context)

        assertEquals("", result) // Execution commands return empty string
        assertEquals(1, mockFileService.createNewCalls.size)
        assertEquals("template.md", mockFileService.createNewCalls[0].template)
        assertEquals("new-file.md", mockFileService.createNewCalls[0].filename)
        assertTrue(mockFileService.createNewCalls[0].openNew)
        assertEquals("/folder", mockFileService.createNewCalls[0].folder)
    }

    @Test
    fun `test CreationDateCommand with service returns exact date`() {
        val command = createCreationDateCommand()

        val testDate = java.time.LocalDateTime.of(2025, 1, 15, 14, 30, 0)
        val timestamp = testDate.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

        val mockFileService = MockFileOperationsService(currentFilePath = "test.md")
        mockFileService.setCreationDate("test.md", timestamp)

        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(emptyList(), context)

        assertEquals("2025-01-15 14:30", result)
    }

    @Test
    fun `test LastModifiedDateCommand with service returns exact date`() {
        val command = createLastModifiedDateCommand()

        val testDate = java.time.LocalDateTime.of(2025, 1, 20, 16, 45, 0)
        val timestamp = testDate.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

        val mockFileService = MockFileOperationsService(currentFilePath = "test.md")
        mockFileService.setModificationDate("test.md", timestamp)

        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(emptyList(), context)

        assertEquals("2025-01-20 16:45", result)
    }

    @Test
    fun `test CreationDateCommand with specific file path`() {
        val command = createCreationDateCommand()

        val testDate = java.time.LocalDateTime.of(2025, 2, 10, 10, 15, 0)
        val timestamp = testDate.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

        val mockFileService = MockFileOperationsService()
        mockFileService.setCreationDate("other-file.md", timestamp)

        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(listOf("yyyy-MM-dd HH:mm", "other-file.md"), context)

        assertEquals("2025-02-10 10:15", result)
    }

    @Test
    fun `test LastModifiedDateCommand with specific file path`() {
        val command = createLastModifiedDateCommand()

        val testDate = java.time.LocalDateTime.of(2025, 2, 10, 10, 15, 0)
        val timestamp = testDate.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

        val mockFileService = MockFileOperationsService()
        mockFileService.setModificationDate("other-file.md", timestamp)

        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(listOf("yyyy-MM-dd HH:mm", "other-file.md"), context)

        assertEquals("2025-02-10 10:15", result)
    }
}

