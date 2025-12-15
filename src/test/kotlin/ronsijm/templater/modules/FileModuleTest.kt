package ronsijm.templater.modules

import ronsijm.templater.TestContextFactory
import ronsijm.templater.handlers.generated.HandlerRegistry
import ronsijm.templater.parser.TemplateContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for file module commands using HandlerRegistry
 */
class FileModuleTest {

    private fun executeFile(command: String, args: List<Any?> = emptyList(), context: TemplateContext = TestContextFactory.create()): String? {
        return HandlerRegistry.executeCommand("file", command, args, context)
    }

    @Test
    fun `test title command`() {
        val context = TestContextFactory.create(fileName = "my-document.md")
        val result = executeFile("title", context = context)

        assertEquals("my-document", result)
    }

    @Test
    fun `test title command with no extension`() {
        val context = TestContextFactory.create(fileName = "README")
        val result = executeFile("title", context = context)

        assertEquals("README", result)
    }

    @Test
    fun `test name command`() {
        val context = TestContextFactory.create(fileName = "my-document.md")
        val result = executeFile("name", context = context)

        assertEquals("my-document.md", result)
    }

    @Test
    fun `test path command with relative path`() {
        val context = TestContextFactory.create(filePath = "/project/docs/file.md")
        val result = executeFile("path", listOf(true), context)

        assertEquals("/project/docs/file.md", result)
    }

    @Test
    fun `test path command with absolute path`() {
        val context = TestContextFactory.create(filePath = "/project/docs/file.md")
        val result = executeFile("path", listOf(false), context)

        assertEquals("/project/docs/file.md", result)
    }

    @Test
    fun `test content command`() {
        val testContent = "# My Document\n\nThis is the content."
        val context = TestContextFactory.create(fileContent = testContent)

        val result = executeFile("content", context = context)

        assertEquals(testContent, result)
    }

    @Test
    fun `test folder command`() {
        val context = TestContextFactory.create(filePath = "/project/docs/subfolder/file.md")
        val result = executeFile("folder", context = context)

        assertEquals("subfolder", result)
    }

    @Test
    fun `test folder command with root file`() {
        val context = TestContextFactory.create(filePath = "/file.md")
        val result = executeFile("folder", context = context)

        assertEquals("", result)
    }

    @Test
    fun `test tags command with no tags`() {
        val context = TestContextFactory.create(fileContent = "# Document\n\nNo tags here.")
        val result = executeFile("tags", context = context)

        assertNotNull(result)
        assertTrue(result!!.isEmpty() || result == "[]")
    }

    @Test
    fun `test tags command with tags in content`() {
        val context = TestContextFactory.create(fileContent = "# Document\n\n#tag1 #tag2 #tag3")
        val result = executeFile("tags", context = context)

        assertNotNull(result)
        // Should contain the tags
        assertTrue(result!!.contains("tag1") || result.contains("#tag1"))
    }

    @Test
    fun `test selection command returns empty when no selection`() {
        val context = TestContextFactory.create()
        val result = executeFile("selection", context = context)

        assertNotNull(result)
        assertEquals("", result)
    }

    @Test
    fun `test get available commands`() {
        val commands = HandlerRegistry.fileCommands.map { it.metadata }

        assertTrue(commands.isNotEmpty())
        assertTrue(commands.any { it.name == "title" })
        assertTrue(commands.any { it.name == "name" })
        assertTrue(commands.any { it.name == "path" })
        assertTrue(commands.any { it.name == "content" })
    }
}

