package ronsijm.templater.handlers.system

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.ResultAssertions.assertCancelled
import ronsijm.templater.ResultAssertions.assertResultEquals
import ronsijm.templater.services.MockSystemOperationsService
import ronsijm.templater.services.ServiceContainer

class PromptHandlerTest : SystemHandlerTestBase() {

    private fun createCommand() = getCommand("prompt")

    @Test
    fun `test with service`() {
        val service = MockSystemOperationsService(promptResponse = "User Input")
        val services = ServiceContainer(systemOperationsService = service)
        val context = createContext(services)

        val command = createCommand()
        val result = command.execute(listOf("Enter name", "John"), context)

        assertResultEquals("User Input", result)
        assertEquals(1, service.promptCalls.size)
        assertEquals("Enter name", service.promptCalls[0].promptText)
        assertEquals("John", service.promptCalls[0].defaultValue)
    }

    @Test
    fun `test without service returns cancelled`() {
        val context = createContext()

        val command = createCommand()
        val result = command.execute(listOf("Enter name"), context)

        assertCancelled(result)
    }
}

