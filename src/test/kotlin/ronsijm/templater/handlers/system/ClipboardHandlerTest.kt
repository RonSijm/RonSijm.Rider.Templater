package ronsijm.templater.handlers.system

import org.junit.jupiter.api.Test
import ronsijm.templater.ResultAssertions.assertResultEquals
import ronsijm.templater.services.MockClipboardService
import ronsijm.templater.services.ServiceContainer

class ClipboardHandlerTest : SystemHandlerTestBase() {

    private fun createCommand() = getCommand("clipboard")

    @Test
    fun `test with service`() {
        val clipboardService = MockClipboardService("Clipboard Content")
        val services = ServiceContainer(clipboardService = clipboardService)
        val context = createContext(services)

        val command = createCommand()
        val result = command.execute(emptyList(), context)

        assertResultEquals("Clipboard Content", result)
    }

    @Test
    fun `test with empty clipboard`() {
        val clipboardService = MockClipboardService("")
        val services = ServiceContainer(clipboardService = clipboardService)
        val context = createContext(services)

        val command = createCommand()
        val result = command.execute(emptyList(), context)

        assertResultEquals("", result)
    }
}

