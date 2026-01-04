package ronsijm.templater.parser

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.TestContextFactory

class TemplateParserScriptIntegrationTest {

    @Test
    fun `test script block with tR accumulator`() {
        val parser = TemplateParser()
        val context = TestContextFactory.create()

        val template = """
            <%*
            tR += "Hello, ";
            tR += "World!";
            %>
        """.trimIndent()

        val result = parser.parse(template, context)

        assertEquals("Hello, World!", result.trim())
    }

    @Test
    fun `test script block with for loop`() {
        val parser = TemplateParser()
        val context = TestContextFactory.create()

        val template = """
            <%*
            for (let i = 1; i <= 3; i++) {
                tR += i + ". Item\n";
            }
            %>
        """.trimIndent()

        val result = parser.parse(template, context)

        assertTrue(result.contains("1. Item"))
        assertTrue(result.contains("2. Item"))
        assertTrue(result.contains("3. Item"))
    }

    @Test
    fun `test script block with if else`() {
        val parser = TemplateParser()
        val context = TestContextFactory.create()

        val template = """
            <%*
            const hour = new Date().getHours();
            if (hour < 12) {
                tR += "Good morning!";
            } else if (hour < 18) {
                tR += "Good afternoon!";
            } else {
                tR += "Good evening!";
            }
            %>
        """.trimIndent()

        val result = parser.parse(template, context)

        assertTrue(
            result.contains("Good morning!") ||
            result.contains("Good afternoon!") ||
            result.contains("Good evening!")
        )
    }

    @Test
    fun `test script block followed by tR interpolation`() {
        val parser = TemplateParser()
        val context = TestContextFactory.create()

        val template = """
            <%*
            tR += "Greeting: ";
            const hour = new Date().getHours();
            if (hour < 12) {
                tR += "Good morning!";
            } else {
                tR += "Good afternoon!";
            }
            %>

            Result: <% tR %>
        """.trimIndent()

        val result = parser.parse(template, context)



        assertTrue(result.contains("Greeting: Good"))
        assertTrue(result.contains("Result: Greeting: Good"))
    }

    @Test
    fun `test multiple script blocks`() {
        val parser = TemplateParser()
        val context = TestContextFactory.create()

        val template = """
            <%*
            tR += "First";
            %>

            <%*
            tR += "Second";
            %>
        """.trimIndent()

        val result = parser.parse(template, context)


        assertTrue(result.contains("First"))
        assertTrue(result.contains("Second"))
    }
}
