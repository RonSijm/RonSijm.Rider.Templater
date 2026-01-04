package ronsijm.templater.parallel

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.TestContextFactory
import ronsijm.templater.services.ServiceContainer
import ronsijm.templater.services.mock.MockSystemOperationsService


class ParallelTemplateParserIntegrationTest {




    private fun createParser(enableParallel: Boolean = false): ParallelTemplateParser {
        return ParallelTemplateParser(
            validateSyntax = true,
            services = ServiceContainer.createForTesting(),
            enableParallel = enableParallel
        )
    }



    @Test
    fun `test simple interpolation`() {
        val parser = createParser()
        val context = TestContextFactory.create(fileName = "test.md")

        val template = "File: <% tp.file.name %>"
        val result = parser.parse(template, context)

        assertEquals("File: test.md", result)
    }

    @Test
    fun `test multiple independent interpolations`() {
        val parser = createParser()
        val context = TestContextFactory.create(
            fileName = "document.md",
            filePath = "/docs/document.md"
        )

        val template = """
            Name: <% tp.file.name %>
            Title: <% tp.file.title %>
            Folder: <% tp.file.folder(true) %>
        """.trimIndent()

        val result = parser.parse(template, context)

        assertTrue(result.contains("Name: document.md"))
        assertTrue(result.contains("Title: document"))
        assertTrue(result.contains("Folder: /docs"))
    }

    @Test
    fun `test execution block with variable`() {
        val parser = createParser()
        val context = TestContextFactory.create()

        val template = """
            <%* let greeting = "Hello World" %>
            Message: <% greeting %>
        """.trimIndent()

        val result = parser.parse(template, context)

        assertTrue(result.contains("Message: Hello World"))
    }



    @Test
    fun `test variable dependencies are respected`() {
        val parser = createParser()
        val context = TestContextFactory.create()

        val template = """
            <%* let counter = 0 %>
            <%* counter = counter + 1 %>
            <%* counter = counter + 1 %>
            <%* counter = counter + 1 %>
            Counter: <% counter %>
        """.trimIndent()

        val result = parser.parse(template, context)

        assertTrue(result.contains("Counter: 3"), "Expected 'Counter: 3' but got: $result")
    }

    @Test
    fun `test read-after-write dependency`() {
        val parser = createParser()
        val context = TestContextFactory.create()

        val template = """
            <%* let greeting = "Hello" %>
            <%* greeting = greeting + " World" %>
            <%* greeting = greeting + "!" %>
            Result: <% greeting %>
        """.trimIndent()

        val result = parser.parse(template, context)

        assertTrue(result.contains("Result: Hello World!"), "Expected 'Result: Hello World!' but got: $result")
    }

    @Test
    fun `test independent variables can be parallel`() {
        val parser = createParser()
        val context = TestContextFactory.create()


        val template = """
            <%* let x = 10 %>
            <%* let y = 20 %>
            <%* let z = 30 %>
            <%* let sum = x + y + z %>
            Sum: <% sum %>
        """.trimIndent()

        val result = parser.parse(template, context)

        assertTrue(result.contains("Sum: 60"), "Expected 'Sum: 60' but got: $result")
    }



    @Test
    fun `test tR accumulation order is preserved`() {
        val parser = createParser()
        val context = TestContextFactory.create()

        val template = """
            <%*
            tR += "A"
            tR += "B"
            tR += "C"
            %>
        """.trimIndent()

        val result = parser.parse(template, context)

        assertTrue(result.contains("ABC"), "Expected 'ABC' but got: $result")
        assertFalse(result.contains("CBA"), "Order should not be reversed")
        assertFalse(result.contains("BCA"), "Order should not be scrambled")
    }

    @Test
    fun `test multiple tR blocks maintain order`() {
        val parser = createParser()
        val context = TestContextFactory.create()

        val template = """
            <%* tR += "First " -%>
            <%* tR += "Second " -%>
            <%* tR += "Third" %>
        """.trimIndent()

        val result = parser.parse(template, context)


        assertTrue(result.contains("First "), "Expected 'First ' but got: $result")
        assertTrue(result.contains("Second "), "Expected 'Second ' but got: $result")
        assertTrue(result.contains("Third"), "Expected 'Third' but got: $result")

        val firstIdx = result.indexOf("First ")
        val secondIdx = result.indexOf("Second ")
        val thirdIdx = result.indexOf("Third")
        assertTrue(firstIdx < secondIdx, "First should come before Second")
        assertTrue(secondIdx < thirdIdx, "Second should come before Third")
    }



