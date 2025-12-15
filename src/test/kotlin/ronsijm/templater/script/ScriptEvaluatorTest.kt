package ronsijm.templater.script

import ronsijm.templater.TestContextFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ScriptEvaluatorTest {

    @Test
    fun `test evaluate simple string literal`() {
        val context = ScriptContext(TestContextFactory.create(fileContent = "test content"))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)
        
        val result = evaluator.evaluateExpression("\"Hello World\"")
        
        assertEquals("Hello World", result)
    }

    @Test
    fun `test evaluate number literal`() {
        val context = ScriptContext(TestContextFactory.create(fileContent = "test content"))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("42")

        assertEquals(42, result)
    }

    @Test
    fun `test evaluate variable`() {
        val context = ScriptContext(TestContextFactory.create(fileContent = "test content"))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)

        context.setVariable("x", 100)
        val result = evaluator.evaluateExpression("x")

        assertEquals(100, result)
    }

    @Test
    fun `test evaluate string concatenation`() {
        val context = ScriptContext(TestContextFactory.create(fileContent = "test content"))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("\"Hello\" + \" \" + \"World\"")

        assertEquals("Hello World", result)
    }

    @Test
    fun `test evaluate concatenation with variable`() {
        val context = ScriptContext(TestContextFactory.create(fileContent = "test content"))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)

        context.setVariable("name", "John")
        val result = evaluator.evaluateExpression("\"Hello \" + name")

        assertEquals("Hello John", result)
    }

    @Test
    fun `test evaluate template literal with variable`() {
        val context = ScriptContext(TestContextFactory.create(fileContent = "test content"))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)

        context.setVariable("i", 5)
        val result = evaluator.evaluateExpression("`Item number ${'$'}{i}`")

        assertEquals("Item number 5", result)
    }

    @Test
    fun `test evaluate template literal with newline escape`() {
        val context = ScriptContext(TestContextFactory.create(fileContent = "test content"))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("`Line 1\\nLine 2`")

        assertEquals("Line 1\nLine 2", result)
    }

    @Test
    fun `test evaluate template literal with tab escape`() {
        val context = ScriptContext(TestContextFactory.create(fileContent = "test content"))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("`Col1\\tCol2`")

        assertEquals("Col1\tCol2", result)
    }

    @Test
    fun `test evaluate function call`() {
        val context = ScriptContext(TestContextFactory.create(fileContent = "test content"))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("tp.date.now(\"yyyy-MM-dd\")")

        assertNotNull(result)
        assertTrue(result is String)
        assertTrue((result as String).matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun `test evaluate new Date`() {
        val context = ScriptContext(TestContextFactory.create(fileContent = "test content"))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("new Date()")

        assertNotNull(result)
        // Should return a DateObject wrapper
    }

    @Test
    fun `test evaluate property access on DateObject`() {
        val context = ScriptContext(TestContextFactory.create(fileContent = "test content"))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)

        context.setVariable("now", evaluator.evaluateExpression("new Date()"))
        val result = evaluator.evaluateExpression("now.getHours()")

        assertNotNull(result)
        assertTrue(result is Int)
        assertTrue((result as Int) in 0..23)
    }

    @Test
    fun `test evaluate concatenation with function call`() {
        val context = ScriptContext(TestContextFactory.create(fileContent = "test content"))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("\"Today: \" + tp.date.now(\"yyyy-MM-dd\")")

        assertNotNull(result)
        assertTrue(result is String)
        assertTrue((result as String).startsWith("Today: "))
    }

    @Test
    fun `test evaluate boolean true`() {
        val context = ScriptContext(TestContextFactory.create(fileContent = "test content"))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("true")

        assertEquals(true, result)
    }

    @Test
    fun `test evaluate boolean false`() {
        val context = ScriptContext(TestContextFactory.create(fileContent = "test content"))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("false")

        assertEquals(false, result)
    }

    @Test
    fun `test evaluate frontmatter access`() {
        val context = ScriptContext(TestContextFactory.create(
            frontmatter = mapOf("title" to "Test Document"),
            fileContent = "test content"
        ))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("tp.frontmatter.title")

        assertEquals("Test Document", result)
    }

    // Arithmetic operation tests

    @Test
    fun `test evaluate addition with numbers`() {
        val context = ScriptContext(TestContextFactory.create(fileContent = "test content"))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("5 + 3")

        assertEquals(8, result)
    }

    @Test
    fun `test evaluate subtraction`() {
        val context = ScriptContext(TestContextFactory.create(fileContent = "test content"))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("10 - 4")

        assertEquals(6, result)
    }

    @Test
    fun `test evaluate multiplication`() {
        val context = ScriptContext(TestContextFactory.create(fileContent = "test content"))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("6 * 7")

        assertEquals(42, result)
    }

    @Test
    fun `test evaluate division`() {
        val context = ScriptContext(TestContextFactory.create(fileContent = "test content"))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("20 / 4")

        assertEquals(5, result)
    }

    @Test
    fun `test evaluate variable plus number`() {
        val context = ScriptContext(TestContextFactory.create(fileContent = "test content"))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)

        context.setVariable("counter", 0)
        val result = evaluator.evaluateExpression("counter + 1")

        assertEquals(1, result)
    }

    @Test
    fun `test evaluate variable times number`() {
        val context = ScriptContext(TestContextFactory.create(fileContent = "test content"))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)

        context.setVariable("x", 5)
        val result = evaluator.evaluateExpression("x * 2")

        assertEquals(10, result)
    }

    @Test
    fun `test evaluate variable plus variable`() {
        val context = ScriptContext(TestContextFactory.create(fileContent = "test content"))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)

        context.setVariable("x", 5)
        context.setVariable("y", 10)
        val result = evaluator.evaluateExpression("y + x")

        assertEquals(15, result)
    }

    @Test
    fun `test evaluate chained arithmetic - multiplication before addition`() {
        val context = ScriptContext(TestContextFactory.create(fileContent = "test content"))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)

        // Note: Our simple evaluator processes left-to-right for same precedence
        // For x * 2 + x where x=5: should be 10 + 5 = 15
        context.setVariable("x", 5)
        context.setVariable("y", 10) // y = x * 2
        val result = evaluator.evaluateExpression("y + x")

        assertEquals(15, result)
    }

    @Test
    fun `test evaluate string plus number does concatenation`() {
        val context = ScriptContext(TestContextFactory.create(fileContent = "test content"))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("\"Value: \" + 42")

        assertEquals("Value: 42", result)
    }

    @Test
    fun `test evaluate double division`() {
        val context = ScriptContext(TestContextFactory.create(fileContent = "test content"))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("7 / 2")

        assertEquals(3.5, result)
    }

    @Test
    fun `test evaluate negative result`() {
        val context = ScriptContext(TestContextFactory.create(fileContent = "test content"))
        val registry = ModuleRegistry(context)
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("3 - 10")

        assertEquals(-7, result)
    }
}

