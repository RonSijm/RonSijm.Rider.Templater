package ronsijm.templater.debug

import ronsijm.templater.ast.StatementNode
import ronsijm.templater.debug.visualization.DataStructureSnapshot
import ronsijm.templater.debug.visualization.StateChange


data class ExecutionStep(
    val id: Int,
    val type: StepType,
    val description: String,
    val input: String? = null,
    val output: String? = null,
    val startPosition: Int? = null,
    val endPosition: Int? = null,
    val parentId: Int? = null,
    val variables: Map<String, Any?> = emptyMap(),
    val durationNanos: Long = 0,

    val statementNode: StatementNode? = null,
    val dataSnapshots: List<DataStructureSnapshot> = emptyList(),
    val stateChanges: List<StateChange> = emptyList()
) {

    val displayLineNumber: Int? get() = statementNode?.lineNumber
    enum class StepType {
        TEMPLATE_START,
        TEMPLATE_END,
        BLOCK_START,
        BLOCK_END,
        STATEMENT,
        EXPRESSION_EVAL,
        VARIABLE_ASSIGN,
        FUNCTION_CALL,
        CONDITION_EVAL,
        LOOP_ITERATION,
        ERROR
    }
}

