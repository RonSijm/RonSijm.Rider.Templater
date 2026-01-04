package ronsijm.templater.debug

import ronsijm.templater.ast.StatementNode
import ronsijm.templater.debug.visualization.DataStructureSnapshot
import ronsijm.templater.debug.visualization.StateChange
import ronsijm.templater.script.VariableUpdater
import ronsijm.templater.utils.Logging


class DebugSession(
    private val onBreakpoint: (DebugBreakpoint) -> DebugAction,
    private val onComplete: (ExecutionTrace) -> Unit = {},
    startInStepMode: Boolean = false
) {
    companion object {
        private val LOG = Logging.getLogger<DebugSession>()
    }

    private val trace = ExecutionTrace()
    private val breakpoints = mutableSetOf<StatementNode>()

    private var stepMode = if (startInStepMode) StepMode.STEP_INTO else StepMode.CONTINUE


    @Volatile
    var variableUpdater: VariableUpdater? = null

    enum class StepMode {
        CONTINUE,
        STEP_INTO,
        STEP_OVER,
        STEP_OUT
    }




    fun addBreakpoint(node: StatementNode) {
        breakpoints.add(node)
    }


    fun removeBreakpoint(node: StatementNode) {
        breakpoints.remove(node)
    }


    fun hasBreakpoint(node: StatementNode): Boolean = node in breakpoints


    fun getBreakpoints(): Set<StatementNode> = breakpoints.toSet()


    fun recordStep(
        type: ExecutionStep.StepType,
        description: String,
        input: String? = null,
        output: String? = null,
        startPosition: Int? = null,
        endPosition: Int? = null,
        variables: Map<String, Any?> = emptyMap(),
        statementNode: StatementNode? = null,
        dataSnapshots: List<DataStructureSnapshot> = emptyList(),
        stateChanges: List<StateChange> = emptyList()
    ): DebugAction {
        val stepId = trace.recordStep(
            type = type,
            description = description,
            input = input,
            output = output,
            startPosition = startPosition,
            endPosition = endPosition,
            variables = variables,
            statementNode = statementNode,
            dataSnapshots = dataSnapshots,
            stateChanges = stateChanges
        )





        val hasBreakpoint = if (statementNode != null && type == ExecutionStep.StepType.STATEMENT) {
            hasBreakpoint(statementNode)
        } else {
            false
        }

        if (statementNode != null && type == ExecutionStep.StepType.STATEMENT) {
            LOG?.debug(
                "Statement at line ${statementNode.lineNumber}, nodeId=${statementNode.id}, " +
                "hasBreakpoint=$hasBreakpoint, stepMode=$stepMode, desc='${description.take(50)}'"
            )
        }

        val shouldPause = when {
            stepMode == StepMode.STEP_INTO -> true
            hasBreakpoint -> true
            else -> false
        }

        if (shouldPause) {
            if (hasBreakpoint) {
                LOG?.debug("PAUSING at breakpoint: nodeId=${statementNode?.id}, line=${statementNode?.lineNumber}")
            }
            val breakpoint = DebugBreakpoint(
                stepId = stepId,
                step = trace.getStep(stepId)!!,
                trace = trace
            )
            val action = onBreakpoint(breakpoint)
            stepMode = when (action) {
                DebugAction.CONTINUE -> StepMode.CONTINUE
                DebugAction.STEP_INTO -> StepMode.STEP_INTO
                DebugAction.STEP_OVER -> StepMode.STEP_OVER
                DebugAction.STEP_OUT -> StepMode.STEP_OUT
                DebugAction.STOP -> return DebugAction.STOP
            }
            return action
        }

        return DebugAction.CONTINUE
    }


    fun getTrace(): ExecutionTrace = trace


    fun complete() {
        onComplete(trace)
    }
}


data class DebugBreakpoint(
    val stepId: Int,
    val step: ExecutionStep,
    val trace: ExecutionTrace
) {

    val statementNode: StatementNode? get() = step.statementNode


    val displayLineNumber: Int? get() = statementNode?.lineNumber
}


enum class DebugAction {
    CONTINUE,
    STEP_INTO,
    STEP_OVER,
    STEP_OUT,
    STOP
}
