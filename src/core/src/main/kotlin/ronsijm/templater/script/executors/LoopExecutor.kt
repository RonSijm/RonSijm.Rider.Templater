package ronsijm.templater.script.executors

import ronsijm.templater.script.ScriptContext
import ronsijm.templater.utils.CancellationChecker


class LoopExecutor(
    private val scriptContext: ScriptContext,
    private val cancellationChecker: CancellationChecker,
    private val clearCacheCallback: () -> Unit,
    private val onIterationCompleteCallback: (loopType: String, iterationNumber: Int) -> Unit = { _, _ -> }
) {
    companion object {
        private const val SMALL_LOOP_THRESHOLD = 100
        private const val CANCELLATION_CHECK_INTERVAL = 100
    }


    fun executeLoop(
        loopType: String,
        iterationCount: Int?,
        hasNext: () -> Boolean,
        setupIteration: () -> Unit,
        executeBody: () -> Unit
    ) {

        val isSmallLoop = iterationCount != null && iterationCount <= SMALL_LOOP_THRESHOLD

        var iterationsSinceCheck = 0
        var currentIteration = 0

        while (hasNext()) {
            currentIteration++


            if (!isSmallLoop) {
                if (iterationsSinceCheck >= CANCELLATION_CHECK_INTERVAL) {
                    cancellationChecker.checkCancelled()
                    iterationsSinceCheck = 0
                }
                iterationsSinceCheck++
            }


            clearCacheCallback()


            setupIteration()


            executeBody()


            onIterationCompleteCallback(loopType, currentIteration)


            if (scriptContext.isReturnRequested()) {
                break
            }
        }


        scriptContext.finalizeStringBuilders()
    }


    fun executeCountedLoop(
        startValue: Int,
        endValue: Int,
        isIncrement: Boolean,
        checkCondition: (current: Int, end: Int) -> Boolean,
        varName: String,
        executeBody: () -> Unit
    ) {

        val iterationCount = if (isIncrement) {
            if (endValue >= startValue) endValue - startValue + 1 else 0
        } else {
            if (startValue >= endValue) startValue - endValue + 1 else 0
        }

        var currentValue = startValue

        executeLoop(
            loopType = "for",
            iterationCount = iterationCount,
            hasNext = { checkCondition(currentValue, endValue) },
            setupIteration = { scriptContext.setVariable(varName, currentValue) },
            executeBody = {
                executeBody()

                currentValue = if (isIncrement) currentValue + 1 else currentValue - 1
            }
        )
    }


    fun executeForOfLoop(
        items: List<Any?>,
        varName: String,
        executeBody: () -> Unit
    ) {
        var index = 0

        executeLoop(
            loopType = "for-of",
            iterationCount = items.size,
            hasNext = { index < items.size },
            setupIteration = { scriptContext.setVariable(varName, items[index]) },
            executeBody = {
                executeBody()
                index++
            }
        )
    }


    fun executeWhileLoop(
        maxIterations: Int,
        checkCondition: () -> Boolean,
        executeBody: () -> Unit
    ) {
        var iterations = 0

        executeLoop(
            loopType = "while",
            iterationCount = null,
            hasNext = { iterations < maxIterations && checkCondition() },
            setupIteration = { iterations++ },
            executeBody = executeBody
        )
    }
}
