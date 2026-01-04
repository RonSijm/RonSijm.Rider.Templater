package ronsijm.templater.handlers.file

import org.junit.jupiter.api.Test
import ronsijm.templater.ResultAssertions.assertResultEquals

class FolderHandlerTest : FileHandlerTestBase() {

    private fun createCommand() = getCommand("folder")

    @Test
    fun `test returns folder name only`() {
        val command = createCommand()
        val context = createContext(filePath = "/path/to/my-document.md")

        val result = command.execute(emptyList(), context)


        assertResultEquals("to", result)
    }

    @Test
    fun `test with relative parameter returns full path`() {
        val command = createCommand()
        val context = createContext(filePath = "/path/to/my-document.md")

        val result = command.execute(listOf(true), context)


        assertResultEquals("/path/to", result)
    }

    @Test
    fun `test with root path`() {
        val command = createCommand()
        val context = createContext(filePath = "/my-document.md")

        val result = command.execute(emptyList(), context)


        assertResultEquals("", result)
    }
}
