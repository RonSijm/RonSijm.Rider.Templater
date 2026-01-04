package ronsijm.templater.handlers.file

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.services.mock.MockFileOperationsService
import ronsijm.templater.services.ServiceContainer
import java.time.LocalDateTime
import java.time.ZoneId

class FileDateHandlerTest : FileHandlerTestBase() {

    private fun createCreationDateCommand() = getCommand("creation_date")
    private fun createLastModifiedDateCommand() = getCommand("last_modified_date")



    @Test
    fun `test CreationDate with default format`() {
        val command = createCreationDateCommand()
        val context = createContext()

        val result = command.execute(emptyList(), context).toString()

        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")))
    }

    @Test
    fun `test CreationDate with custom format`() {
        val command = createCreationDateCommand()
        val context = createContext()

        val result = command.execute(listOf("dd/MM/yyyy"), context).toString()

        assertTrue(result.matches(Regex("\\d{2}/\\d{2}/\\d{4}")))
    }

    @Test
    fun `test CreationDate with service returns exact date`() {
        val command = createCreationDateCommand()

        val testDate = LocalDateTime.of(2025, 1, 15, 14, 30, 0)
        val timestamp = testDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val mockFileService = MockFileOperationsService(currentFilePath = "test.md")
        mockFileService.setCreationDate("test.md", timestamp)

        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(emptyList(), context).toString()

        assertEquals("2025-01-15 14:30", result)
    }

    @Test
    fun `test CreationDate with specific file path`() {
        val command = createCreationDateCommand()

        val testDate = LocalDateTime.of(2025, 2, 10, 10, 15, 0)
        val timestamp = testDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val mockFileService = MockFileOperationsService()
        mockFileService.setCreationDate("other-file.md", timestamp)

        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(listOf("yyyy-MM-dd HH:mm", "other-file.md"), context).toString()

        assertEquals("2025-02-10 10:15", result)
    }



    @Test
    fun `test LastModifiedDate with default format`() {
        val command = createLastModifiedDateCommand()
        val context = createContext()

        val result = command.execute(emptyList(), context).toString()

        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")))
    }

    @Test
    fun `test LastModifiedDate with custom format`() {
        val command = createLastModifiedDateCommand()
        val context = createContext()

        val result = command.execute(listOf("MMMM dd, yyyy"), context).toString()

        assertTrue(result.matches(Regex("[A-Za-z]+ \\d{2}, \\d{4}")))
    }

    @Test
    fun `test LastModifiedDate with service returns exact date`() {
        val command = createLastModifiedDateCommand()

        val testDate = LocalDateTime.of(2025, 1, 20, 16, 45, 0)
        val timestamp = testDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val mockFileService = MockFileOperationsService(currentFilePath = "test.md")
        mockFileService.setModificationDate("test.md", timestamp)

        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(emptyList(), context).toString()

        assertEquals("2025-01-20 16:45", result)
    }

    @Test
    fun `test LastModifiedDate with specific file path`() {
        val command = createLastModifiedDateCommand()

        val testDate = LocalDateTime.of(2025, 2, 10, 10, 15, 0)
        val timestamp = testDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val mockFileService = MockFileOperationsService()
        mockFileService.setModificationDate("other-file.md", timestamp)

        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(listOf("yyyy-MM-dd HH:mm", "other-file.md"), context).toString()

        assertEquals("2025-02-10 10:15", result)
    }
}
