package ronsijm.templater.handlers.file

import org.junit.jupiter.api.Test
import ronsijm.templater.ResultAssertions.assertResultEquals

class ContentHandlerTest : FileHandlerTestBase() {

    private fun createCommand() = getCommand("content")

    @Test
    fun `test with content`() {
        val command = createCommand()
        val content = "# My Document\n\nThis is the content."
        val context = createContext(fileContent = content)

        val result = command.execute(emptyList(), context)

        assertResultEquals(content, result)
    }

    @Test
    fun `test without content returns empty`() {
        val command = createCommand()
        val context = createContext(fileContent = null)

        val result = command.execute(emptyList(), context)

        assertResultEquals("", result)
    }
}

