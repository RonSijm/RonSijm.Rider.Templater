package ronsijm.templater.parallel

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ronsijm.templater.utils.Logging
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext




data class ParallelConfig(
    val maxConcurrency: Int = Runtime.getRuntime().availableProcessors(),
    val timeoutMs: Long = 30_000L,
    val failFast: Boolean = true,
    val dispatcher: CoroutineDispatcher = Dispatchers.Default
)


sealed interface ParallelResult<out T> {
    data class Success<T>(val value: T) : ParallelResult<T>
    data class Failure(val error: Throwable) : ParallelResult<Nothing>
    data class Timeout(val message: String) : ParallelResult<Nothing>
    data object Cancelled : ParallelResult<Nothing>
}


class TemplateExecutionScope(
    private val config: ParallelConfig = ParallelConfig(),
    parentContext: CoroutineContext = Dispatchers.Default
) : CoroutineScope {
    companion object {
        private val LOG = Logging.getLogger<TemplateExecutionScope>()
    }

    private val supervisorJob = SupervisorJob()
    private val _isCancelled = AtomicBoolean(false)
    private val _activeTaskCount = AtomicInteger(0)

    override val coroutineContext: CoroutineContext =
        parentContext + supervisorJob + CoroutineExceptionHandler { _, throwable ->
            handleException(throwable)
        }

    val isCancelled: Boolean get() = _isCancelled.get()
    val activeTaskCount: Int get() = _activeTaskCount.get()

    private fun handleException(throwable: Throwable) {

        LOG?.error("Template execution error: ${throwable.message}", throwable)
    }


    suspend fun <T> executeWithTimeout(
        timeoutMs: Long = config.timeoutMs,
        block: suspend () -> T
    ): ParallelResult<T> {
        if (_isCancelled.get()) return ParallelResult.Cancelled

        return try {
            _activeTaskCount.incrementAndGet()
            val result = withTimeout(timeoutMs) { block() }
            ParallelResult.Success(result)
        } catch (e: TimeoutCancellationException) {
            ParallelResult.Timeout("Execution timed out after ${timeoutMs}ms")
        } catch (e: CancellationException) {
            ParallelResult.Cancelled
        } catch (e: Exception) {
            ParallelResult.Failure(e)
        } finally {
            _activeTaskCount.decrementAndGet()
        }
    }


    suspend fun <T, R> executeParallel(
        items: List<T>,
        transform: suspend (T) -> R
    ): List<ParallelResult<R>> {
        if (_isCancelled.get()) return items.map { ParallelResult.Cancelled }

        return items.asFlow()
            .map { item ->
                async(config.dispatcher) {
                    executeWithTimeout { transform(item) }
                }
            }
            .buffer(config.maxConcurrency)
            .map { it.await() }
            .toList()
    }


    suspend fun <T, R> executePhased(
        phases: List<List<T>>,
        transform: suspend (T) -> R
    ): List<List<ParallelResult<R>>> {
        val results = mutableListOf<List<ParallelResult<R>>>()

        for (phase in phases) {
            if (_isCancelled.get()) {
                results.add(phase.map { ParallelResult.Cancelled })
                continue
            }

            val phaseResults = executeParallel(phase, transform)
            results.add(phaseResults)


            if (config.failFast && phaseResults.any { it is ParallelResult.Failure }) {
                cancel()

                for (i in (results.size until phases.size)) {
                    results.add(phases[i].map { ParallelResult.Cancelled })
                }
                break
            }
        }

        return results
    }


    fun cancel() {
        _isCancelled.set(true)
        supervisorJob.cancel()
    }


    suspend fun awaitCompletion() {
        supervisorJob.children.forEach { it.join() }
    }


    fun close() {
        cancel()
        supervisorJob.cancel()
    }
}


suspend fun <T> withTemplateScope(
    config: ParallelConfig = ParallelConfig(),
    block: suspend TemplateExecutionScope.() -> T
): T {
    val scope = TemplateExecutionScope(config)
    return try {
        scope.block()
    } finally {
        scope.close()
    }
}

