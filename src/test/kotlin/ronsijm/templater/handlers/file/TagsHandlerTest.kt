package ronsijm.templater.handlers.file

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.services.MockFileOperationsService
import ronsijm.templater.services.ServiceContainer

class TagsHandlerTest : FileHandlerTestBase() {

    private fun createCommand() = getCommand("tags")

    @Test
    fun `test extracts hashtags from content`() {
        val command = createCommand()
        val content = "# My Document\n\nThis has #tag1 and #tag2 in it."
        val context = createContext(fileContent = content)

        val result = command.execute(emptyList(), context).toString()

        assertTrue(result.contains("tag1"))
        assertTrue(result.contains("tag2"))
    }

    @Test
    fun `test with service`() {
        val command = createCommand()
        val mockFileService = MockFileOperationsService(tags = listOf("custom-tag1", "custom-tag2"))
        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(emptyList(), context).toString()

        assertEquals("custom-tag1, custom-tag2", result)
        assertEquals(1, mockFileService.getTagsCalls.size)
    }
}

