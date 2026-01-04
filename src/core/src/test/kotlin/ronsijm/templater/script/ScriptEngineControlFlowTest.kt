package ronsijm.templater.script

import ronsijm.templater.TestContextFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*


class ScriptEngineControlFlowTest {

    @Test
    fun `test if statement basic`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.initializeResultAccumulator("")
        engine.execute("""
            let x = 5
            if (x < 10) {
                tR += "less than 10"
            }
        """.trimIndent())

        assertEquals("less than 10", engine.getResultAccumulator())
    }

    @Test
    fun `test if else statement`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.initializeResultAccumulator("")
        engine.execute("""
            let x = 15
            if (x < 10) {
                tR += "less than 10"
            } else {
                tR += "greater or equal to 10"
            }
        """.trimIndent())

        assertEquals("greater or equal to 10", engine.getResultAccumulator())
    }

    @Test
    fun `test if else if else statement`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.initializeResultAccumulator("")
        engine.execute("""
            let hour = 15
            if (hour < 12) {
                tR += "morning"
            } else if (hour < 18) {
                tR += "afternoon"
            } else {
                tR += "evening"
            }
        """.trimIndent())

        assertEquals("afternoon", engine.getResultAccumulator())
    }

    @Test
    fun `test new Date and getHours`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("const now = new Date()")
        val now = engine.getVariable("now")
        assertNotNull(now)

        engine.execute("const hour = now.getHours()")
        val hour = engine.getVariable("hour")
        assertNotNull(hour)
        assertTrue(hour is Int)
        assertTrue((hour as Int) in 0..23)
    }

    @Test
    fun `test greeting based on time`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.initializeResultAccumulator("")


        engine.execute("""
            const hour = 10
            if (hour < 12) {
                tR += "Good morning!"
            } else if (hour < 18) {
                tR += "Good afternoon!"
            } else {
                tR += "Good evening!"
            }
        """.trimIndent())

        assertEquals("Good morning!", engine.getResultAccumulator())
    }

    @Test
    fun `test comparison operators`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.initializeResultAccumulator("")


        engine.execute("""
            let x = 5
            if (x <= 5) {
                tR += "A"
            }
        """.trimIndent())


        engine.execute("""
            let y = 10
            if (y >= 10) {
                tR += "B"
            }
        """.trimIndent())


        engine.execute("""
            let z = 7
            if (z == 7) {
                tR += "C"
            }
        """.trimIndent())

        assertEquals("ABC", engine.getResultAccumulator())
    }

    @Test
    fun `test exact user example with new Date and if else`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.initializeResultAccumulator("")
        engine.execute("""
            const hour = new Date().getHours();
            if (hour < 12) {
                tR += "Good morning! ??";
            } else if (hour < 18) {
                tR += "Good afternoon! ???";
            } else {
                tR += "Good evening! ??";
            }
        """.trimIndent())

        val result = engine.getResultAccumulator()


        assertTrue(
            result.contains("Good morning!") ||
            result.contains("Good afternoon!") ||
            result.contains("Good evening!"),
            "Expected a greeting but got: $result"
        )
    }
}

