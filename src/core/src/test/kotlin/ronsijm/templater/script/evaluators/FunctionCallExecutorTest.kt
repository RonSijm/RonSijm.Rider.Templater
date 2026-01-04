package ronsijm.templater.script.evaluators

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ronsijm.templater.common.CommandResult
import ronsijm.templater.common.FrontmatterAccess
import ronsijm.templater.common.ModuleExecutor
import ronsijm.templater.common.OkValueResult
import ronsijm.templater.script.ArrowFunction
import ronsijm.templater.script.ModuleRegistry
import ronsijm.templater.script.ScriptContext

class FunctionCallExecutorTest {

    private val scriptContext = ScriptContext()


    private val mockFrontmatterAccess = object : FrontmatterAccess {
        override fun getValue(parts: List<String>): Any? = null
        override fun getAll(): Map<String, Any> = emptyMap()
    }


    private val mockModuleExecutor = object : ModuleExecutor {
        override fun executeModuleFunction(module: String, function: String, args: List<Any?>): CommandResult {
            return OkValueResult("mock result")
        }
    }

    private val moduleRegistry = ModuleRegistry(mockFrontmatterAccess, mockModuleExecutor)


    private val simpleEvaluator: (String) -> Any? = { expr ->
        val trimmed = expr.trim()
        when {
            trimmed.startsWith("\"") && trimmed.endsWith("\"") -> trimmed.substring(1, trimmed.length - 1)
            trimmed.startsWith("'") && trimmed.endsWith("'") -> trimmed.substring(1, trimmed.length - 1)
            trimmed == "true" -> true
            trimmed == "false" -> false
            else -> trimmed.toIntOrNull() ?: scriptContext.getVariable(trimmed)
        }
    }

    private val arrowFunctionExecutor: (ArrowFunction, List<Any?>) -> Any? = { fn, args ->

        args.firstOrNull()
    }

    private val executor = FunctionCallExecutor(
        scriptContext,
        moduleRegistry,
        simpleEvaluator,
        arrowFunctionExecutor
    )


    @Test
    fun `parseArguments returns empty list for blank string`() {
        val result = executor.parseArguments("")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseArguments parses single argument`() {
        val result = executor.parseArguments("42")
        assertEquals(listOf(42), result)
    }

    @Test
    fun `parseArguments parses multiple arguments`() {
        val result = executor.parseArguments("1, 2, 3")
        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun `parseArguments parses string arguments`() {
        val result = executor.parseArguments("\"hello\", \"world\"")
        assertEquals(listOf("hello", "world"), result)
    }

    @Test
    fun `parseArguments handles nested parentheses`() {
        val result = executor.parseArguments("func(1, 2), 3")
        assertEquals(2, result.size)
    }

    @Test
    fun `parseArguments handles nested brackets`() {
        val result = executor.parseArguments("[1, 2], 3")
        assertEquals(2, result.size)
    }


    @Test
    fun `executeFunctionCall Object_keys returns map keys`() {
        val map = mapOf("a" to 1, "b" to 2)
        val result = executor.executeFunctionCall("Object.keys", listOf(map))
        assertEquals(listOf("a", "b"), result)
    }

    @Test
    fun `executeFunctionCall Object_values returns map values`() {
        val map = mapOf("a" to 1, "b" to 2)
        val result = executor.executeFunctionCall("Object.values", listOf(map))
        assertEquals(listOf(1, 2), result)
    }

    @Test
    fun `executeFunctionCall Object_entries returns key-value pairs`() {
        val map = mapOf("a" to 1)
        val result = executor.executeFunctionCall("Object.entries", listOf(map)) as List<*>
        assertEquals(1, result.size)
        val entry = result[0] as List<*>
        assertEquals("a", entry[0])
        assertEquals(1, entry[1])
    }


    @Test
    fun `executeFunctionCall calls string method`() {
        scriptContext.setVariable("str", "hello")
        val result = executor.executeFunctionCall("str.toUpperCase", emptyList())
        assertEquals("HELLO", result)
    }


    @Test
    fun `executeFunctionCall calls array method`() {
        scriptContext.setVariable("arr", listOf("a", "b", "c"))
        val result = executor.executeFunctionCall("arr.join", listOf("-"))
        assertEquals("a-b-c", result)
    }


    @Test
    fun `executeFunctionCall accesses map property`() {
        scriptContext.setVariable("obj", mapOf("name" to "John"))
        val result = executor.executeFunctionCall("obj.name", emptyList())
        assertEquals("John", result)
    }


    @Test
    fun `executeFunctionCall executes arrow function variable`() {
        val fn = ArrowFunction(listOf("x"), "x + 1", true)
        scriptContext.setVariable("myFunc", fn)
        val result = executor.executeFunctionCall("myFunc", listOf(5))
        assertEquals(5, result)
    }


    @Test
    fun `DateObject getHours returns valid hour`() {
        val dateObj = DateObject()
        val hours = dateObj.getHours()
        assertTrue(hours in 0..23)
    }

    @Test
    fun `DateObject getMinutes returns valid minute`() {
        val dateObj = DateObject()
        val minutes = dateObj.getMinutes()
        assertTrue(minutes in 0..59)
    }

    @Test
    fun `DateObject getSeconds returns valid second`() {
        val dateObj = DateObject()
        val seconds = dateObj.getSeconds()
        assertTrue(seconds in 0..59)
    }

    @Test
    fun `DateObject getDate returns valid day`() {
        val dateObj = DateObject()
        val day = dateObj.getDate()
        assertTrue(day in 1..31)
    }

    @Test
    fun `DateObject getMonth returns zero-indexed month`() {
        val dateObj = DateObject()
        val month = dateObj.getMonth()
        assertTrue(month in 0..11)
    }

    @Test
    fun `DateObject getFullYear returns valid year`() {
        val dateObj = DateObject()
        val year = dateObj.getFullYear()
        assertTrue(year >= 2024)
    }

    @Test
    fun `DateObject getDay returns valid day of week`() {
        val dateObj = DateObject()
        val day = dateObj.getDay()
        assertTrue(day in 0..6)
    }

    @Test
    fun `DateObject toString returns readable date string`() {
        val dateObj = DateObject()
        val str = dateObj.toString()


        assertFalse(str.contains("@"), "toString should not contain @ (default object hash)")
        assertTrue(str.contains("202"), "toString should contain year")
    }
}
