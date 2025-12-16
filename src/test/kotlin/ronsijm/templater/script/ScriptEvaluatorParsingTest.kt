package ronsijm.templater.script

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ronsijm.templater.TestContextFactory
import ronsijm.templater.parser.TemplateContext

/**
 * Tests for low-level parsing functions in ScriptEvaluator.
 * These tests cover array access, method chaining, bracket matching, and related parsing logic.
 */
class ScriptEvaluatorParsingTest {

    private lateinit var context: TemplateContext
    private lateinit var engine: ScriptEngine

    @BeforeEach
    fun setup() {
        context = TestContextFactory.create(fileContent = "test content")
        engine = ScriptEngine(context)
    }
    
    // ==================== Array Index Access Tests ====================
    
    @Test
    fun `test simple array index access`() {
        engine.execute("const arr = [10, 20, 30]")
        val result = engine.evaluateExpression("arr[0]")
        assertEquals(10, result)
    }
    
    @Test
    fun `test array index access with variable index`() {
        engine.execute("const arr = ['a', 'b', 'c']")
        engine.execute("const idx = 1")
        val result = engine.evaluateExpression("arr[idx]")
        assertEquals("b", result)
    }
    
    @Test
    fun `test array index access last element`() {
        engine.execute("const arr = [1, 2, 3, 4, 5]")
        val result = engine.evaluateExpression("arr[4]")
        assertEquals(5, result)
    }
    
    @Test
    fun `test array index access out of bounds returns null`() {
        engine.execute("const arr = [1, 2, 3]")
        val result = engine.evaluateExpression("arr[10]")
        assertNull(result)
    }
    
    @Test
    fun `test negative array index returns null`() {
        engine.execute("const arr = [1, 2, 3]")
        val result = engine.evaluateExpression("arr[-1]")
        assertNull(result)
    }
    
    // ==================== Map/Object Access Tests ====================
    
    @Test
    fun `test map access with string key`() {
        engine.execute("""const obj = {"name": "John", "age": "30"}""")
        engine.execute("const key = \"name\"")
        val result = engine.evaluateExpression("obj[key]")
        assertEquals("John", result)
    }
    
    @Test
    fun `test map access with literal string key`() {
        engine.execute("""const obj = {"foo": "bar"}""")
        val result = engine.evaluateExpression("""obj["foo"]""")
        assertEquals("bar", result)
    }
    
    @Test
    fun `test map access with non-existent key returns null`() {
        engine.execute("""const obj = {"a": "1"}""")
        val result = engine.evaluateExpression("""obj["nonexistent"]""")
        assertNull(result)
    }
    
    // ==================== Method Chain After Array Access Tests ====================
    
    @Test
    fun `test array access followed by method call`() {
        engine.execute("""const arr = ["hello#world", "foo#bar"]""")
        val result = engine.evaluateExpression("arr[0].split('#')[0]")
        assertEquals("hello", result)
    }
    
    @Test
    fun `test map access followed by method call`() {
        engine.execute("""const obj = {"key": "hello#world"}""")
        engine.execute("const k = \"key\"")
        val result = engine.evaluateExpression("obj[k].split('#')[0]")
        assertEquals("hello", result)
    }
    
    @Test
    fun `test map access followed by multiple method calls`() {
        engine.execute("""const obj = {"key": "  hello#world  "}""")
        engine.execute("const k = \"key\"")
        val result = engine.evaluateExpression("obj[k].trim().split('#')[0]")
        assertEquals("hello", result)
    }
    
    @Test
    fun `test array access followed by trim`() {
        engine.execute("""const arr = ["  hello  ", "  world  "]""")
        val result = engine.evaluateExpression("arr[0].trim()")
        assertEquals("hello", result)
    }
    
    @Test
    fun `test chained array access`() {
        engine.execute("""const arr = [["a", "b"], ["c", "d"]]""")
        val result = engine.evaluateExpression("arr[0][1]")
        assertEquals("b", result)
    }
    
    // ==================== String Index Access Tests ====================
    
    @Test
    fun `test string character access`() {
        engine.execute("const str = \"hello\"")
        val result = engine.evaluateExpression("str[0]")
        assertEquals("h", result)
    }
    
    @Test
    fun `test string character access middle`() {
        engine.execute("const str = \"hello\"")
        val result = engine.evaluateExpression("str[2]")
        assertEquals("l", result)
    }
    
