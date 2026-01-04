package ronsijm.templater.handlers.file

import org.junit.jupiter.api.Test
import ronsijm.templater.ResultAssertions.assertResultEquals

class TitleHandlerTest : FileHandlerTestBase() {

    private fun createCommand() = getCommand("title")

    @Test
    fun `test with extension`() {
        val command = createCommand()
        val context = createContext(fileName = "my-document.md")

        val result = command.execute(emptyList(), context)

        assertResultEquals("my-document", result)
    }

    @Test
    fun `test without extension`() {
        val command = createCommand()
        val context = createContext(fileName = "my-document")

        val result = command.execute(emptyList(), context)

        assertResultEquals("my-document", result)
    }

    @Test
    fun `test with multiple dots`() {
        val command = createCommand()
        val context = createContext(fileName = "my.document.test.md")

        val result = command.execute(emptyList(), context)

        assertResultEquals("my.document.test", result)
    }
}
