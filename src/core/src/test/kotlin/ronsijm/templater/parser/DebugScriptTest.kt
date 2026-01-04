package ronsijm.templater.parser

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class DebugScriptTest {

    @Test
    fun `debug script block execution`() {
        val parser = TemplateParser()
        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md",
            filePath = "/test.md"
        )

        val template = """
            <%*
            const hour = new Date().getHours();
            if (hour < 12) {
                tR += "Good morning! ??";
            } else if (hour < 18) {
                tR += "Good afternoon! ???";
            } else {
                tR += "Good evening! ??";
            }
            %>
        """.trimIndent()

        println("=== INPUT TEMPLATE ===")
        println(template)
        println()

        val result = parser.parse(template, context)

        println("=== OUTPUT RESULT ===")
        println(result)
        println()
        println("=== RESULT LENGTH: ${result.length} ===")
        println("=== RESULT BYTES: ${result.toByteArray().joinToString(",")} ===")


        val hasGreeting = result.contains("Good morning!") ||
                         result.contains("Good afternoon!") ||
                         result.contains("Good evening!")

        println("=== HAS GREETING: $hasGreeting ===")

        assertTrue(hasGreeting, "Expected a greeting but got: '$result'")
    }

    @Test
    fun `debug simple tR accumulator`() {
        val parser = TemplateParser()
        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md",
            filePath = "/test.md"
        )

        val template = """
            <%*
            tR += "Hello";
            %>
        """.trimIndent()

        println("=== SIMPLE TEST INPUT ===")
        println(template)

        val result = parser.parse(template, context)

        println("=== SIMPLE TEST OUTPUT ===")
        println("'$result'")
        println("Length: ${result.length}")

        assertEquals("Hello", result.trim(), "Expected 'Hello' but got: '$result'")
    }
}