    @Test
    fun `test string character access out of bounds returns null`() {
        engine.execute("const str = \"hi\"")
        val result = engine.evaluateExpression("str[10]")
        assertNull(result)
    }
    
    // ==================== Method Call Followed by Array Access Tests ====================
    
    @Test
    fun `test split followed by array access`() {
        engine.execute("const str = \"a,b,c\"")
        val result = engine.evaluateExpression("str.split(',')[1]")
        assertEquals("b", result)
    }
    
    @Test
    fun `test split followed by array access first element`() {
        engine.execute("const str = \"hello#world\"")
        val result = engine.evaluateExpression("str.split('#')[0]")
        assertEquals("hello", result)
    }
    
    @Test
    fun `test split followed by array access last element`() {
        engine.execute("const str = \"a-b-c-d\"")
        val result = engine.evaluateExpression("str.split('-')[3]")
        assertEquals("d", result)
    }

    // ==================== Complex Chain Tests ====================

    @Test
    fun `test method chain with multiple array accesses`() {
        engine.execute("const str = \"a,b#c,d\"")
        val result = engine.evaluateExpression("str.split(',')[1].split('#')[0]")
        assertEquals("b", result)
    }

    @Test
    fun `test Object keys followed by array access`() {
        engine.execute("""const obj = {"x": 1, "y": 2, "z": 3}""")
        engine.execute("const keys = Object.keys(obj)")
        val result = engine.evaluateExpression("keys[0]")
        assertEquals("x", result)
    }

    @Test
    fun `test Object values followed by array access`() {
        engine.execute("""const obj = {"a": "first", "b": "second"}""")
        engine.execute("const vals = Object.values(obj)")
        val result = engine.evaluateExpression("vals[1]")
        assertEquals("second", result)
    }

    // ==================== Logical NOT Operator Tests ====================

    @Test
    fun `test NOT operator with truthy value`() {
        engine.execute("const x = \"hello\"")
        val result = engine.evaluateExpression("!x")
        assertEquals(false, result)
    }

    @Test
    fun `test NOT operator with falsy null value`() {
        engine.execute("const x = null")
        val result = engine.evaluateExpression("!x")
        assertEquals(true, result)
    }

    @Test
    fun `test NOT operator with empty string`() {
        engine.execute("const x = \"\"")
        val result = engine.evaluateExpression("!x")
        assertEquals(true, result)
    }

    @Test
    fun `test NOT operator with zero`() {
        engine.execute("const x = 0")
        val result = engine.evaluateExpression("!x")
        assertEquals(true, result)
    }

    @Test
    fun `test NOT operator with non-zero number`() {
        engine.execute("const x = 42")
        val result = engine.evaluateExpression("!x")
        assertEquals(false, result)
    }

    @Test
    fun `test NOT operator with boolean true`() {
        engine.execute("const x = true")
        val result = engine.evaluateExpression("!x")
        assertEquals(false, result)
    }

    @Test
    fun `test NOT operator with boolean false`() {
        engine.execute("const x = false")
        val result = engine.evaluateExpression("!x")
        assertEquals(true, result)
    }

    @Test
    fun `test NOT operator does not match not-equals`() {
        // Make sure !x doesn't get confused with != operator
        engine.execute("const x = 5")
        engine.execute("const y = 3")
        val result = engine.evaluateExpression("!x")
        assertEquals(false, result) // 5 is truthy, so !5 is false
    }

    // ==================== Template Literal Tests ====================

    @Test
    fun `test template literal with simple variable`() {
        engine.execute("const name = \"World\"")
        val result = engine.evaluateExpression("`Hello \${name}!`")
        assertEquals("Hello World!", result)
    }

    @Test
    fun `test template literal with expression`() {
        engine.execute("const x = 5")
        val result = engine.evaluateExpression("`Result: \${x + 1}`")
        assertEquals("Result: 6", result)
    }

    @Test
    fun `test template literal with multiple variables`() {
        engine.execute("const a = \"foo\"")
        engine.execute("const b = \"bar\"")
        val result = engine.evaluateExpression("`\${a}-\${b}`")
        assertEquals("foo-bar", result)
    }

    @Test
    fun `test template literal with newline escape`() {
        val result = engine.evaluateExpression("""`line1\nline2`""")
        assertEquals("line1\nline2", result)
    }

    @Test
    fun `test template literal with tab escape`() {
        val result = engine.evaluateExpression("""`col1\tcol2`""")
        assertEquals("col1\tcol2", result)
    }
}

