package ronsijm.templater.debug

import ronsijm.templater.ast.StatementNode
import ronsijm.templater.debug.visualization.DataStructureSnapshot
import ronsijm.templater.debug.visualization.StateChange
import java.util.concurrent.atomic.AtomicInteger


class ExecutionTrace {
    private val steps = mutableListOf<ExecutionStep>()
    private val stepIdCounter = AtomicInteger(0)
    private var currentParentId: Int? = null

    val allSteps: List<ExecutionStep> get() = steps.toList()
    val isEmpty: Boolean get() = steps.isEmpty()
    val size: Int get() = steps.size


    fun recordStep(
        type: ExecutionStep.StepType,
        description: String,
        input: String? = null,
        output: String? = null,
        startPosition: Int? = null,
        endPosition: Int? = null,
        variables: Map<String, Any?> = emptyMap(),
        durationNanos: Long = 0,
        statementNode: StatementNode? = null,
        dataSnapshots: List<DataStructureSnapshot> = emptyList(),
        stateChanges: List<StateChange> = emptyList()
    ): Int {
        val id = stepIdCounter.incrementAndGet()
        val step = ExecutionStep(
            id = id,
            type = type,
            description = description,
            input = input,
            output = output,
            startPosition = startPosition,
            endPosition = endPosition,
            parentId = currentParentId,
            variables = variables,
            durationNanos = durationNanos,
            statementNode = statementNode,
            dataSnapshots = dataSnapshots,
            stateChanges = stateChanges
        )
        steps.add(step)
        return id
    }


    fun enterScope(stepId: Int) {
        currentParentId = stepId
    }


    fun exitScope() {
        currentParentId = steps.findLast { it.id == currentParentId }?.parentId
    }


    fun getStep(id: Int): ExecutionStep? = steps.find { it.id == id }


    fun getChildSteps(parentId: Int): List<ExecutionStep> =
        steps.filter { it.parentId == parentId }


    fun getRootSteps(): List<ExecutionStep> =
        steps.filter { it.parentId == null }


    fun clear() {
        steps.clear()
        stepIdCounter.set(0)
        currentParentId = null
    }


    fun getStatistics(): ExecutionStatistics {
        return ExecutionStatistics.fromTrace(this)
    }
}

