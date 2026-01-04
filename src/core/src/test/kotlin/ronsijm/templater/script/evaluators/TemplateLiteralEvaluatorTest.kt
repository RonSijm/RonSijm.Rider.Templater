package ronsijm.templater.script.evaluators

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TemplateLiteralEvaluatorTest {


    private val variables = mutableMapOf<String, Any?>(
        "name" to "World",
        "count" to 42,
        "flag" to true
    )

    private val simpleEvaluator: (String) -> Any? = { expr ->
        val trimmed = expr.trim()
        when {
            trimmed.startsWith("\"") && trimmed.endsWith("\"") -> trimmed.substring(1, trimmed.length - 1)
            trimmed.startsWith("'") && trimmed.endsWith("'") -> trimmed.substring(1, trimmed.length - 1)
            trimmed == "true" -> true
            trimmed == "false" -> false
            trimmed.contains("+") -> {
                val parts = trimmed.split("+").map { it.trim() }
                val left = parts[0].toIntOrNull() ?: variables[parts[0]] as? Int ?: 0
                val right = parts[1].toIntOrNull() ?: variables[parts[1]] as? Int ?: 0
                left + right
            }
            else -> trimmed.toIntOrNull() ?: variables[trimmed]
        }
    }

    private val evaluator = TemplateLiteralEvaluator(simpleEvaluator)


    @Test
    fun `evaluates simple template literal without interpolation`() {
        val result = evaluator.evaluate("`Hello World`")
        assertEquals("Hello World", result)
    }

    @Test
    fun `evaluates empty template literal`() {
        val result = evaluator.evaluate("``")
        assertEquals("", result)
    }

    @Test
    fun `evaluates template literal with single interpolation`() {
        val result = evaluator.evaluate("`Hello \${name}!`")
        assertEquals("Hello World!", result)
    }

    @Test
    fun `evaluates template literal with multiple interpolations`() {
        val result = evaluator.evaluate("`Name: \${name}, Count: \${count}`")
        assertEquals("Name: World, Count: 42", result)
    }

    @Test
    fun `evaluates template literal with number interpolation`() {
        val result = evaluator.evaluate("`The answer is \${count}`")
        assertEquals("The answer is 42", result)
    }

    @Test
    fun `evaluates template literal with boolean interpolation`() {
        val result = evaluator.evaluate("`Flag is \${flag}`")
        assertEquals("Flag is true", result)
    }


    @Test
    fun `evaluates template literal with expression`() {
        val result = evaluator.evaluate("`Sum: \${1 + 2}`")
        assertEquals("Sum: 3", result)
    }

    @Test
    fun `evaluates template literal with variable expression`() {
        val result = evaluator.evaluate("`Result: \${count + 8}`")
        assertEquals("Result: 50", result)
    }


    @Test
    fun `handles null value in interpolation`() {
        val result = evaluator.evaluate("`Value: \${undefined}`")
        assertEquals("Value: ", result)
    }

    @Test
    fun `handles interpolation at start`() {
        val result = evaluator.evaluate("`\${name} says hello`")
        assertEquals("World says hello", result)
    }

    @Test
    fun `handles interpolation at end`() {
        val result = evaluator.evaluate("`Hello \${name}`")
        assertEquals("Hello World", result)
    }

    @Test
    fun `handles consecutive interpolations`() {
        val result = evaluator.evaluate("`\${name}\${count}`")
        assertEquals("World42", result)
    }

    @Test
    fun `handles nested braces in interpolation`() {

        val result = evaluator.evaluate("`Value: \${count}`")
        assertEquals("Value: 42", result)
    }

    @Test
    fun `handles dollar sign without brace`() {
        val result = evaluator.evaluate("`Price: \$100`")
        assertEquals("Price: \$100", result)
    }
}
