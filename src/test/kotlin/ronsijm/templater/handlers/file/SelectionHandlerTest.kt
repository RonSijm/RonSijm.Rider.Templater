package ronsijm.templater.handlers.file

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.ResultAssertions.assertResultEquals
import ronsijm.templater.services.MockFileOperationsService
import ronsijm.templater.services.ServiceContainer

class SelectionHandlerTest : FileHandlerTestBase() {

    private fun createCommand() = getCommand("selection")

    @Test
    fun `test with selection`() {
        val command = createCommand()
        val mockFileService = MockFileOperationsService(selection = "selected text")
        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(emptyList(), context)

        assertResultEquals("selected text", result)
        assertEquals(1, mockFileService.getSelectionCalls.size)
    }

    @Test
    fun `test without selection returns empty`() {
        val command = createCommand()
        val context = createContext()

        val result = command.execute(emptyList(), context)

        assertResultEquals("", result)
    }
}

