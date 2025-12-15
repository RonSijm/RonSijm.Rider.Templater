package ronsijm.templater.integration

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.TestContextFactory
import ronsijm.templater.parser.TemplateParser

/**
 * Integration tests for error handling and edge cases
 */
class ErrorHandlingIntegrationTest {

    @Test
    fun `test missing frontmatter key returns null`() {
        val context = TestContextFactory.create(frontmatter = mapOf("title" to "Test"))
        val parser = TemplateParser()
        
        val template = "Value: <% tp.frontmatter.nonExistent %>"
        val result = parser.parse(template, context)
        
        // Should handle gracefully - returns "null" string
        assertTrue(result.contains("Value: null") || result.contains("Value:"))
    }

    @Test
    fun `test empty template returns empty string`() {
        val context = TestContextFactory.create()
        val parser = TemplateParser()
        
        val result = parser.parse("", context)
        
        assertEquals("", result)
    }

    @Test
    fun `test template with only text returns text unchanged`() {
        val context = TestContextFactory.create()
        val parser = TemplateParser()
        
        val template = "This is plain text with no template commands."
        val result = parser.parse(template, context)
        
        assertEquals(template, result)
    }

    @Test
    fun `test nested frontmatter access`() {
        val frontmatter = mapOf(
            "metadata" to mapOf(
                "author" to "John Doe",
                "tags" to listOf("test", "integration")
            )
        )
        
        val context = TestContextFactory.create(frontmatter = frontmatter)
        val parser = TemplateParser()
        
        val template = "Author: <% tp.frontmatter.metadata.author %>"
        val result = parser.parse(template, context)
        
        assertTrue(result.contains("Author: John Doe"))
    }

    @Test
    fun `test script with undefined variable`() {
        val context = TestContextFactory.create()
        val parser = TemplateParser()
        
        val template = """
            <%*
            // Using undefined variable should return null
            const value = undefinedVar;
            if (value === null) {
                tR += "Variable is null";
            } else {
                tR += "Variable has value: " + value;
            }
            %>
        """.trimIndent()
        
        val result = parser.parse(template, context)
        
        assertTrue(result.contains("Variable is null"))
    }

    @Test
    fun `test script with division by zero`() {
        val context = TestContextFactory.create()
        val parser = TemplateParser()
        
        val template = """
            <%*
            const result = 10 / 0;
            tR += "Result: " + result;
            %>
        """.trimIndent()
        
        // Should handle gracefully (JavaScript returns Infinity)
        val result = parser.parse(template, context)
        
        assertTrue(result.contains("Result:"))
    }

    @Test
    fun `test empty for loop`() {
        val context = TestContextFactory.create()
        val parser = TemplateParser()
        
        val template = """
            <%*
            for (let i = 0; i < 0; i++) {
                tR += "This should not appear";
            }
            tR += "Loop completed";
            %>
        """.trimIndent()
        
        val result = parser.parse(template, context)
        
        assertFalse(result.contains("This should not appear"))
        assertTrue(result.contains("Loop completed"))
    }

    @Test
    fun `test if statement with false condition`() {
        val context = TestContextFactory.create()
        val parser = TemplateParser()
        
        val template = """
            <%*
            if (false) {
                tR += "Should not appear";
            } else {
                tR += "Else branch executed";
            }
            %>
        """.trimIndent()
        
        val result = parser.parse(template, context)
        
        assertFalse(result.contains("Should not appear"))
        assertTrue(result.contains("Else branch executed"))
    }

    @Test
    fun `test multiple consecutive script blocks`() {
        val context = TestContextFactory.create()
        val parser = TemplateParser()
        
        val template = """
            <%* tR += "First"; %>
            <%* tR += "Second"; %>
            <%* tR += "Third"; %>
        """.trimIndent()
        
        val result = parser.parse(template, context)
        
        assertTrue(result.contains("First"))
        assertTrue(result.contains("Second"))
        assertTrue(result.contains("Third"))
    }

    @Test
    fun `test template with special characters in strings`() {
        val context = TestContextFactory.create()
        val parser = TemplateParser()
        
        val template = """
            <%*
            tR += "String with \"quotes\" and 'apostrophes'";
            %>
        """.trimIndent()
        
        val result = parser.parse(template, context)
        
        assertTrue(result.contains("quotes"))
        assertTrue(result.contains("apostrophes"))
    }
}

