package ronsijm.templater.script.executors

import ronsijm.templater.script.ScriptContext
import ronsijm.templater.script.parser.TryCatchParser
import ronsijm.templater.utils.Logging


class TryCatchExecutor(
    private val scriptContext: ScriptContext,
    private val statementExecutor: (String) -> Unit
) {
    companion object {
        private val LOG = Logging.getLogger<TryCatchExecutor>()
    }


    fun execute(tryCatchInfo: TryCatchParser.TryCatchInfo) {
        LOG?.debug("executeTryCatch called")
        LOG?.debug("  tryBody: ${tryCatchInfo.tryBody}")
        LOG?.debug("  catchVarName: ${tryCatchInfo.catchVarName}")
        LOG?.debug("  catchBody: ${tryCatchInfo.catchBody}")
        LOG?.debug("  finallyBody: ${tryCatchInfo.finallyBody}")

        try {
            executeTryBlock(tryCatchInfo.tryBody)
        } catch (e: Exception) {
            LOG?.debug("Exception caught: ${e.message}")
            executeCatchBlock(tryCatchInfo.catchVarName, tryCatchInfo.catchBody, e)
        } finally {
            executeFinallyBlock(tryCatchInfo.finallyBody)
        }
    }


    private fun executeTryBlock(tryBody: List<String>) {
        for (statement in tryBody) {
            statementExecutor(statement)
        }
    }


    private fun executeCatchBlock(catchVarName: String?, catchBody: List<String>, exception: Exception) {

        if (catchVarName != null) {
            scriptContext.setVariable(catchVarName, exception.message ?: "Unknown error")
        }


        for (statement in catchBody) {
            statementExecutor(statement)
        }


        if (catchVarName != null) {
            scriptContext.removeVariable(catchVarName)
        }
    }


    private fun executeFinallyBlock(finallyBody: List<String>) {
        for (statement in finallyBody) {
            statementExecutor(statement)
        }
    }
}

