package ronsijm.templater.parser

import ronsijm.templater.TestContextFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TemplateParserScriptTest {

    private val parser = TemplateParser()

    @Test
    fun `test execution command with variable assignment`() {
        val content = """
            <%* let greeting = "Hello World" -%>
            Message: <% greeting %>
        """.trimIndent()

        val context = TestContextFactory.create(fileContent = "test content")
        val result = parser.parse(content, context)
        
        assertTrue(result.contains("Message: Hello World"))
    }
    
    @Test
    fun `test execution command with date function`() {
        val content = """
            <%* let today = tp.date.now("yyyy-MM-dd") -%>
            Today: <% today %>
        """.trimIndent()
        
        val context = TestContextFactory.create(fileContent = "test content")
        val result = parser.parse(content, context)

        assertTrue(result.contains("Today:"))
        assertTrue(result.contains("-")) // Date should contain dashes
    }

    @Test
    fun `test multiple execution commands`() {
        val content = """
            <%* let a = "Hello" -%>
            <%* let b = "World" -%>
            <%* let c = a + " " + b -%>
            Result: <% c %>
        """.trimIndent()

        val context = TestContextFactory.create(fileContent = "test content")
        val result = parser.parse(content, context)

        assertTrue(result.contains("Result: Hello World"))
    }

    @Test
    fun `test string concatenation in execution command`() {
        val content = """
            <%* let fileName = "note-" + tp.date.now("yyyy-MM-dd") -%>
            File: <% fileName %>
        """.trimIndent()

        val context = TestContextFactory.create(fileContent = "test content")
        val result = parser.parse(content, context)

        assertTrue(result.contains("File: note-"))
    }

    @Test
    fun `test Move dot md scenario`() {
        val content = """
            <%*
            let qcFileName = "TestNote"
            titleName = qcFileName + " " + tp.date.now("yyyy-MM-dd")
            -%>
            ---
            title: <% qcFileName %>
            date: <% tp.file.creation_date("yyyy-MM-dd HH:mm:ss") %>
            tags: quick_note
            topic:
            ---

            Content here
        """.trimIndent()

        val context = TestContextFactory.create(fileContent = "test content")
        val result = parser.parse(content, context)

        // Should not throw an error
        assertNotNull(result)
        assertTrue(result.contains("title: TestNote"))
        assertTrue(result.contains("tags: quick_note"))
    }

    @Test
    fun `test execution command removes itself`() {
        val content = """
            Before
            <%* let x = "hidden" -%>
            After
        """.trimIndent()

        val context = TestContextFactory.create(fileContent = "test content")
        val result = parser.parse(content, context)

        // Execution command should be removed
        assertFalse(result.contains("<%*"))
        assertFalse(result.contains("let x"))
        assertTrue(result.contains("Before"))
        assertTrue(result.contains("After"))
    }

    @Test
    fun `test whitespace trimming with execution command`() {
        val content = "Line1\n<%* let x = \"test\" -%>\nLine2"

        val context = TestContextFactory.create(fileContent = "test content")
        val result = parser.parse(content, context)

        // Should trim the newline after the execution command
        assertEquals("Line1\nLine2", result)
    }

    @Test
    fun `test frontmatter access in script`() {
        val content = """
            <%* let docTitle = tp.frontmatter.title -%>
            Title: <% docTitle %>
        """.trimIndent()

        val context = TestContextFactory.create(
            frontmatter = mapOf("title" to "My Document"),
            fileContent = "test content"
        )
        val result = parser.parse(content, context)

        assertTrue(result.contains("Title: My Document"))
    }

    @Test
    fun `test complex concatenation`() {
        val content = """
            <%*
            let part1 = "Hello"
            let part2 = "World"
            let part3 = tp.date.now("yyyy")
            let combined = part1 + " " + part2 + " " + part3
            -%>
            Result: <% combined %>
        """.trimIndent()

        val context = TestContextFactory.create(fileContent = "test content")
        val result = parser.parse(content, context)
        
        assertTrue(result.contains("Result: Hello World"))
    }
}

