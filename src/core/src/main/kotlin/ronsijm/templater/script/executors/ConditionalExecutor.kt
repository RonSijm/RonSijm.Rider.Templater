package ronsijm.templater.script.executors

import ronsijm.templater.utils.Logging


class ConditionalExecutor(
    private val conditionEvaluator: ConditionEvaluator,
    private val statementExecutor: (String) -> Unit
) {
    companion object {
        private val LOG = Logging.getLogger<ConditionalExecutor>()
    }


    fun execute(
        ifHeader: String,
        ifBody: List<String>,
        elseBranches: List<Pair<String?, List<String>>>
    ) {
        LOG?.debug("executeIfStatement called")
        LOG?.debug("  ifHeader: $ifHeader")
        LOG?.debug("  ifBody: $ifBody")
        LOG?.debug("  elseBranches: $elseBranches")

        val condition = extractCondition(ifHeader)
        LOG?.debug("  condition: $condition")

        val conditionResult = conditionEvaluator.evaluate(condition)
        LOG?.debug("  condition evaluates to: $conditionResult")

        if (conditionResult) {
            LOG?.debug("  Executing if body with ${ifBody.size} statements")
            executeBody(ifBody)
        } else {
            LOG?.debug("  Checking ${elseBranches.size} else branches")
            executeElseBranches(elseBranches)
        }
    }


    private fun extractCondition(ifHeader: String): String {
        val afterIf = ifHeader.substringAfter("if").trim()
        return if (afterIf.startsWith("(") && afterIf.contains(")")) {
            afterIf.substringAfter("(").substringBefore(")")
        } else {
            afterIf.removeSurrounding("(", ")").substringBefore("{").trim()
        }
    }


    private fun executeElseBranches(elseBranches: List<Pair<String?, List<String>>>) {
        for ((elseCondition, elseBody) in elseBranches) {
            if (elseCondition == null) {

                LOG?.debug("  Executing final else with ${elseBody.size} statements")
                executeBody(elseBody)
                break
            } else if (conditionEvaluator.evaluate(elseCondition)) {

                LOG?.debug("  Executing else if with ${elseBody.size} statements")
                executeBody(elseBody)
                break
            }
        }
    }


    private fun executeBody(statements: List<String>) {
        for (statement in statements) {
            statementExecutor(statement)
        }
    }
}

