package ronsijm.templater.script.evaluators

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ronsijm.templater.script.ScriptContext

class ArrowFunctionHandlerTest {

    private val scriptContext = ScriptContext()


    private val simpleEvaluator: (String) -> Any? = { expr ->
        val trimmed = expr.trim()
        when {
            trimmed.startsWith("\"") && trimmed.endsWith("\"") -> trimmed.substring(1, trimmed.length - 1)
            trimmed.startsWith("'") && trimmed.endsWith("'") -> trimmed.substring(1, trimmed.length - 1)
            trimmed == "true" -> true
            trimmed == "false" -> false
            trimmed.contains("+") -> {
                val parts = trimmed.split("+").map { it.trim() }
                val left = parts[0].toIntOrNull() ?: scriptContext.getVariable(parts[0]) as? Int ?: 0
                val right = parts[1].toIntOrNull() ?: scriptContext.getVariable(parts[1]) as? Int ?: 0
                left + right
            }
            trimmed.contains("*") -> {
                val parts = trimmed.split("*").map { it.trim() }
                val left = parts[0].toIntOrNull() ?: scriptContext.getVariable(parts[0]) as? Int ?: 0
                val right = parts[1].toIntOrNull() ?: scriptContext.getVariable(parts[1]) as? Int ?: 0
                left * right
            }
            trimmed.contains(">") -> {
                val parts = trimmed.split(">").map { it.trim() }
                val left = parts[0].toIntOrNull() ?: scriptContext.getVariable(parts[0]) as? Int ?: 0
                val right = parts[1].toIntOrNull() ?: 0
                left > right
            }
            else -> trimmed.toIntOrNull() ?: scriptContext.getVariable(trimmed)
        }
    }

    private val handler = ArrowFunctionHandler(scriptContext, simpleEvaluator)


    @Test
    fun `isTopLevelArrowFunction returns true for simple arrow function`() {
        assertTrue(handler.isTopLevelArrowFunction("x => x + 1"))
    }

    @Test
    fun `isTopLevelArrowFunction returns true for parenthesized params`() {
        assertTrue(handler.isTopLevelArrowFunction("(x) => x + 1"))
    }

    @Test
    fun `isTopLevelArrowFunction returns true for multiple params`() {
        assertTrue(handler.isTopLevelArrowFunction("(a, b) => a + b"))
    }

    @Test
    fun `isTopLevelArrowFunction returns true for no params`() {
        assertTrue(handler.isTopLevelArrowFunction("() => 42"))
    }

    @Test
    fun `isTopLevelArrowFunction returns false for no arrow`() {
        assertFalse(handler.isTopLevelArrowFunction("x + 1"))
    }

    @Test
    fun `isTopLevelArrowFunction returns false for arrow in string`() {
        assertFalse(handler.isTopLevelArrowFunction("\"x => y\""))
    }

    @Test
    fun `isTopLevelArrowFunction returns false for arrow in parentheses`() {
        assertFalse(handler.isTopLevelArrowFunction("func(x => x)"))
    }


    @Test
    fun `parseArrowFunction parses simple arrow function`() {
        val fn = handler.parseArrowFunction("x => x + 1")
        assertEquals(listOf("x"), fn.parameters)
        assertEquals("x + 1", fn.body)
        assertTrue(fn.isExpression)
    }

    @Test
    fun `parseArrowFunction parses parenthesized single param`() {
        val fn = handler.parseArrowFunction("(x) => x * 2")
        assertEquals(listOf("x"), fn.parameters)
        assertEquals("x * 2", fn.body)
        assertTrue(fn.isExpression)
    }

    @Test
    fun `parseArrowFunction parses multiple params`() {
        val fn = handler.parseArrowFunction("(a, b) => a + b")
        assertEquals(listOf("a", "b"), fn.parameters)
        assertEquals("a + b", fn.body)
        assertTrue(fn.isExpression)
    }

    @Test
    fun `parseArrowFunction parses no params`() {
        val fn = handler.parseArrowFunction("() => 42")
        assertTrue(fn.parameters.isEmpty())
        assertEquals("42", fn.body)
        assertTrue(fn.isExpression)
    }

    @Test
    fun `parseArrowFunction parses block body`() {
        val fn = handler.parseArrowFunction("x => { return x + 1 }")
        assertEquals(listOf("x"), fn.parameters)
        assertEquals("return x + 1", fn.body)
        assertFalse(fn.isExpression)
    }


    @Test
    fun `executeArrowFunction executes simple expression`() {
        val fn = handler.parseArrowFunction("x => x + 1")
        val result = handler.executeArrowFunction(fn, listOf(5))
        assertEquals(6, result)
    }

    @Test
    fun `executeArrowFunction executes with multiple params`() {
        val fn = handler.parseArrowFunction("(a, b) => a + b")
        val result = handler.executeArrowFunction(fn, listOf(3, 4))
        assertEquals(7, result)
    }

    @Test
    fun `executeArrowFunction executes no-param function`() {
        val fn = handler.parseArrowFunction("() => 42")
        val result = handler.executeArrowFunction(fn, emptyList())
        assertEquals(42, result)
    }

    @Test
    fun `executeArrowFunction restores previous variable values`() {
        scriptContext.setVariable("x", 100)
        val fn = handler.parseArrowFunction("x => x + 1")
        handler.executeArrowFunction(fn, listOf(5))
        assertEquals(100, scriptContext.getVariable("x"))
    }

    @Test
    fun `executeArrowFunction removes temporary variables`() {
        val fn = handler.parseArrowFunction("temp => temp + 1")
        handler.executeArrowFunction(fn, listOf(5))
        assertNull(scriptContext.getVariable("temp"))
    }

    @Test
    fun `executeArrowFunction handles comparison`() {
        val fn = handler.parseArrowFunction("x => x > 5")
        val result1 = handler.executeArrowFunction(fn, listOf(10))
        val result2 = handler.executeArrowFunction(fn, listOf(3))
        assertEquals(true, result1)
        assertEquals(false, result2)
    }
}
