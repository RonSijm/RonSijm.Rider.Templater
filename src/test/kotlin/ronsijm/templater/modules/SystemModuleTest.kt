package ronsijm.templater.modules

import ronsijm.templater.TestContextFactory
import ronsijm.templater.handlers.generated.HandlerRegistry
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.services.ServiceContainer
import ronsijm.templater.services.MockSystemOperationsService
import ronsijm.templater.services.MockClipboardService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for system module commands using HandlerRegistry
 */
class SystemModuleTest {

    private fun executeSystem(command: String, args: List<Any?> = emptyList(), context: TemplateContext): String? {
        return HandlerRegistry.executeCommand("system", command, args, context)
    }

    @Test
    fun `test prompt with default parameters`() {
        val mockService = MockSystemOperationsService(promptResponse = "user input")
        val services = ServiceContainer.createForTesting(systemOperationsService = mockService)
        val context = TestContextFactory.create(services = services)

        val result = executeSystem("prompt", listOf("Enter name"), context)

        assertEquals("user input", result)
        assertEquals(1, mockService.promptCalls.size)
        assertEquals("Enter name", mockService.promptCalls[0].promptText)
        assertNull(mockService.promptCalls[0].defaultValue)
        assertFalse(mockService.promptCalls[0].multiLine)
    }

    @Test
    fun `test prompt with all parameters`() {
        val mockService = MockSystemOperationsService(promptResponse = "user input")
        val services = ServiceContainer.createForTesting(systemOperationsService = mockService)
        val context = TestContextFactory.create(services = services)

        val result = executeSystem("prompt", listOf("Enter name", "John", "true", "true"), context)

        assertEquals("user input", result)
        assertEquals(1, mockService.promptCalls.size)
        assertEquals("Enter name", mockService.promptCalls[0].promptText)
        assertEquals("John", mockService.promptCalls[0].defaultValue)
        assertTrue(mockService.promptCalls[0].multiLine)
    }

    @Test
    fun `test suggester with basic parameters`() {
        val textItemsList = listOf("Option 1", "Option 2")
        val itemsList = listOf("val1", "val2")

        val mockService = MockSystemOperationsService(suggesterResponse = "val1")
        val services = ServiceContainer.createForTesting(systemOperationsService = mockService)
        val context = TestContextFactory.create(services = services)

        val result = executeSystem("suggester", listOf(textItemsList, itemsList), context)

        assertEquals("val1", result)
        assertEquals(1, mockService.suggesterCalls.size)
        assertEquals(textItemsList, mockService.suggesterCalls[0].textItems)
        assertEquals(itemsList, mockService.suggesterCalls[0].values)
    }

    @Test
    fun `test clipboard`() {
        val mockClipboard = MockClipboardService("clipboard content")
        val services = ServiceContainer.createForTesting(clipboardService = mockClipboard)
        val context = TestContextFactory.create(services = services)

        val result = executeSystem("clipboard", emptyList(), context)

        assertEquals("clipboard content", result)
    }

    @Test
    fun `test multi_suggester`() {
        val textItemsList = listOf("Opt 1", "Opt 2")
        val itemsList = listOf("v1", "v2")

        val mockService = MockSystemOperationsService(multiSuggesterResponse = listOf("v1", "v2"))
        val services = ServiceContainer.createForTesting(systemOperationsService = mockService)
        val context = TestContextFactory.create(services = services)

        val result = executeSystem("multi_suggester", listOf(textItemsList, itemsList), context)

        // The result is a string representation of the list
        assertEquals("[v1, v2]", result)
        assertEquals(1, mockService.multiSuggesterCalls.size)
        assertEquals(textItemsList, mockService.multiSuggesterCalls[0].textItems)
        assertEquals(itemsList, mockService.multiSuggesterCalls[0].values)
    }
}

