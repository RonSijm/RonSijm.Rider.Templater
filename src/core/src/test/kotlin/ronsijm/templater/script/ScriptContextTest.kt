package ronsijm.templater.script

import ronsijm.templater.TestContextFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ScriptContextTest {

    @Test
    fun `test variable get and set`() {
        val context = ScriptContext()

        context.setVariable("x", 42)
        assertEquals(42, context.getVariable("x"))

        context.setVariable("name", "John")
        assertEquals("John", context.getVariable("name"))
    }

    @Test
    fun `test variable has and remove`() {
        val context = ScriptContext()

        context.setVariable("x", 10)
        assertTrue(context.hasVariable("x"))

        context.removeVariable("x")
        assertFalse(context.hasVariable("x"))
        assertNull(context.getVariable("x"))
    }

    @Test
    fun `test get non-existent variable returns null`() {
        val context = ScriptContext()

        assertNull(context.getVariable("nonExistent"))
        assertFalse(context.hasVariable("nonExistent"))
    }

    @Test
    fun `test result accumulator initialization`() {
        val context = ScriptContext()

        context.initializeResultAccumulator("Initial content")
        assertEquals("Initial content", context.getResultAccumulator())
    }

    @Test
    fun `test result accumulator append`() {
        val context = ScriptContext()

        context.initializeResultAccumulator("Start")
        context.appendToResult(" Middle")
        context.appendToResult(" End")

        assertEquals("Start Middle End", context.getResultAccumulator())
    }

    @Test
    fun `test result accumulator set`() {
        val context = ScriptContext()

        context.initializeResultAccumulator("Initial")
        context.setResult("Replaced")

        assertEquals("Replaced", context.getResultAccumulator())
    }

    @Test
    fun `test module instances are created internally`() {

        val registry = TestContextFactory.createModuleRegistry()
        val result = registry.executeFunction("tp.date.now", listOf("yyyy-MM-dd"))

        assertNotNull(result)
        assertTrue(result is String)
    }

    @Test
    fun `test multiple variables`() {
        val context = ScriptContext()

        context.setVariable("a", 1)
        context.setVariable("b", 2)
        context.setVariable("c", 3)

        assertEquals(1, context.getVariable("a"))
        assertEquals(2, context.getVariable("b"))
        assertEquals(3, context.getVariable("c"))
    }

    @Test
    fun `test variable overwrite`() {
        val context = ScriptContext()

        context.setVariable("x", "first")
        assertEquals("first", context.getVariable("x"))

        context.setVariable("x", "second")
        assertEquals("second", context.getVariable("x"))
    }

    @Test
    fun `test result accumulator with empty string`() {
        val context = ScriptContext()

        context.initializeResultAccumulator("")
        assertEquals("", context.getResultAccumulator())

        context.appendToResult("Content")
        assertEquals("Content", context.getResultAccumulator())
    }

    @Test
    fun `test variables with different types`() {
        val context = ScriptContext()

        context.setVariable("int", 42)
        context.setVariable("string", "hello")
        context.setVariable("boolean", true)
        context.setVariable("double", 3.14)

        assertEquals(42, context.getVariable("int"))
        assertEquals("hello", context.getVariable("string"))
        assertEquals(true, context.getVariable("boolean"))
        assertEquals(3.14, context.getVariable("double"))
    }
}
