package ronsijm.templater.script.executors

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConditionEvaluatorTest {

    private fun createEvaluator(variables: Map<String, Any?> = emptyMap()): ConditionEvaluator {
        return ConditionEvaluator { expr ->
            val trimmed = expr.trim()

            when {
                trimmed == "true" -> true
                trimmed == "false" -> false
                trimmed == "null" -> null
                trimmed == "0" -> 0
                trimmed == "1" -> 1
                trimmed == "5" -> 5
                trimmed == "10" -> 10
                trimmed == "\"\"" -> ""
                trimmed == "\"hello\"" -> "hello"
                trimmed == "\"world\"" -> "world"
                trimmed.toIntOrNull() != null -> trimmed.toInt()
                trimmed.toDoubleOrNull() != null -> trimmed.toDouble()
                trimmed.startsWith("\"") && trimmed.endsWith("\"") ->
                    trimmed.substring(1, trimmed.length - 1)
                variables.containsKey(trimmed) -> variables[trimmed]
                else -> trimmed
            }
        }
    }

    @Test
    fun `test strict equality with same values`() {
        val evaluator = createEvaluator()
        assertTrue(evaluator.evaluate("5 === 5"))
        assertTrue(evaluator.evaluate("\"hello\" === \"hello\""))
    }

    @Test
    fun `test strict equality with different values`() {
        val evaluator = createEvaluator()
        assertFalse(evaluator.evaluate("5 === 10"))
        assertFalse(evaluator.evaluate("\"hello\" === \"world\""))
    }

    @Test
    fun `test strict inequality`() {
        val evaluator = createEvaluator()
        assertTrue(evaluator.evaluate("5 !==10"))
        assertFalse(evaluator.evaluate("5 !== 5"))
    }

    @Test
    fun `test less than`() {
        val evaluator = createEvaluator()
        assertTrue(evaluator.evaluate("5 < 10"))
        assertFalse(evaluator.evaluate("10 < 5"))
        assertFalse(evaluator.evaluate("5 < 5"))
    }

    @Test
    fun `test less than or equal`() {
        val evaluator = createEvaluator()
        assertTrue(evaluator.evaluate("5 <= 10"))
        assertTrue(evaluator.evaluate("5 <= 5"))
        assertFalse(evaluator.evaluate("10 <= 5"))
    }

    @Test
    fun `test greater than`() {
        val evaluator = createEvaluator()
        assertTrue(evaluator.evaluate("10 > 5"))
        assertFalse(evaluator.evaluate("5 > 10"))
        assertFalse(evaluator.evaluate("5 > 5"))
    }

    @Test
    fun `test greater than or equal`() {
        val evaluator = createEvaluator()
        assertTrue(evaluator.evaluate("10 >= 5"))
        assertTrue(evaluator.evaluate("5 >= 5"))
        assertFalse(evaluator.evaluate("5 >= 10"))
    }

    @Test
    fun `test equality`() {
        val evaluator = createEvaluator()
        assertTrue(evaluator.evaluate("5 == 5"))
        assertFalse(evaluator.evaluate("5 == 10"))
    }

    @Test
    fun `test inequality`() {
        val evaluator = createEvaluator()
        assertTrue(evaluator.evaluate("5 != 10"))
        assertFalse(evaluator.evaluate("5 != 5"))
    }

    @Test
    fun `test truthy with boolean true`() {
        val evaluator = createEvaluator()
        assertTrue(evaluator.evaluate("true"))
    }

    @Test
    fun `test falsy with boolean false`() {
        val evaluator = createEvaluator()
        assertFalse(evaluator.evaluate("false"))
    }

    @Test
    fun `test falsy with null`() {
        val evaluator = createEvaluator()
        assertFalse(evaluator.evaluate("null"))
    }

    @Test
    fun `test falsy with zero`() {
        val evaluator = createEvaluator()
        assertFalse(evaluator.evaluate("0"))
    }

    @Test
    fun `test truthy with non-zero number`() {
        val evaluator = createEvaluator()
        assertTrue(evaluator.evaluate("1"))
        assertTrue(evaluator.evaluate("5"))
    }

    @Test
    fun `test falsy with empty string`() {
        val evaluator = createEvaluator()
        assertFalse(evaluator.evaluate("\"\""))
    }

    @Test
    fun `test truthy with non-empty string`() {
        val evaluator = createEvaluator()
        assertTrue(evaluator.evaluate("\"hello\""))
    }

    @Test
    fun `test string comparison`() {
        val evaluator = createEvaluator()
        assertTrue(evaluator.evaluate("\"hello\" < \"world\""))
        assertFalse(evaluator.evaluate("\"world\" < \"hello\""))
    }

    @Test
    fun `test comparison with variables`() {
        val evaluator = createEvaluator(mapOf("x" to 5, "y" to 10))
        assertTrue(evaluator.evaluate("x < y"))
        assertFalse(evaluator.evaluate("y < x"))
    }
}