    @Test
    fun `test for loop with tR accumulation`() {
        val parser = createParser()
        val context = TestContextFactory.create()

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
    fun `test conditional execution`() {
        val parser = createParser()
        val context = TestContextFactory.create()

        val template = """
            <%*
            let value = 10;
            if (value > 5) {
                tR += "Greater";
            } else {
                tR += "Lesser";
            }
            %>
        """.trimIndent()

        val result = parser.parse(template, context)

        assertTrue(result.contains("Greater"), "Expected 'Greater' but got: $result")
        assertFalse(result.contains("Lesser"), "Should not contain 'Lesser'")
    }



    @Test
    fun `test frontmatter access with parallel execution`() {
        val parser = createParser()
        val frontmatter = mapOf(
            "title" to "My Document",
            "author" to "John Doe",
            "version" to "1.0"
        )
        val context = TestContextFactory.create(frontmatter = frontmatter)

        val template = """
            Title: <% tp.frontmatter.title %>
            Author: <% tp.frontmatter.author %>
            Version: <% tp.frontmatter.version %>
        """.trimIndent()

        val result = parser.parse(template, context)

        assertTrue(result.contains("Title: My Document"))
        assertTrue(result.contains("Author: John Doe"))
        assertTrue(result.contains("Version: 1.0"))
    }



    @Test
    fun `test date module with parallel execution`() {
        val parser = createParser()
        val context = TestContextFactory.create()

        val template = """
            Today: <% tp.date.now("YYYY-MM-DD") %>
            Weekday: <% tp.date.weekday("dddd") %>
        """.trimIndent()

        val result = parser.parse(template, context)


        assertTrue(result.matches(Regex(".*Today: \\d{4}-\\d{2}-\\d{2}.*", RegexOption.DOT_MATCHES_ALL)))

        assertTrue(result.contains("Weekday:"))
    }



    @Test
    fun `test sequential execution produces correct results`() {



        val sequentialParser = createParser(enableParallel = false)
        val context = TestContextFactory.create(
            fileName = "test.md",
            frontmatter = mapOf("key" to "value")
        )

        val template = """
            <%* let x = 5 %>
            <%* let y = 10 %>
            <%* let sum = x + y %>
            File: <% tp.file.name %>
            Sum: <% sum %>
            Key: <% tp.frontmatter.key %>
        """.trimIndent()

        val result = sequentialParser.parse(template, context)

        assertTrue(result.contains("File: test.md"), "Expected 'File: test.md' but got: $result")
        assertTrue(result.contains("Sum: 15"), "Expected 'Sum: 15' but got: $result")
        assertTrue(result.contains("Key: value"), "Expected 'Key: value' but got: $result")
    }

    @Test
    fun `test complex template with dependencies produces correct result`() {
        val parser = createParser()
        val context = TestContextFactory.create()

        val template = """
            <%* let a = 1 %>
            <%* let b = 2 %>
            <%* let c = a + b %>
            <%* let d = c * 2 %>
            Result: <% d %>
        """.trimIndent()

        val result = parser.parse(template, context)

        assertTrue(result.contains("Result: 6"), "Expected 'Result: 6' but got: $result")
    }



    @Test
    fun `test barrier function forces sequential execution`() {
        val mockSystemOps = MockSystemOperationsService(promptResponse = "TestUser")
        val services = ServiceContainer(
            clipboardService = ServiceContainer.createForTesting().clipboardService,
            httpService = ServiceContainer.createForTesting().httpService,
            fileOperationService = ServiceContainer.createForTesting().fileOperationService,
            systemOperationsService = mockSystemOps
        )
        val parser = ParallelTemplateParser(
            validateSyntax = true,
            services = services,
            enableParallel = true
        )
        val context = TestContextFactory.create(services = services)

        val template = """
            <%* let name = await tp.system.prompt("Enter name") %>
            Hello, <% name %>!
        """.trimIndent()

        val result = parser.parse(template, context)

        assertTrue(result.contains("Hello, TestUser!"), "Expected 'Hello, TestUser!' but got: $result")
        assertEquals(1, mockSystemOps.promptCalls.size, "Prompt should be called exactly once")
    }



    @Test
    fun `test empty template`() {
        val parser = createParser()
        val context = TestContextFactory.create()

        val result = parser.parse("", context)

        assertEquals("", result)
    }

    @Test
    fun `test template with no blocks`() {
        val parser = createParser()
        val context = TestContextFactory.create()

        val template = "Just plain text without any template blocks"
        val result = parser.parse(template, context)

        assertEquals(template, result)
    }

    @Test
    fun `test single block template`() {
        val parser = createParser()
        val context = TestContextFactory.create(fileName = "single.md")

        val template = "<% tp.file.name %>"
        val result = parser.parse(template, context)

        assertEquals("single.md", result)
    }

    @Test
    fun `test whitespace trimming with parallel execution`() {
        val parser = createParser()
        val context = TestContextFactory.create()

        val template = """
            Line 1
            <%* let x = 5 -%>
            Line 2
        """.trimIndent()

        val result = parser.parse(template, context)


        assertTrue(result.contains("Line 1"))
        assertTrue(result.contains("Line 2"))
    }
}
