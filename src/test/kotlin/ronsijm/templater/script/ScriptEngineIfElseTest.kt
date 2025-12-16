package ronsijm.templater.script

import ronsijm.templater.TestContextFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.fail

class ScriptEngineIfElseTest {

    @Test
    fun `test if else with tR accumulator`() {
        val engine = ScriptEngine(TestContextFactory.create())

        // Initialize tR
        engine.initializeResultAccumulator("")

        // Execute the EXACT script from the user's file - NO INDENTATION
        val script = """const hour = new Date().getHours();
if (hour < 12) {
tR += "Good morning! ??";
} else if (hour < 18) {
tR += "Good afternoon! ???";
} else {
tR += "Good evening! ??";
}"""

        val result = engine.execute(script)
        val tRValue = engine.getResultAccumulator()

        // Should contain one of the greetings
        val hasGreeting = tRValue.contains("Good morning!") ||
                         tRValue.contains("Good afternoon!") ||
                         tRValue.contains("Good evening!")

        assertTrue(hasGreeting, "Expected tR to contain a greeting but got: '$tRValue'")
        assertFalse(tRValue.isEmpty(), "tR should not be empty")
    }
    
    @Test
    fun `test simple if with tR`() {
        val engine = ScriptEngine(TestContextFactory.create())

        engine.initializeResultAccumulator("")

        val script = """
            const x = 5;
            if (x > 3) {
                tR += "x is greater than 3";
            }
        """.trimIndent()

        engine.execute(script)
        val tRValue = engine.getResultAccumulator()

        // This should pass if tR is working
        assertEquals("x is greater than 3", tRValue)
    }
    
    @Test
    fun `test if else with tR - false condition`() {
        val engine = ScriptEngine(TestContextFactory.create())

        engine.initializeResultAccumulator("")

        val script = """
            const x = 2;
            if (x > 3) {
                tR += "x is greater than 3";
            } else {
                tR += "x is not greater than 3";
            }
        """.trimIndent()

        println("=== IF/ELSE FALSE CONDITION TEST ===")
        engine.execute(script)
        val tRValue = engine.getResultAccumulator()
        println("tR: '$tRValue'")

        assertEquals("x is not greater than 3", tRValue, "Expected tR to contain the else message")
    }

    @Test
    fun `test if with not null check - variable has value`() {
        val engine = ScriptEngine(TestContextFactory.create())

        engine.initializeResultAccumulator("")

        val script = """
            let calloutType = "note";
            if (calloutType != null) {
                tR += "callout is: " + calloutType;
            }
        """.trimIndent()

        engine.execute(script)
        val tRValue = engine.getResultAccumulator()

        assertEquals("callout is: note", tRValue, "Expected tR to contain the callout type")
    }

    @Test
    fun `test if with not null check - variable is null`() {
        val engine = ScriptEngine(TestContextFactory.create())

        engine.initializeResultAccumulator("")

        val script = """
            let calloutType = null;
            if (calloutType != null) {
                tR += "callout is: " + calloutType;
            } else {
                tR += "no callout selected";
            }
        """.trimIndent()

        engine.execute(script)
        val tRValue = engine.getResultAccumulator()

        assertEquals("no callout selected", tRValue, "Expected tR to contain the else message when variable is null")
    }

    @Test
    fun `test callout template pattern`() {
        val engine = ScriptEngine(TestContextFactory.create())

        engine.initializeResultAccumulator("")

        // Simulates the callout template pattern
        val script = """
            let calloutType = "warning";
            let foldState = "+";
            let title = "Important";
            let calloutContent = "This is the content";
            if (calloutType != null) {
                let content = "> [!" + calloutType + "]" + foldState + " " + title + "\n> " + calloutContent + "\n";
                tR += content;
            }
        """.trimIndent()

        engine.execute(script)
        val tRValue = engine.getResultAccumulator()

        assertTrue(tRValue.contains("> [!warning]+"), "Expected callout header")
        assertTrue(tRValue.contains("Important"), "Expected title")
        assertTrue(tRValue.contains("This is the content"), "Expected content")
    }
}

