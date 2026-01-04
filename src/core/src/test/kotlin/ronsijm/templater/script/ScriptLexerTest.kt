package ronsijm.templater.script

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ScriptLexerTest {

    private val lexer = ScriptLexer()

    @Test
    fun `test preprocess script removes single line comments`() {
        val script = """
            let x = 5
            // This is a comment
            let y = 10
        """.trimIndent()

        val result = lexer.preprocessScript(script)

        assertFalse(result.contains("// This is a comment"))
        assertTrue(result.contains("let x = 5"))
        assertTrue(result.contains("let y = 10"))
    }

    @Test
    fun `test preprocess script removes multi line comments`() {
        val script = """
            let x = 5
            /* This is a
               multi-line comment */
            let y = 10
        """.trimIndent()

        val result = lexer.preprocessScript(script)

        assertFalse(result.contains("/* This is a"))
        assertFalse(result.contains("multi-line comment */"))
        assertTrue(result.contains("let x = 5"))
        assertTrue(result.contains("let y = 10"))
    }

    @Test
    fun `test preprocess script strips await keyword`() {
        val script = "let result = await tp.system.prompt(\"Enter name\")"

        val result = lexer.preprocessScript(script)

        assertFalse(result.contains("await"))
        assertTrue(result.contains("let result = tp.system.prompt(\"Enter name\")"))
    }

    @Test
    fun `test smart split statements by newline`() {
        val script = """
            let x = 5
            let y = 10
            let z = 15
        """.trimIndent()

        val statements = lexer.smartSplitStatements(script)

        assertEquals(3, statements.size)
        assertEquals("let x = 5", statements[0])
        assertEquals("let y = 10", statements[1])
        assertEquals("let z = 15", statements[2])
    }

    @Test
    fun `test smart split statements by semicolon`() {
        val script = "let x = 5; let y = 10; let z = 15"

        val statements = lexer.smartSplitStatements(script)

        assertEquals(3, statements.size)
        assertEquals("let x = 5", statements[0])
        assertEquals("let y = 10", statements[1])
        assertEquals("let z = 15", statements[2])
    }

    @Test
    fun `test smart split respects parentheses`() {
        val script = "let result = tp.system.prompt(\"Enter; name\")"

        val statements = lexer.smartSplitStatements(script)

        assertEquals(1, statements.size)
        assertEquals("let result = tp.system.prompt(\"Enter; name\")", statements[0])
    }

    @Test
    fun `test smart split respects quotes`() {
        val script = "let message = \"Hello\nWorld\""

        val statements = lexer.smartSplitStatements(script)

        assertEquals(1, statements.size)
        assertTrue(statements[0].contains("Hello\nWorld"))
    }

    @Test
    fun `test smart split with braces`() {
        val script = """
            if (x > 5) {
                tR += "yes"
            }
        """.trimIndent()

        val statements = lexer.smartSplitStatements(script)


        assertEquals(1, statements.size)
        assertTrue(statements[0].startsWith("if"))
        assertTrue(statements[0].contains("tR += \"yes\""))
    }

    @Test
    fun `test preprocess empty script`() {
        val result = lexer.preprocessScript("")
        assertEquals("", result)
    }

    @Test
    fun `test smart split empty script`() {
        val statements = lexer.smartSplitStatements("")
        assertEquals(0, statements.size)
    }

    @Test
    fun `test preprocess script with only comments`() {
        val script = """
            // Comment 1
            /* Comment 2 */
        """.trimIndent()

        val result = lexer.preprocessScript(script)

        assertTrue(result.trim().isEmpty() || result.trim() == "\n")
    }

    @Test
    fun `test smart split with mixed separators`() {
        val script = """
            let x = 5; let y = 10
            let z = 15
        """.trimIndent()

        val statements = lexer.smartSplitStatements(script)

        assertEquals(3, statements.size)
        assertEquals("let x = 5", statements[0])
        assertEquals("let y = 10", statements[1])
        assertEquals("let z = 15", statements[2])
    }

    @Test
    fun `test preprocess preserves string content`() {
        val script = "let message = \"This // is not a comment\""

        val result = lexer.preprocessScript(script)

        assertTrue(result.contains("This // is not a comment"))
    }
}
