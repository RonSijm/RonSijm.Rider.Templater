package ronsijm.templater.integration

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.TestContextFactory
import ronsijm.templater.parser.TemplateParser
import ronsijm.templater.services.MockSystemOperationsService
import ronsijm.templater.services.ServiceContainer

/**
 * Integration tests for full template execution with multiple modules
 */
class FullTemplateIntegrationTest {

    @Test
    fun `test full template with date and frontmatter`() {
        val frontmatter = mapOf(
            "title" to "Test Document",
            "author" to "John Doe"
        )
        
        val context = TestContextFactory.create(frontmatter = frontmatter, fileContent = "test content")
        val parser = TemplateParser()
        
        val template = """
            # <% tp.frontmatter.title %>
            Author: <% tp.frontmatter.author %>
            Created: <% tp.date.now("YYYY-MM-DD") %>
        """.trimIndent()
        
        val result = parser.parse(template, context)
        
        assertTrue(result.contains("# Test Document"))
        assertTrue(result.contains("Author: John Doe"))
        assertTrue(result.matches(Regex(".*Created: \\d{4}-\\d{2}-\\d{2}.*", RegexOption.DOT_MATCHES_ALL)))
    }

    @Test
    fun `test template with file module and script execution`() {
        val context = TestContextFactory.create(
            fileName = "MyDocument.md",
            filePath = "/projects/docs/MyDocument.md",
            fileContent = "test content"
        )
        val parser = TemplateParser()
        
        val template = """
            # <% tp.file.title %>
            
            <%*
            const fileName = tp.file.name;
            const folder = tp.file.folder(true);
            tR += "File: " + fileName + "\n";
            tR += "Location: " + folder;
            %>
        """.trimIndent()
        
        val result = parser.parse(template, context)
        
        assertTrue(result.contains("# MyDocument"))
        assertTrue(result.contains("File: MyDocument.md"))
        assertTrue(result.contains("Location: /projects/docs"))
    }

    @Test
    fun `test template with loops and conditionals`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val parser = TemplateParser()

        val template = """
            <%*
            for (let i = 1; i <= 3; i++) {
                tR += i + ". Item\n";
            }
            %>
        """.trimIndent()

        val result = parser.parse(template, context)

        assertTrue(result.contains("1. Item"), "Expected '1. Item' but got: $result")
        assertTrue(result.contains("2. Item"), "Expected '2. Item' but got: $result")
        assertTrue(result.contains("3. Item"), "Expected '3. Item' but got: $result")
    }

    @Test
    fun `test template with system prompt simulation`() {
        // Create a mock system operations service with predefined response
        val mockSystemOps = MockSystemOperationsService(promptResponse = "User Input")

        val services = ServiceContainer.createForTesting()
        val servicesWithMock = ServiceContainer(
            clipboardService = services.clipboardService,
            httpService = services.httpService,
            fileOperationService = services.fileOperationService,
            systemOperationsService = mockSystemOps
        )

        val context = TestContextFactory.create(
            fileContent = "test content",
            services = servicesWithMock
        )
        val parser = TemplateParser()

        val template = """
            <%*
            const name = await tp.system.prompt("Enter your name");
            tR += "Hello, " + name + "!";
            %>
        """.trimIndent()

        val result = parser.parse(template, context)

        // Verify the prompt was called with the correct message
        assertEquals(1, mockSystemOps.promptCalls.size)
        assertEquals("Enter your name", mockSystemOps.promptCalls[0].promptText)
        assertTrue(result.contains("Hello, User Input!"))
    }

    @Test
    fun `test complex template with multiple modules`() {
        val frontmatter = mapOf(
            "project" to "Templater Plugin",
            "version" to "1.0.0"
        )

        val context = TestContextFactory.create(
            frontmatter = frontmatter,
            fileName = "README.md",
            filePath = "/projects/templater/README.md",
            fileContent = "test content"
        )
        val parser = TemplateParser()

        val template = """
            <%* let project = tp.frontmatter.project -%>
            <%* let version = tp.frontmatter.version -%>
            # <% project %>
            Version: <% version %>
            File: <% tp.file.name %>
        """.trimIndent()

        val result = parser.parse(template, context)

        assertTrue(result.contains("# Templater Plugin"), "Expected '# Templater Plugin' but got: $result")
        assertTrue(result.contains("Version: 1.0.0"), "Expected 'Version: 1.0.0' but got: $result")
        assertTrue(result.contains("File: README.md"), "Expected 'File: README.md' but got: $result")
    }
}

