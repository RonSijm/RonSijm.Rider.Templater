package ronsijm.templater.script

import ronsijm.templater.TestContextFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*


class ScriptEngineFunctionTest {

    @Test
    fun `test function declaration and call`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            function add(a, b) {
                return a + b;
            }
        """.trimIndent())

        val fn = engine.getVariable("add")
        assertNotNull(fn, "Function 'add' should be stored")
        assertTrue(fn is UserFunction, "Function should be a UserFunction")
    }

    @Test
    fun `test arrow function parsing`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("const double = x => x * 2")

        val fn = engine.getVariable("double")
        assertNotNull(fn, "Arrow function should be stored")
        assertTrue(fn is ArrowFunction, "Should be ArrowFunction but was ${fn?.javaClass}")
    }

    @Test
    fun `test arrow function execution`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            const double = x => x * 2
            const result = double(5)
            tR += result
        """.trimIndent())

        val result = engine.getResultAccumulator()
        assertEquals("10", result, "double(5) should be 10")
    }

    @Test
    fun `test arrow function with filter`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("const nums = [1, 2, 3, 4, 5]")
        engine.execute("const evens = nums.filter(x => x > 2)")
        engine.execute("tR += evens.join(\", \")")

        val result = engine.getResultAccumulator()
        assertTrue(result.contains("3"), "Should contain 3: $result")
        assertTrue(result.contains("4"), "Should contain 4: $result")
        assertTrue(result.contains("5"), "Should contain 5: $result")
    }

    @Test
    fun `test arrow function parsing directly`() {
        val scriptContext = ScriptContext()
        val moduleRegistry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(scriptContext, moduleRegistry)

        val arrowFn = evaluator.evaluateExpression("x => x > 2")
        assertTrue(arrowFn is ArrowFunction, "Should parse as ArrowFunction: $arrowFn (${arrowFn?.javaClass})")

        if (arrowFn is ArrowFunction) {
            val result = evaluator.executeArrowFunction(arrowFn, listOf(5))
            assertEquals(true, result, "5 > 2 should be true: $result")

            val result2 = evaluator.executeArrowFunction(arrowFn, listOf(1))
            assertEquals(false, result2, "1 > 2 should be false: $result2")
        }
    }

    @Test
    fun `test function with for loop body parsing`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            function countSimple() {
                let sum = 0
                for (let i = 0; i < 3; i++) {
                    sum += 1
                }
                return sum
            }
        """.trimIndent())

        val fn = engine.getVariable("countSimple")
        assertNotNull(fn, "countSimple function should be stored")
        assertTrue(fn is UserFunction, "countSimple should be a UserFunction")

        val userFn = fn as UserFunction
        assertTrue(userFn.body.contains("let sum = 0"), "Body should contain variable declaration")
        assertTrue(userFn.body.contains("for ("), "Body should contain for loop")
        assertTrue(userFn.body.contains("return sum"), "Body should contain return statement")
    }

    @Test
    fun `test function with for loop execution`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            function countSimple() {
                let sum = 0
                for (let i = 0; i < 3; i++) {
                    sum += 1
                }
                return sum
            }
            let result = countSimple()
        """.trimIndent())

        val result = engine.getVariable("result")
        assertNotNull(result, "countSimple should return a value")
        assertEquals(3, (result as Number).toInt(), "countSimple should return 3")
    }

    @Test
    fun `test nested for loop in function`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            function nestedLoop() {
                let sum = 0
                for (let i = 0; i < 3; i++) {
                    for (let j = 0; j < 2; j++) {
                        sum += 1
                    }
                }
                return sum
            }
            let result = nestedLoop()
        """.trimIndent())

        val result = engine.getVariable("result")
        assertNotNull(result, "result should have a value")
        assertEquals(6, (result as Number).toInt(), "result should be 6 (3 * 2)")
    }

    @Test
    fun `test pi spigot simplified`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            function simpleAdd(a, b) {
                return a + b;
            }
            let addResult = simpleAdd(3, 4)
        """.trimIndent())

        val addResult = engine.getVariable("addResult")
        assertEquals(7, (addResult as Number).toInt(), "simpleAdd should return 7")
    }
}

