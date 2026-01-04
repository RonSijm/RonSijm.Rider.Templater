package ronsijm.templater.script

import ronsijm.templater.TestContextFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*


class ScriptEngineTemplateLiteralTest {

    @Test
    fun `test template literal with variable interpolation`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("let i = 5")
        val result = engine.evaluateExpression("`Item number ${'$'}{i}`")

        assertEquals("Item number 5", result)
    }

    @Test
    fun `test template literal with newline escape`() {
        val engine = TestContextFactory.createScriptEngine()

        val result = engine.evaluateExpression("`Line 1\\nLine 2`")

        assertEquals("Line 1\nLine 2", result)
    }

    @Test
    fun `test template literal with tab escape`() {
        val engine = TestContextFactory.createScriptEngine()

        val result = engine.evaluateExpression("`Col1\\tCol2`")

        assertEquals("Col1\tCol2", result)
    }

    @Test
    fun `test tR accumulator initialization`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.initializeResultAccumulator("Initial content")

        assertEquals("Initial content", engine.getResultAccumulator())
    }

    @Test
    fun `test tR accumulator append`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.initializeResultAccumulator("")
        engine.execute("""
            tR += "Hello"
            tR += " World"
        """.trimIndent())

        assertEquals("Hello World", engine.getResultAccumulator())
    }

    @Test
    fun `test tR accumulator set`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.initializeResultAccumulator("Old content")
        engine.execute("tR = \"New content\"")

        assertEquals("New content", engine.getResultAccumulator())
    }
}

