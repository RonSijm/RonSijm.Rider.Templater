package ronsijm.templater.script

import ronsijm.templater.ast.StatementNode


interface ScriptExecutionCallback {

    fun setCurrentBlockLineNumber(lineNumber: Int) {}
    fun getVariableUpdater(): VariableUpdater? = null
    fun setCurrentBlockContent(content: String) {}

    fun onBlockProcessed(
        originalBlock: String,
        replacement: String,
        currentDocument: String,
        lineNumber: Int
    ) {}


    fun beforeStatement(
        node: StatementNode,
        variables: Map<String, Any?>
    ): ExecutionAction = ExecutionAction.CONTINUE


    fun afterStatement(
        node: StatementNode,
        variables: Map<String, Any?>
    ) {}

    fun enterBlock(blockType: String, blockDescription: String) {}
    fun exitBlock(blockType: String) {}


    fun onLoopIteration(loopType: String, iterationNumber: Int, variables: Map<String, Any?>) {}
}

enum class ExecutionAction {
    CONTINUE,
    STOP
}

object NoOpExecutionCallback : ScriptExecutionCallback {

}

interface VariableUpdater {
    fun updateVariable(name: String, value: String): Boolean
}


interface MutableVariableUpdaterWrapper : VariableUpdater {
    fun setDelegate(updater: VariableUpdater?)
}

