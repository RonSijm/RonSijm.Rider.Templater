package ronsijm.templater.handlers.file

import org.junit.jupiter.api.Test
import ronsijm.templater.ResultAssertions.assertResultEquals

class PathHandlerTest : FileHandlerTestBase() {

    private fun createCommand() = getCommand("path")

    @Test
    fun `test returns full path`() {
        val command = createCommand()
        val context = createContext(filePath = "/path/to/my-document.md")

        val result = command.execute(emptyList(), context)

        assertResultEquals("/path/to/my-document.md", result)
    }

    @Test
    fun `test with relative parameter`() {
        val command = createCommand()
        val context = createContext(filePath = "/path/to/my-document.md")

        val result = command.execute(listOf(true), context)


        assertResultEquals("/path/to/my-document.md", result)
    }
}
