package ronsijm.templater.script

import ronsijm.templater.TestContextFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*


class ScriptEngineOperatorTest {

    @Test
    fun `test ternary operator`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            let x = 5
            let result = x > 3 ? "big" : "small"
        """.trimIndent())

        assertEquals("big", engine.getVariable("result"))
    }

    @Test
    fun `test ternary operator false branch`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            let x = 1
            let result = x > 3 ? "big" : "small"
        """.trimIndent())

        assertEquals("small", engine.getVariable("result"))
    }

    @Test
    fun `test typeof undefined variable`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            let result = typeof undefinedVar
        """.trimIndent())

        assertEquals("undefined", engine.getVariable("result"))
    }

    @Test
    fun `test typeof with comparison`() {
        val engine = TestContextFactory.createScriptEngine()


        engine.execute("""
            let typeResult = typeof performance
        """.trimIndent())
        assertEquals("undefined", engine.getVariable("typeResult"))


        engine.execute("""
            let result = typeof undefinedVar !== "undefined" ? "defined" : "not defined"
        """.trimIndent())

        assertEquals("not defined", engine.getVariable("result"))
    }

    @Test
    fun `test strict equality operator`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            let a = 9
            let result1 = a === 9
            let result2 = a === 10
        """.trimIndent())

        assertEquals(true, engine.getVariable("result1"))
        assertEquals(false, engine.getVariable("result2"))
    }

    @Test
    fun `test compound assignment operators`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            let x = 5
            x += 3
            let y = 10
            y -= 4
        """.trimIndent())

        assertEquals(8.0, engine.getVariable("x"))
        assertEquals(6.0, engine.getVariable("y"))
    }

    @Test
    fun `test string repeat`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            let str = "0"
            let result = str.repeat(5)
        """.trimIndent())

        assertEquals("00000", engine.getVariable("result"))
    }

    @Test
    fun `test string literal method call`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            let result = "0".repeat(5)
        """.trimIndent())

        assertEquals("00000", engine.getVariable("result"))
    }

    @Test
    fun `test Math floor`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            let result = Math.floor(3.7)
        """.trimIndent())

        assertEquals(3, engine.getVariable("result"))
    }
}

