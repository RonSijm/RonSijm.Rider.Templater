package ronsijm.templater.script.profiling


object ProfilingContext {

    private val enabledThreadLocal = ThreadLocal.withInitial { false }
    private val executorProfilerThreadLocal = ThreadLocal.withInitial { ScriptExecutorProfiler() }
    private val evaluatorProfilerThreadLocal = ThreadLocal.withInitial { ScriptEvaluatorProfiler() }
    private val bytecodeVMProfilerThreadLocal = ThreadLocal.withInitial { BytecodeVMProfiler() }
    private val arithmeticProfilerThreadLocal = ThreadLocal.withInitial { ArithmeticEvaluatorProfiler() }


    var isEnabled: Boolean
        get() = enabledThreadLocal.get()
        set(value) = enabledThreadLocal.set(value)


    val executorProfiler: ScriptExecutorProfiler
        get() = executorProfilerThreadLocal.get()


    val evaluatorProfiler: ScriptEvaluatorProfiler
        get() = evaluatorProfilerThreadLocal.get()


    val bytecodeVMProfiler: BytecodeVMProfiler
        get() = bytecodeVMProfilerThreadLocal.get()


    val arithmeticProfiler: ArithmeticEvaluatorProfiler
        get() = arithmeticProfilerThreadLocal.get()


    fun reset() {
        executorProfiler.reset()
        evaluatorProfiler.reset()
        bytecodeVMProfiler.reset()
        arithmeticProfiler.reset()
    }


    fun getReport(): String {
        return executorProfiler.getReport(evaluatorProfiler)
    }


    inline fun <T> withProfiling(block: () -> T): T {
        val wasEnabled = isEnabled
        isEnabled = true
        reset()
        try {
            return block()
        } finally {
            isEnabled = wasEnabled
        }
    }


    inline fun <T> profiled(
        timeAccumulator: kotlin.reflect.KMutableProperty0<Long>,
        countAccumulator: kotlin.reflect.KMutableProperty0<Long>,
        block: () -> T
    ): T {
        return if (isEnabled) {
            val t0 = System.nanoTime()
            val result = block()
            timeAccumulator.set(timeAccumulator.get() + (System.nanoTime() - t0))
            countAccumulator.set(countAccumulator.get() + 1)
            result
        } else {
            block()
        }
    }


    inline fun <T> profiledTime(
        timeAccumulator: kotlin.reflect.KMutableProperty0<Long>,
        block: () -> T
    ): T {
        return if (isEnabled) {
            val t0 = System.nanoTime()
            val result = block()
            timeAccumulator.set(timeAccumulator.get() + (System.nanoTime() - t0))
            result
        } else {
            block()
        }
    }


    fun cleanup() {
        enabledThreadLocal.remove()
        executorProfilerThreadLocal.remove()
        evaluatorProfilerThreadLocal.remove()
        bytecodeVMProfilerThreadLocal.remove()
        arithmeticProfilerThreadLocal.remove()
    }
}

