package ronsijm.templater.integration

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.TestContextFactory
import ronsijm.templater.parser.TemplateParser
import ronsijm.templater.services.mock.MockSystemOperationsService
import ronsijm.templater.services.ServiceContainer


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

    @Test
    fun `test callout template with multiple suggesters`() {


        val mockSystemOps = MockSystemOperationsService(
            suggesterResponses = listOf("note", "+"),
            promptResponse = "My Title"
        )

        val services = ServiceContainer.createForTesting(systemOperationsService = mockSystemOps)
        val context = TestContextFactory.create(
            fileContent = "test content",
            services = services
        )
        val parser = TemplateParser()


        val template = """
            <%_*
            let calloutType = await tp.system.suggester(
                ["Note", "Warning", "Info"],
                ["note", "warning", "info"],
                false,
                "Which type of callout?"
            )
            %>
            <%_*
            let foldState = await tp.system.suggester(
                ["Not Foldable", "Default Expanded", "Default Collapsed"],
                ["", "+", "-"],
                false,
                "Folding state?"
            )
            %>
            <%_*
            let title = await tp.system.prompt("Optional Title Text", "")
            %>
            <%-*
            if (calloutType != null) {
                let content = "> [!" + calloutType + "]" + foldState + " " + title
                tR += content
            }
            %>
        """.trimIndent()

        val result = parser.parse(template, context)


        assertEquals(2, mockSystemOps.suggesterCalls.size, "Expected 2 suggester calls")
        assertEquals(1, mockSystemOps.promptCalls.size, "Expected 1 prompt call")


        assertTrue(result.contains("> [!note]+ My Title"), "Expected '> [!note]+ My Title' but got: $result")
    }

    @Test
    fun `test suggester result is assigned to variable correctly`() {

        val mockSystemOps = MockSystemOperationsService(suggesterResponse = "selected_value")

        val services = ServiceContainer.createForTesting(systemOperationsService = mockSystemOps)
        val context = TestContextFactory.create(
            fileContent = "test content",
            services = services
        )
        val parser = TemplateParser()

        val template = """
            <%*
            let choice = await tp.system.suggester(["A", "B"], ["selected_value", "other_value"])
            tR += "You chose: " + choice
            %>
        """.trimIndent()

        val result = parser.parse(template, context)

        assertTrue(result.contains("You chose: selected_value"), "Expected 'You chose: selected_value' but got: $result")
    }

    @Test
    fun `test suggester cancellation with null check`() {

        val mockSystemOps = MockSystemOperationsService(suggesterResponse = null)

        val services = ServiceContainer.createForTesting(systemOperationsService = mockSystemOps)
        val context = TestContextFactory.create(
            fileContent = "test content",
            services = services
        )
        val parser = TemplateParser()

        val template = """
            <%*
            let choice = await tp.system.suggester(["A", "B"], ["a", "b"])
            if (choice != null) {
                tR += "Selected: " + choice
            } else {
                tR += "Cancelled"
            }
            %>
        """.trimIndent()

        val result = parser.parse(template, context)

        assertTrue(result.contains("Cancelled"), "Expected 'Cancelled' but got: $result")
    }
}
