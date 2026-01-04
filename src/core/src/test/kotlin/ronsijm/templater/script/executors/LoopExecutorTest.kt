package ronsijm.templater.script.executors

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ronsijm.templater.TestContextFactory
import ronsijm.templater.script.ScriptContext
import ronsijm.templater.utils.CancellationChecker

class LoopExecutorTest {

    private var cacheCleared = 0
    private var cancellationChecks = 0

    private fun createContext(): ScriptContext {
        return ScriptContext()
    }

    private fun createExecutor(scriptContext: ScriptContext): LoopExecutor {
        cacheCleared = 0
        cancellationChecks = 0

        return LoopExecutor(
            scriptContext = scriptContext,
            cancellationChecker = object : CancellationChecker {
                override fun checkCancelled() {
                    cancellationChecks++
                }
                override fun isCancelled(): Boolean = false
            },
            clearCacheCallback = { cacheCleared++ }
        )
    }

    @Test
    fun `test small loop skips cancellation checks`() {
        val context = createContext()
        val executor = createExecutor(context)

        var iterations = 0
        executor.executeLoop(
            loopType = "for",
            iterationCount = 50,
            hasNext = { iterations < 50 },
            setupIteration = { iterations++ },
            executeBody = {}
        )

        assertEquals(50, iterations)
        assertEquals(50, cacheCleared)
        assertEquals(0, cancellationChecks, "Small loops should not check cancellation")
    }

    @Test
    fun `test large loop checks cancellation every 100 iterations`() {
        val context = createContext()
        val executor = createExecutor(context)

        var iterations = 0
        executor.executeLoop(
            loopType = "for",
            iterationCount = 500,
            hasNext = { iterations < 500 },
            setupIteration = { iterations++ },
            executeBody = {}
        )

        assertEquals(500, iterations)
        assertEquals(500, cacheCleared)

        assertEquals(4, cancellationChecks, "Should check cancellation every 100 iterations")
    }

    @Test
    fun `test executeCountedLoop with increment`() {
        val context = createContext()
        val executor = createExecutor(context)

        val values = mutableListOf<Int>()
        executor.executeCountedLoop(
            startValue = 0,
            endValue = 4,
            isIncrement = true,
            checkCondition = { current, end -> current <= end },
            varName = "i",
            executeBody = {
                values.add(context.getVariable("i") as Int)
            }
        )

        assertEquals(listOf(0, 1, 2, 3, 4), values)
    }

    @Test
    fun `test executeCountedLoop with decrement`() {
        val context = createContext()
        val executor = createExecutor(context)

        val values = mutableListOf<Int>()
        executor.executeCountedLoop(
            startValue = 4,
            endValue = 0,
            isIncrement = false,
            checkCondition = { current, end -> current >= end },
            varName = "i",
            executeBody = {
                values.add(context.getVariable("i") as Int)
            }
        )

        assertEquals(listOf(4, 3, 2, 1, 0), values)
    }

    @Test
    fun `test executeForOfLoop`() {
        val context = createContext()
        val executor = createExecutor(context)

        val items = listOf("a", "b", "c")
        val values = mutableListOf<String>()

        executor.executeForOfLoop(
            items = items,
            varName = "item",
            executeBody = {
                values.add(context.getVariable("item") as String)
            }
        )

        assertEquals(listOf("a", "b", "c"), values)
    }

    @Test
    fun `test executeWhileLoop`() {
        val context = createContext()
        val executor = createExecutor(context)

        var counter = 0
        val values = mutableListOf<Int>()

        executor.executeWhileLoop(
            maxIterations = 100,
            checkCondition = { counter < 5 },
            executeBody = {
                values.add(counter)
                counter++
            }
        )

        assertEquals(listOf(0, 1, 2, 3, 4), values)
    }

    @Test
    fun `test loop stops on return request`() {
        val context = createContext()
        val executor = createExecutor(context)

        var iterations = 0
        executor.executeLoop(
            loopType = "for",
            iterationCount = 100,
            hasNext = { iterations < 100 },
            setupIteration = { iterations++ },
            executeBody = {
                if (iterations == 10) {
                    context.requestReturn()
                }
            }
        )

        assertEquals(10, iterations, "Loop should stop when return is requested")
    }
}
