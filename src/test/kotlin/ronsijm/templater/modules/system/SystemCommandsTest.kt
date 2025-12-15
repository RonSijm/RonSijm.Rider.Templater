package ronsijm.templater.modules.system

import ronsijm.templater.parser.FrontmatterParser
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.services.MockSystemOperationsService
import ronsijm.templater.services.ServiceContainer
import ronsijm.templater.handlers.Command
import ronsijm.templater.handlers.generated.HandlerRegistry
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class SystemCommandsTest {

    private fun getCommand(name: String): Command {
        return HandlerRegistry.commandsByModule["system"]?.get(name)
            ?: throw IllegalArgumentException("Command system.$name not found")
    }

    private fun createPromptCommand(): Command = getCommand("prompt")
    private fun createSuggesterCommand(): Command = getCommand("suggester")
    private fun createMultiSuggesterCommand(): Command = getCommand("multi_suggester")
    private fun createClipboardCommand(): Command = getCommand("clipboard")

    @Test
    fun `test PromptCommand with service`() {
        val service = MockSystemOperationsService(promptResponse = "User Input")
        val services = ServiceContainer(systemOperationsService = service)
        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md",
            filePath = "/test.md",
            services = services
        )

        val command = createPromptCommand()
        val result = command.execute(listOf("Enter name", "John"), context)

        assertEquals("User Input", result)
        assertEquals(1, service.promptCalls.size)
        assertEquals("Enter name", service.promptCalls[0].promptText)
        assertEquals("John", service.promptCalls[0].defaultValue)
    }

    @Test
    fun `test PromptCommand without service returns null`() {
        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md",
            filePath = "/test.md"
        )

        val command = createPromptCommand()
        val result = command.execute(listOf("Enter name"), context)

        assertNull(result)
    }

    @Test
    fun `test SuggesterCommand with service`() {
        val service = MockSystemOperationsService(suggesterResponse = "Option 1")
        val services = ServiceContainer(systemOperationsService = service)
        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md",
            filePath = "/test.md",
            services = services
        )

        val command = createSuggesterCommand()
        val result = command.execute(listOf(listOf("Option 1", "Option 2")), context)

        assertEquals("Option 1", result)
        assertEquals(1, service.suggesterCalls.size)
    }

    @Test
    fun `test SuggesterCommand with separate values`() {
        val service = MockSystemOperationsService(suggesterResponse = "value2")
        val services = ServiceContainer(systemOperationsService = service)
        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md",
            filePath = "/test.md",
            services = services
        )

        val command = createSuggesterCommand()
        val result = command.execute(
            listOf(
                listOf("Display 1", "Display 2"),
                listOf("value1", "value2")
            ),
            context
        )

        assertEquals("value2", result)
        assertEquals(1, service.suggesterCalls.size)
        assertEquals(listOf("Display 1", "Display 2"), service.suggesterCalls[0].textItems)
        assertEquals(listOf("value1", "value2"), service.suggesterCalls[0].values)
    }

    @Test
    fun `test MultiSuggesterCommand with service`() {
        val service = MockSystemOperationsService(multiSuggesterResponse = listOf("A", "C"))
        val services = ServiceContainer(systemOperationsService = service)
        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md",
            filePath = "/test.md",
            services = services
        )

        val command = createMultiSuggesterCommand()
        val result = command.execute(listOf(listOf("A", "B", "C")), context)

        // Result is a list representation
        assertEquals("[A, C]", result)
        assertEquals(1, service.multiSuggesterCalls.size)
        assertEquals(listOf("A", "B", "C"), service.multiSuggesterCalls[0].textItems)
    }

    @Test
    fun `test ClipboardCommand with service`() {
        val clipboardService = ronsijm.templater.services.MockClipboardService("Clipboard Content")
        val services = ServiceContainer(clipboardService = clipboardService)
        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md",
            filePath = "/test.md",
            services = services
        )

        val command = createClipboardCommand()
        val result = command.execute(emptyList(), context)

        assertEquals("Clipboard Content", result)
    }

    @Test
    fun `test ClipboardCommand with empty clipboard`() {
        val clipboardService = ronsijm.templater.services.MockClipboardService("")
        val services = ServiceContainer(clipboardService = clipboardService)
        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md",
            filePath = "/test.md",
            services = services
        )

        val command = createClipboardCommand()
        val result = command.execute(emptyList(), context)

        assertEquals("", result)
    }
}

