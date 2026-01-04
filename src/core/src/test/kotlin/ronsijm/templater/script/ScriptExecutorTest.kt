package ronsijm.templater.script

import ronsijm.templater.TestContextFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ScriptExecutorTest {

    @Test
    fun `test execute variable declaration`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)
        val parser = ScriptParser()
        val executor = ScriptExecutor(context, evaluator, parser)

        executor.executeStatement("let x = 42")

        assertEquals(42, context.getVariable("x"))
    }

    @Test
    fun `test execute const declaration`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)
        val parser = ScriptParser()
        val executor = ScriptExecutor(context, evaluator, parser)

        executor.executeStatement("const y = \"hello\"")

        assertEquals("hello", context.getVariable("y"))
    }

    @Test
    fun `test execute var declaration`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)
        val parser = ScriptParser()
        val executor = ScriptExecutor(context, evaluator, parser)

        executor.executeStatement("var z = true")

        assertEquals(true, context.getVariable("z"))
    }

    @Test
    fun `test execute assignment`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)
        val parser = ScriptParser()
        val executor = ScriptExecutor(context, evaluator, parser)

        context.setVariable("x", 10)
        executor.executeStatement("x = 20")

        assertEquals(20, context.getVariable("x"))
    }

    @Test
    fun `test execute tR append`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)
        val parser = ScriptParser()
        val executor = ScriptExecutor(context, evaluator, parser)

        context.initializeResultAccumulator("Start")
        executor.executeStatement("tR += \" End\"")

        assertEquals("Start End", context.getResultAccumulator())
    }

    @Test
    fun `test execute tR set`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)
        val parser = ScriptParser()
        val executor = ScriptExecutor(context, evaluator, parser)

        context.initializeResultAccumulator("Initial")
        executor.executeStatement("tR = \"Replaced\"")

        assertEquals("Replaced", context.getResultAccumulator())
    }

    @Test
    fun `test execute for loop`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)
        val parser = ScriptParser()
        val executor = ScriptExecutor(context, evaluator, parser)

        context.initializeResultAccumulator("")
        val loopBody = listOf("tR += \"x\"")

        executor.executeForLoop("for (let i = 1; i <= 3; i++)", loopBody)

        assertEquals("xxx", context.getResultAccumulator())
    }

    @Test
    fun `test execute for loop with decrement`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)
        val parser = ScriptParser()
        val executor = ScriptExecutor(context, evaluator, parser)

        context.initializeResultAccumulator("")
        val loopBody = listOf("tR += \"x\"")

        executor.executeForLoop("for (let i = 3; i >= 1; i--)", loopBody)

        assertEquals("xxx", context.getResultAccumulator())
    }

    @Test
    fun `test execute if statement true condition`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)
        val parser = ScriptParser()
        val executor = ScriptExecutor(context, evaluator, parser)

        context.initializeResultAccumulator("")
        context.setVariable("x", 10)

        executor.executeIfStatement("if (x > 5)", listOf("tR += \"yes\""), emptyList())

        assertEquals("yes", context.getResultAccumulator())
    }

    @Test
    fun `test execute if statement false condition`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)
        val parser = ScriptParser()
        val executor = ScriptExecutor(context, evaluator, parser)

        context.initializeResultAccumulator("")
        context.setVariable("x", 3)

        executor.executeIfStatement("if (x > 5)", listOf("tR += \"yes\""), emptyList())

        assertEquals("", context.getResultAccumulator())
    }

    @Test
    fun `test execute if else statement`() {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        val evaluator = ScriptEvaluator(context, registry)
        val parser = ScriptParser()
        val executor = ScriptExecutor(context, evaluator, parser)

        context.initializeResultAccumulator("")
        context.setVariable("x", 3)

        val elseBranches = listOf(Pair(null, listOf("tR += \"no\"")))
        executor.executeIfStatement("if (x > 5)", listOf("tR += \"yes\""), elseBranches)

        assertEquals("no", context.getResultAccumulator())
    }
}
