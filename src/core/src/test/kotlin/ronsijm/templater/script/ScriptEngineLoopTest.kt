package ronsijm.templater.script

import ronsijm.templater.TestContextFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*


class ScriptEngineLoopTest {

    @Test
    fun `test for loop basic`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.initializeResultAccumulator("")
        engine.execute("""
            for (let i = 1; i <= 3; i++) {
                tR += `${'$'}{i}`
            }
        """.trimIndent())

        assertEquals("123", engine.getResultAccumulator())
    }

    @Test
    fun `test for loop with template literal`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.initializeResultAccumulator("")
        engine.execute("""
            for (let i = 1; i <= 5; i++) {
                tR += `${'$'}{i}. Item number ${'$'}{i}\n`
            }
        """.trimIndent())

        val expected = "1. Item number 1\n2. Item number 2\n3. Item number 3\n4. Item number 4\n5. Item number 5\n"
        assertEquals(expected, engine.getResultAccumulator())
    }

    @Test
    fun `test for loop with less than condition`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.initializeResultAccumulator("")
        engine.execute("""
            for (let i = 0; i < 3; i++) {
                tR += `${'$'}{i}`
            }
        """.trimIndent())

        assertEquals("012", engine.getResultAccumulator())
    }

    @Test
    fun `test for loop with greater than condition`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.initializeResultAccumulator("")
        engine.execute("""
            for (let i = 5; i > 2; i--) {
                tR += `${'$'}{i}`
            }
        """.trimIndent())

        assertEquals("543", engine.getResultAccumulator())
    }

    @Test
    fun `test for loop with greater than or equal condition`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.initializeResultAccumulator("")
        engine.execute("""
            for (let i = 3; i >= 1; i--) {
                tR += `${'$'}{i}`
            }
        """.trimIndent())

        assertEquals("321", engine.getResultAccumulator())
    }

    @Test
    fun `test for loop with comments`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.initializeResultAccumulator("")
        engine.execute("""
            // Generate a numbered list
            for (let i = 1; i <= 3; i++) {
                // Add item to result
                tR += `${'$'}{i}. Item\n`
            }
        """.trimIndent())

        assertEquals("1. Item\n2. Item\n3. Item\n", engine.getResultAccumulator())
    }

    @Test
    fun `test for loop with variable in body`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.initializeResultAccumulator("")
        engine.execute("""
            let prefix = "Task"
            for (let i = 1; i <= 2; i++) {
                tR += `${'$'}{prefix} ${'$'}{i}\n`
            }
        """.trimIndent())

        assertEquals("Task 1\nTask 2\n", engine.getResultAccumulator())
    }

    @Test
    fun `test while loop`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            let count = 0
            while (count < 5) {
                count++
            }
        """.trimIndent())

        assertEquals(5, engine.getVariable("count"))
    }

    @Test
    fun `test while loop with string concatenation`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            let result = ""
            let i = 0
            while (i < 3) {
                result += "x"
                i++
            }
        """.trimIndent())

        assertEquals("xxx", engine.getVariable("result"))
    }

    @Test
    fun `test nested for loops`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            let sum = 0
            for (let i = 0; i < 3; i++) {
                sum += 1
            }
        """.trimIndent())

        val sum = engine.getVariable("sum")
        assertEquals(3, (sum as Number).toInt())
    }
}

