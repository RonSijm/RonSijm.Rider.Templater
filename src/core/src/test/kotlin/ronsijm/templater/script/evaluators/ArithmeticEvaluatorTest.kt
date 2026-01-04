package ronsijm.templater.script.evaluators

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ArithmeticEvaluatorTest {


    private val simpleEvaluator: (String) -> Any? = { expr ->
        val trimmed = expr.trim()
        when {
            trimmed.startsWith("\"") && trimmed.endsWith("\"") -> trimmed.substring(1, trimmed.length - 1)
            trimmed.startsWith("'") && trimmed.endsWith("'") -> trimmed.substring(1, trimmed.length - 1)
            else -> trimmed.toIntOrNull() ?: trimmed.toDoubleOrNull() ?: trimmed
        }
    }

    private val literalParser = LiteralParser(simpleEvaluator)
    private val evaluator = ArithmeticEvaluator(simpleEvaluator, literalParser)


    @Test
    fun `findArithmeticOperator finds addition`() {
        val result = evaluator.findArithmeticOperator("5 + 3")
        assertNotNull(result)
        assertEquals('+', result?.first)
    }

    @Test
    fun `findArithmeticOperator finds subtraction`() {
        val result = evaluator.findArithmeticOperator("10 - 4")
        assertNotNull(result)
        assertEquals('-', result?.first)
    }

    @Test
    fun `findArithmeticOperator finds multiplication`() {
        val result = evaluator.findArithmeticOperator("6 * 7")
        assertNotNull(result)
        assertEquals('*', result?.first)
    }

    @Test
    fun `findArithmeticOperator finds division`() {
        val result = evaluator.findArithmeticOperator("20 / 4")
        assertNotNull(result)
        assertEquals('/', result?.first)
    }

    @Test
    fun `findArithmeticOperator ignores operators in quotes`() {
        val result = evaluator.findArithmeticOperator("\"5 + 3\"")
        assertNull(result)
    }

    @Test
    fun `findArithmeticOperator ignores operators in parentheses`() {
        val result = evaluator.findArithmeticOperator("(5 + 3)")
        assertNull(result)
    }

    @Test
    fun `findArithmeticOperator returns null for no operator`() {
        val result = evaluator.findArithmeticOperator("42")
        assertNull(result)
    }


    @Test
    fun `evaluateArithmetic adds integers`() {
        val result = evaluator.evaluateArithmetic("5 + 3", Pair('+', 2))
        assertEquals(8, result)
    }

    @Test
    fun `evaluateArithmetic subtracts integers`() {
        val result = evaluator.evaluateArithmetic("10 - 4", Pair('-', 3))
        assertEquals(6, result)
    }

    @Test
    fun `evaluateArithmetic multiplies integers`() {
        val result = evaluator.evaluateArithmetic("6 * 7", Pair('*', 2))
        assertEquals(42, result)
    }

    @Test
    fun `evaluateArithmetic divides integers evenly`() {
        val result = evaluator.evaluateArithmetic("20 / 4", Pair('/', 3))
        assertEquals(5, result)
    }

    @Test
    fun `evaluateArithmetic divides with remainder returns double`() {
        val result = evaluator.evaluateArithmetic("7 / 2", Pair('/', 2))
        assertEquals(3.5, result)
    }

    @Test
    fun `evaluateArithmetic adds doubles`() {
        val result = evaluator.evaluateArithmetic("3.5 + 2.5", Pair('+', 4))
        assertEquals(6.0, result)
    }

    @Test
    fun `evaluateArithmetic concatenates strings with plus`() {
        val result = evaluator.evaluateArithmetic("\"hello\" + \"world\"", Pair('+', 8))
        assertEquals("helloworld", result)
    }

    @Test
    fun `evaluateArithmetic concatenates string and number`() {
        val result = evaluator.evaluateArithmetic("\"value: \" + 42", Pair('+', 10))
        assertEquals("value: 42", result)
    }


    @Test
    fun `findComparisonOperator finds greater than`() {
        val result = evaluator.findComparisonOperator("5 > 3")
        assertNotNull(result)
        assertEquals(">", result?.first)
    }

    @Test
    fun `findComparisonOperator finds less than`() {
        val result = evaluator.findComparisonOperator("3 < 5")
        assertNotNull(result)
        assertEquals("<", result?.first)
    }

    @Test
    fun `findComparisonOperator finds greater than or equal`() {
        val result = evaluator.findComparisonOperator("5 >= 3")
        assertNotNull(result)
        assertEquals(">=", result?.first)
    }

    @Test
    fun `findComparisonOperator finds less than or equal`() {
        val result = evaluator.findComparisonOperator("3 <= 5")
        assertNotNull(result)
        assertEquals("<=", result?.first)
    }

    @Test
    fun `findComparisonOperator finds equality`() {
        val result = evaluator.findComparisonOperator("5 == 5")
        assertNotNull(result)
        assertEquals("==", result?.first)
    }

    @Test
    fun `findComparisonOperator finds inequality`() {
        val result = evaluator.findComparisonOperator("5 != 3")
        assertNotNull(result)
        assertEquals("!=", result?.first)
    }

    @Test
    fun `findComparisonOperator ignores operators in quotes`() {
        val result = evaluator.findComparisonOperator("\"5 > 3\"")
        assertNull(result)
    }


    @Test
    fun `evaluateComparison greater than true`() {
        val result = evaluator.evaluateComparison("5 > 3", Pair(">", 2))
        assertTrue(result)
    }

    @Test
    fun `evaluateComparison greater than false`() {
        val result = evaluator.evaluateComparison("3 > 5", Pair(">", 2))
        assertFalse(result)
    }

    @Test
    fun `evaluateComparison less than true`() {
        val result = evaluator.evaluateComparison("3 < 5", Pair("<", 2))
        assertTrue(result)
    }

    @Test
    fun `evaluateComparison less than false`() {
        val result = evaluator.evaluateComparison("5 < 3", Pair("<", 2))
        assertFalse(result)
    }

    @Test
    fun `evaluateComparison greater than or equal true`() {
        val result = evaluator.evaluateComparison("5 >= 5", Pair(">=", 2))
        assertTrue(result)
    }

    @Test
    fun `evaluateComparison less than or equal true`() {
        val result = evaluator.evaluateComparison("5 <= 5", Pair("<=", 2))
        assertTrue(result)
    }

    @Test
    fun `evaluateComparison equality true for numbers`() {
        val result = evaluator.evaluateComparison("5 == 5", Pair("==", 2))
        assertTrue(result)
    }

    @Test
    fun `evaluateComparison equality false for different numbers`() {
        val result = evaluator.evaluateComparison("5 == 3", Pair("==", 2))
        assertFalse(result)
    }

    @Test
    fun `evaluateComparison inequality true`() {
        val result = evaluator.evaluateComparison("5 != 3", Pair("!=", 2))
        assertTrue(result)
    }

    @Test
    fun `evaluateComparison inequality false`() {
        val result = evaluator.evaluateComparison("5 != 5", Pair("!=", 2))
        assertFalse(result)
    }

    @Test
    fun `evaluateComparison string equality`() {
        val result = evaluator.evaluateComparison("\"hello\" == \"hello\"", Pair("==", 8))
        assertTrue(result)
    }

    @Test
    fun `evaluateComparison string inequality`() {
        val result = evaluator.evaluateComparison("\"hello\" != \"world\"", Pair("!=", 8))
        assertTrue(result)
    }


    @Test
    fun `evaluateArithmetic with negative result`() {
        val result = evaluator.evaluateArithmetic("3 - 10", Pair('-', 2))
        assertEquals(-7, result)
    }

    @Test
    fun `evaluateArithmetic division by zero returns infinity`() {
        val result = evaluator.evaluateArithmetic("5 / 0", Pair('/', 2))
        assertEquals(Double.POSITIVE_INFINITY, result)
    }

    @Test
    fun `evaluateComparison with doubles`() {
        val result = evaluator.evaluateComparison("3.5 > 3.4", Pair(">", 4))
        assertTrue(result)
    }
}
