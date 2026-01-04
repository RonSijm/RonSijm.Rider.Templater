package ronsijm.templater.parallel

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong


interface ProgressReporter {

    fun report(progress: ExecutionProgress)


    fun phaseStarted(phaseName: String, totalItems: Int)


    fun itemCompleted(itemName: String, success: Boolean)


    fun completed(success: Boolean, message: String? = null)
}


data class ExecutionProgress(
    val currentPhase: String,
    val completedItems: Int,
    val totalItems: Int,
    val elapsedMs: Long,
    val status: ExecutionStatus
) {
    val percentComplete: Double
        get() = if (totalItems > 0) (completedItems.toDouble() / totalItems) * 100 else 0.0

    val itemsPerSecond: Double
        get() = if (elapsedMs > 0) (completedItems.toDouble() / elapsedMs) * 1000 else 0.0
}


enum class ExecutionStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELLED
}


class FlowProgressReporter : ProgressReporter {
    private val _progress = MutableSharedFlow<ExecutionProgress>(replay = 1)
    private val startTime = AtomicLong(System.currentTimeMillis())
    private val completedCount = AtomicInteger(0)
    private var totalCount = AtomicInteger(0)
    private var currentPhaseName = ""


    val progressFlow: SharedFlow<ExecutionProgress> = _progress.asSharedFlow()

    override fun report(progress: ExecutionProgress) {
        _progress.tryEmit(progress)
    }

    override fun phaseStarted(phaseName: String, totalItems: Int) {
        currentPhaseName = phaseName
        totalCount.set(totalItems)
        completedCount.set(0)
        startTime.set(System.currentTimeMillis())

        report(ExecutionProgress(
            currentPhase = phaseName,
            completedItems = 0,
            totalItems = totalItems,
            elapsedMs = 0,
            status = ExecutionStatus.IN_PROGRESS
        ))
    }

    override fun itemCompleted(itemName: String, success: Boolean) {
        val completed = completedCount.incrementAndGet()
        val elapsed = System.currentTimeMillis() - startTime.get()

        report(ExecutionProgress(
            currentPhase = currentPhaseName,
            completedItems = completed,
            totalItems = totalCount.get(),
            elapsedMs = elapsed,
            status = ExecutionStatus.IN_PROGRESS
        ))
    }

    override fun completed(success: Boolean, message: String?) {
        val elapsed = System.currentTimeMillis() - startTime.get()

        report(ExecutionProgress(
            currentPhase = currentPhaseName,
            completedItems = completedCount.get(),
            totalItems = totalCount.get(),
            elapsedMs = elapsed,
            status = if (success) ExecutionStatus.COMPLETED else ExecutionStatus.FAILED
        ))
    }
}


object NoOpProgressReporter : ProgressReporter {
    override fun report(progress: ExecutionProgress) {}
    override fun phaseStarted(phaseName: String, totalItems: Int) {}
    override fun itemCompleted(itemName: String, success: Boolean) {}
    override fun completed(success: Boolean, message: String?) {}
}


class ConsoleProgressReporter : ProgressReporter {
    override fun report(progress: ExecutionProgress) {
        println("[${progress.status}] ${progress.currentPhase}: ${progress.completedItems}/${progress.totalItems} (${progress.percentComplete.toInt()}%)")
    }

    override fun phaseStarted(phaseName: String, totalItems: Int) {
        println("Starting phase: $phaseName ($totalItems items)")
    }

    override fun itemCompleted(itemName: String, success: Boolean) {
        val status = if (success) "?" else "?"
        println("  $status $itemName")
    }

    override fun completed(success: Boolean, message: String?) {
        val status = if (success) "SUCCESS" else "FAILED"
        println("Execution $status${message?.let { ": $it" } ?: ""}")
    }
}


class CompositeProgressReporter(
    private val reporters: List<ProgressReporter>
) : ProgressReporter {
    override fun report(progress: ExecutionProgress) {
        reporters.forEach { it.report(progress) }
    }

    override fun phaseStarted(phaseName: String, totalItems: Int) {
        reporters.forEach { it.phaseStarted(phaseName, totalItems) }
    }

    override fun itemCompleted(itemName: String, success: Boolean) {
        reporters.forEach { it.itemCompleted(itemName, success) }
    }

    override fun completed(success: Boolean, message: String?) {
        reporters.forEach { it.completed(success, message) }
    }
}

