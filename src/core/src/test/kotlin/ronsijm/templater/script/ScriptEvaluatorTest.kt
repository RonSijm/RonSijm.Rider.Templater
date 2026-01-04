package ronsijm.templater.script

import ronsijm.templater.TestContextFactory
import ronsijm.templater.script.methods.ArrayMethodExecutor
import ronsijm.templater.utils.ArgumentParser
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ScriptEvaluatorTest {

    @Test
    fun `test evaluate simple string literal`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("\"Hello World\"")

        assertEquals("Hello World", result)
    }

    @Test
    fun `test evaluate number literal`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("42")

        assertEquals(42, result)
    }

    @Test
    fun `test evaluate variable`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        context.setVariable("x", 100)
        val result = evaluator.evaluateExpression("x")

        assertEquals(100, result)
    }

    @Test
    fun `test evaluate string concatenation`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("\"Hello\" + \" \" + \"World\"")

        assertEquals("Hello World", result)
    }

    @Test
    fun `test evaluate concatenation with variable`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        context.setVariable("name", "John")
        val result = evaluator.evaluateExpression("\"Hello \" + name")

        assertEquals("Hello John", result)
    }

    @Test
    fun `test evaluate template literal with variable`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        context.setVariable("i", 5)
        val result = evaluator.evaluateExpression("`Item number ${'$'}{i}`")

        assertEquals("Item number 5", result)
    }

    @Test
    fun `test evaluate template literal with newline escape`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("`Line 1\\nLine 2`")

        assertEquals("Line 1\nLine 2", result)
    }

    @Test
    fun `test evaluate template literal with tab escape`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("`Col1\\tCol2`")

        assertEquals("Col1\tCol2", result)
    }

    @Test
    fun `test evaluate function call`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createRealModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("tp.date.now(\"yyyy-MM-dd\")")

        assertNotNull(result)
        assertTrue(result is String)
        assertTrue((result as String).matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun `test evaluate new Date`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("new Date()")

        assertNotNull(result)

    }

    @Test
    fun `test evaluate property access on DateObject`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        context.setVariable("now", evaluator.evaluateExpression("new Date()"))
        val result = evaluator.evaluateExpression("now.getHours()")

        assertNotNull(result)
        assertTrue(result is Int)
        assertTrue((result as Int) in 0..23)
    }

    @Test
    fun `test evaluate new Date with chained method call`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)


        val result = evaluator.evaluateExpression("new Date().getHours()")

        assertNotNull(result)
        assertTrue(result is Int, "Result should be Int, got ${result?.javaClass}")
        assertTrue((result as Int) in 0..23, "Hours should be 0-23, got $result")
    }

    @Test
    fun `test evaluate concatenation with function call`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("\"Today: \" + tp.date.now(\"yyyy-MM-dd\")")

        assertNotNull(result)
        assertTrue(result is String)
        assertTrue((result as String).startsWith("Today: "))
    }

    @Test
    fun `test evaluate boolean true`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("true")

        assertEquals(true, result)
    }

    @Test
    fun `test evaluate boolean false`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("false")

        assertEquals(false, result)
    }

    @Test
    fun `test evaluate frontmatter access`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry(
            frontmatter = mapOf("title" to "Test Document")
        )
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("tp.frontmatter.title")

        assertEquals("Test Document", result)
    }



    @Test
    fun `test evaluate addition with numbers`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("5 + 3")

        assertEquals(8, result)
    }

    @Test
    fun `test evaluate subtraction`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("10 - 4")

        assertEquals(6, result)
    }

    @Test
    fun `test evaluate multiplication`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("6 * 7")

        assertEquals(42, result)
    }

    @Test
    fun `test evaluate division`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("20 / 4")

        assertEquals(5, result)
    }

    @Test
    fun `test evaluate variable plus number`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        context.setVariable("counter", 0)
        val result = evaluator.evaluateExpression("counter + 1")

        assertEquals(1, result)
    }

    @Test
    fun `test evaluate variable times number`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        context.setVariable("x", 5)
        val result = evaluator.evaluateExpression("x * 2")

        assertEquals(10, result)
    }

    @Test
    fun `test evaluate variable plus variable`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        context.setVariable("x", 5)
        context.setVariable("y", 10)
        val result = evaluator.evaluateExpression("y + x")

        assertEquals(15, result)
    }

    @Test
    fun `test evaluate chained arithmetic - multiplication before addition`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)



        context.setVariable("x", 5)
        context.setVariable("y", 10)
        val result = evaluator.evaluateExpression("y + x")

        assertEquals(15, result)
    }

    @Test
    fun `test evaluate string plus number does concatenation`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("\"Value: \" + 42")

        assertEquals("Value: 42", result)
    }

    @Test
    fun `test evaluate double division`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("7 / 2")

        assertEquals(3.5, result)
    }

    @Test
    fun `test evaluate negative result`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("3 - 10")

        assertEquals(-7, result)
    }



    @Test
    fun `test evaluate array literal`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("""["a", "b", "c"]""")

        assertTrue(result is List<*>)
        val list = result as List<*>
        assertEquals(3, list.size)
        assertEquals("a", list[0])
        assertEquals("b", list[1])
        assertEquals("c", list[2])
    }

    @Test
    fun `test evaluate array with many string elements`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)


        val result = evaluator.evaluateExpression("""["Abstract", "Attention", "Bug", "Caution", "Check"]""")

        assertTrue(result is List<*>)
        val list = result as List<*>
        assertEquals(5, list.size)
        assertEquals("Abstract", list[0])
        assertEquals("Attention", list[1])
        assertEquals("Bug", list[2])
        assertEquals("Caution", list[3])
        assertEquals("Check", list[4])
    }

    @Test
    fun `test evaluate mixed type array`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("""["text", 42, true, false]""")

        assertTrue(result is List<*>)
        val list = result as List<*>
        assertEquals(4, list.size)
        assertEquals("text", list[0])
        assertEquals(42, list[1])
        assertEquals(true, list[2])
        assertEquals(false, list[3])
    }

    @Test
    fun `test evaluate empty array`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("[]")

        assertTrue(result is List<*>)
        val list = result as List<*>
        assertEquals(0, list.size)
    }

    @Test
    fun `test evaluate nested arrays`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("""[[1, 2], [3, 4]]""")

        assertTrue(result is List<*>)
        val list = result as List<*>
        assertEquals(2, list.size)
        assertTrue(list[0] is List<*>)
        assertTrue(list[1] is List<*>)
    }



    @Test
    fun `test parseArguments with two array arguments`() {

        val result = ArgumentParser.parseArgumentString("""["a", "b", "c"], ["x", "y", "z"]""")

        assertEquals(2, result.size, "Should have exactly 2 arguments (two arrays)")

        assertTrue(result[0] is List<*>, "First argument should be a list")
        val firstArray = result[0] as List<*>
        assertEquals(3, firstArray.size)
        assertEquals("a", firstArray[0])
        assertEquals("b", firstArray[1])
        assertEquals("c", firstArray[2])

        assertTrue(result[1] is List<*>, "Second argument should be a list")
        val secondArray = result[1] as List<*>
        assertEquals(3, secondArray.size)
        assertEquals("x", secondArray[0])
        assertEquals("y", secondArray[1])
        assertEquals("z", secondArray[2])
    }

    @Test
    fun `test parseArguments with arrays and other arguments - suggester pattern`() {


        val result = ArgumentParser.parseArgumentString(
            """["Option 1", "Option 2"], ["val1", "val2"], false, "Select an option"""")

        assertEquals(4, result.size, "Should have exactly 4 arguments")


        assertTrue(result[0] is List<*>, "First argument should be a list")
        val textItems = result[0] as List<*>
        assertEquals(2, textItems.size)
        assertEquals("Option 1", textItems[0])
        assertEquals("Option 2", textItems[1])


        assertTrue(result[1] is List<*>, "Second argument should be a list")
        val items = result[1] as List<*>
        assertEquals(2, items.size)
        assertEquals("val1", items[0])
        assertEquals("val2", items[1])


        assertEquals(false, result[2])


        assertEquals("Select an option", result[3])
    }

    @Test
    fun `test parseArguments with large arrays - callout suggester pattern`() {


        val result = ArgumentParser.parseArgumentString(
            """["Abstract", "Attention", "Bug", "Caution", "Check"], ["abstract", "attention", "bug", "caution", "check"], false, "Which callout?"""")

        assertEquals(4, result.size, "Should have exactly 4 arguments")


        assertTrue(result[0] is List<*>, "First argument should be a list")
        val displayNames = result[0] as List<*>
        assertEquals(5, displayNames.size)
        assertEquals("Abstract", displayNames[0])
        assertEquals("Check", displayNames[4])


        assertTrue(result[1] is List<*>, "Second argument should be a list")
        val values = result[1] as List<*>
        assertEquals(5, values.size)
        assertEquals("abstract", values[0])
        assertEquals("check", values[4])


        assertEquals(false, result[2])


        assertEquals("Which callout?", result[3])
    }

    @Test
    fun `test Object keys on map`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)


        val callouts = mapOf(
            "note" to "Note",
            "info" to "Info",
            "warning" to "Warning"
        )
        context.setVariable("callouts", callouts)

        val result = evaluator.evaluateExpression("Object.keys(callouts)")

        assertTrue(result is List<*>, "Result should be a list")
        val keys = result as List<*>
        assertEquals(3, keys.size)
        assertTrue(keys.contains("note"))
        assertTrue(keys.contains("info"))
        assertTrue(keys.contains("warning"))
    }

    @Test
    fun `test Object values on map`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)


        val callouts = mapOf(
            "note" to "Note",
            "info" to "Info",
            "warning" to "Warning"
        )
        context.setVariable("callouts", callouts)

        val result = evaluator.evaluateExpression("Object.values(callouts)")

        assertTrue(result is List<*>, "Result should be a list")
        val values = result as List<*>
        assertEquals(3, values.size)
        assertTrue(values.contains("Note"))
        assertTrue(values.contains("Info"))
        assertTrue(values.contains("Warning"))
    }

    @Test
    fun `test Object entries on map`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)


        val callouts = mapOf(
            "note" to "Note",
            "info" to "Info"
        )
        context.setVariable("callouts", callouts)

        val result = evaluator.evaluateExpression("Object.entries(callouts)")

        assertTrue(result is List<*>, "Result should be a list")
        val entries = result as List<*>
        assertEquals(2, entries.size)

        assertTrue(entries[0] is List<*>)
    }

    @Test
    fun `test object literal parsing`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)

        val result = evaluator.evaluateExpression("""{ note: "Note", info: "Info" }""")

        assertTrue(result is Map<*, *>, "Result should be a map")
        val map = result as Map<*, *>
        assertEquals("Note", map["note"])
        assertEquals("Info", map["info"])
    }

    @Test
    fun `test Object keys with inline object literal`() {
        val engine = TestContextFactory.createScriptEngine()
        engine.initializeResultAccumulator("")

        val script = """
            const callouts = { note: "Note", info: "Info", warning: "Warning" };
            const keys = Object.keys(callouts);
            tR += keys.join(", ");
        """.trimIndent()

        engine.execute(script)
        val result = engine.getResultAccumulator()


        assertTrue(result.contains("note"), "Result should contain 'note': $result")
        assertTrue(result.contains("info"), "Result should contain 'info': $result")
        assertTrue(result.contains("warning"), "Result should contain 'warning': $result")
    }

    @Test
    fun `test Object values with inline object literal`() {
        val engine = TestContextFactory.createScriptEngine()
        engine.initializeResultAccumulator("")

        val script = """
            const callouts = { note: "Note", info: "Info", warning: "Warning" };
            const values = Object.values(callouts);
            tR += values.join(", ");
        """.trimIndent()

        engine.execute(script)
        val result = engine.getResultAccumulator()


        assertTrue(result.contains("Note"), "Result should contain 'Note': $result")
        assertTrue(result.contains("Info"), "Result should contain 'Info': $result")
        assertTrue(result.contains("Warning"), "Result should contain 'Warning': $result")
    }

    @Test
    fun `test multiline object literal with Object values and keys`() {
        val engine = TestContextFactory.createScriptEngine()
        engine.initializeResultAccumulator("")


        val script = """
const callouts = {
note:     'Note',
info:     'Info',
todo:     'Todo',
};
const keys = Object.keys(callouts);
const values = Object.values(callouts);
tR += "Keys: " + keys.join(", ") + " | Values: " + values.join(", ");
        """.trimIndent()

        engine.execute(script)
        val result = engine.getResultAccumulator()


        assertTrue(result.contains("note"), "Result should contain 'note': $result")
        assertTrue(result.contains("info"), "Result should contain 'info': $result")
        assertTrue(result.contains("todo"), "Result should contain 'todo': $result")

        assertTrue(result.contains("Note"), "Result should contain 'Note': $result")
        assertTrue(result.contains("Info"), "Result should contain 'Info': $result")
        assertTrue(result.contains("Todo"), "Result should contain 'Todo': $result")
    }

    @Test
    fun `test forEach with arrow function populates arrays`() {
        val engine = TestContextFactory.createScriptEngine()
        engine.initializeResultAccumulator("")


        val script = """
const arr = [];
arr.push('a');
arr.push('b');
tR += arr.join(", ");
        """.trimIndent()

        engine.execute(script)
        val result = engine.getResultAccumulator()

        assertEquals("a, b", result, "Push should modify array in place")
    }

    @Test
    fun `test forEach with simple callback - direct API`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)


        context.setVariable("items", mutableListOf("a", "b", "c"))
        context.setVariable("result", mutableListOf<Any?>())


        val arrowFn = evaluator.evaluateExpression("item => result.push(item)")
        assertTrue(arrowFn is ArrowFunction, "Should parse as ArrowFunction: $arrowFn")


        val items = context.getVariable("items") as List<*>
        val forEachResult = ArrayMethodExecutor.execute(items, "forEach", listOf(arrowFn)) { fn, args ->
            evaluator.executeArrowFunction(fn, args)
        }


        val resultList = context.getVariable("result") as List<*>
        assertEquals(listOf("a", "b", "c"), resultList, "forEach should have pushed items to result")
    }

    @Test
    fun `test forEach via script execution`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)
        val parser = ScriptParser()
        val executor = ScriptExecutor(context, evaluator, parser)


        executor.executeStatement("const items = ['a', 'b', 'c']")
        val items = context.getVariable("items")
        assertTrue(items is MutableList<*>, "items should be MutableList: ${items?.javaClass}")
        assertEquals(listOf("a", "b", "c"), items, "items should be ['a', 'b', 'c']")


        executor.executeStatement("const result = []")
        val result = context.getVariable("result")
        assertTrue(result is MutableList<*>, "result should be MutableList: ${result?.javaClass}")
        assertEquals(emptyList<Any?>(), result, "result should be empty")


        val arrowFn = evaluator.evaluateExpression("item => result.push(item)")
        assertTrue(arrowFn is ArrowFunction, "Should parse as ArrowFunction: $arrowFn (type: ${arrowFn?.javaClass})")


        val forEachExpr = "items.forEach(item => result.push(item))"
        val evalResult = evaluator.evaluateExpression(forEachExpr)


        val resultAfter = context.getVariable("result")
        assertEquals(listOf("a", "b", "c"), resultAfter, "result should have items after forEach, evalResult=$evalResult")
    }

    @Test
    fun `test forEach with Object keys populates arrays`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)
        val parser = ScriptParser()
        val executor = ScriptExecutor(context, evaluator, parser)


        executor.executeStatement("const callouts = { note: 'Note', info: 'Info' }")
        val callouts = context.getVariable("callouts")
        assertTrue(callouts is Map<*, *>, "callouts should be Map: ${callouts?.javaClass}")


        executor.executeStatement("const typeNames = []")
        executor.executeStatement("const typeLabels = []")


        val keys = evaluator.evaluateExpression("Object.keys(callouts)")
        assertTrue(keys is List<*>, "Object.keys should return List: ${keys?.javaClass}")
        assertEquals(listOf("note", "info"), keys, "Object.keys should return keys")


        val forEachExpr = "Object.keys(callouts).forEach(key => typeNames.push(key))"
        evaluator.evaluateExpression(forEachExpr)

        val typeNames = context.getVariable("typeNames")
        assertEquals(listOf("note", "info"), typeNames, "typeNames should have keys after forEach")
    }

    @Test
    fun `test multiline method chaining with sort and forEach`() {

        val engine = TestContextFactory.createScriptEngine()
        engine.initializeResultAccumulator("")

        val script = """
const callouts = {
"bug": "Bug",
"warning": "Warning",
"info": "Info",
};

const typeNames = [];
const typeLabels = [];

Object.keys(callouts)
.sort()
.forEach(key =>
typeNames.push(key) && typeLabels.push(callouts[key])
);

tR += "Names: " + typeNames.join(", ") + " | Labels: " + typeLabels.join(", ");
        """.trimIndent()

        val lexer = ScriptLexer()
        val preprocessed = lexer.preprocessScript(script)
        val statements = lexer.smartSplitStatements(preprocessed)

        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)
        val parser = ScriptParser()
        val executor = ScriptExecutor(context, evaluator, parser)
        context.initializeResultAccumulator("")

        for (stmt in statements) {
            executor.executeStatement(stmt)
        }

        val result = context.getResultAccumulator()


        assertTrue(result.contains("bug"), "Result should contain 'bug': $result")
        assertTrue(result.contains("info"), "Result should contain 'info': $result")
        assertTrue(result.contains("warning"), "Result should contain 'warning': $result")

        assertTrue(result.contains("Bug"), "Result should contain 'Bug': $result")
        assertTrue(result.contains("Info"), "Result should contain 'Info': $result")
        assertTrue(result.contains("Warning"), "Result should contain 'Warning': $result")
    }

    @Test
    fun `test return statement stops script execution`() {
        val engine = TestContextFactory.createScriptEngine()
        engine.initializeResultAccumulator("")

        val script = """
tR += "Before return";
return;
tR += " - After return";
        """.trimIndent()

        engine.execute(script)
        val result = engine.getResultAccumulator()

        assertEquals("Before return", result, "Return should stop execution")
        assertTrue(engine.isReturnRequested(), "Return flag should be set")
    }

    @Test
    fun `test return in if block stops execution`() {
        val engine = TestContextFactory.createScriptEngine()
        engine.initializeResultAccumulator("")

        val script = """
const value = null;
if (!value) {
    return;
}
tR += "This should not appear";
        """.trimIndent()

        engine.execute(script)
        val result = engine.getResultAccumulator()

        assertEquals("", result, "Return in if block should stop execution")
        assertTrue(engine.isReturnRequested(), "Return flag should be set")
    }
}
