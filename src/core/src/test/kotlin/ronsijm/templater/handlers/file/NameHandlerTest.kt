package ronsijm.templater.handlers.file

import org.junit.jupiter.api.Test
import ronsijm.templater.ResultAssertions.assertResultEquals

class NameHandlerTest : FileHandlerTestBase() {

    private fun createCommand() = getCommand("name")

    @Test
    fun `test returns full filename`() {
        val command = createCommand()
        val context = createContext(fileName = "my-document.md")

        val result = command.execute(emptyList(), context)

        assertResultEquals("my-document.md", result)
    }
}
