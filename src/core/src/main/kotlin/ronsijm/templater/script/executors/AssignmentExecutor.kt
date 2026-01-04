package ronsijm.templater.script.executors

import ronsijm.templater.script.ScriptContext
import ronsijm.templater.script.profiling.ProfilingContext
import ronsijm.templater.script.profiling.ScriptExecutorProfiler


@Suppress("UnusedPrivateProperty")
class AssignmentExecutor(
    private val scriptContext: ScriptContext,
    private val evaluateExpression: (String) -> Any?,
    private val profiler: ScriptExecutorProfiler,
    private val profilingEnabled: () -> Boolean
) {


    fun executeVariableDeclaration(statement: String) {

        val withoutKeyword = ProfilingContext.profiledTime(profiler::varDeclRegexTime) {
            when {
                statement.startsWith("const ") -> statement.substring(6)
                statement.startsWith("let ") -> statement.substring(4)
                statement.startsWith("var ") -> statement.substring(4)
                else -> statement
            }
        }

        executeAssignmentWithProfiling(withoutKeyword)
    }


    private fun executeAssignmentWithProfiling(statement: String) {

        val assignmentIndex = ProfilingContext.profiledTime(profiler::varDeclFindOpTime) {
            findAssignmentOperator(statement)
        }
        if (assignmentIndex == -1) return

        val varName = statement.substring(0, assignmentIndex).trim()
        val valueExpr = statement.substring(assignmentIndex + 1).trim()

        val value = ProfilingContext.profiledTime(profiler::varDeclEvaluateTime) {
            evaluateExpression(valueExpr)
        }


        ProfilingContext.profiledTime(profiler::varDeclSetVarTime) {
            if (varName.contains("[") && varName.endsWith("]")) {
                executeArrayElementAssignment(varName, value)
            } else {
                scriptContext.setVariable(varName, value)
            }
        }
    }


    fun executeAssignment(statement: String) {

        val assignmentIndex = findAssignmentOperator(statement)
        if (assignmentIndex == -1) return

        val varName = statement.substring(0, assignmentIndex).trim()
        val valueExpr = statement.substring(assignmentIndex + 1).trim()
        val value = evaluateExpression(valueExpr)


        if (varName.contains("[") && varName.endsWith("]")) {
            executeArrayElementAssignment(varName, value)
        } else {
            scriptContext.setVariable(varName, value)
        }
    }


    fun executeCompoundAssignment(statement: String) {
        val operators = listOf("+=", "-=", "*=", "/=")
        for (op in operators) {
            val index = statement.indexOf(op)
            if (index != -1) {
                val varName = statement.substring(0, index).trim()
                val valueExpr = statement.substring(index + 2).trim()
                val currentValue = scriptContext.getVariable(varName)
                val addValue = evaluateExpression(valueExpr)

                val newValue = when (op) {
                    "+=" -> {
                        if (currentValue is Number && addValue is Number) {
                            currentValue.toDouble() + addValue.toDouble()
                        } else {



                            when (currentValue) {
                                is StringBuilder -> {
                                    currentValue.append(addValue?.toString() ?: "")
                                    currentValue
                                }
                                else -> {

                                    val sb = StringBuilder(currentValue?.toString() ?: "")
                                    sb.append(addValue?.toString() ?: "")
                                    sb
                                }
                            }
                        }
                    }
                    "-=" -> {
                        if (currentValue is Number && addValue is Number) {
                            currentValue.toDouble() - addValue.toDouble()
                        } else currentValue
                    }
                    "*=" -> {
                        if (currentValue is Number && addValue is Number) {
                            currentValue.toDouble() * addValue.toDouble()
                        } else currentValue
                    }
                    "/=" -> {
                        if (currentValue is Number && addValue is Number) {
                            currentValue.toDouble() / addValue.toDouble()
                        } else currentValue
                    }
                    else -> currentValue
                }
                scriptContext.setVariable(varName, newValue)
                return
            }
        }
    }


    private fun executeArrayElementAssignment(target: String, value: Any?) {
        val bracketIndex = target.indexOf('[')
        val arrayName = target.substring(0, bracketIndex).trim()
        val indexExpr = target.substring(bracketIndex + 1, target.length - 1).trim()

        val array = scriptContext.getVariable(arrayName)
        val index = evaluateExpression(indexExpr)

        if (array is MutableList<*> && index is Number) {
            @Suppress("UNCHECKED_CAST")
            val mutableArray = array as MutableList<Any?>
            val idx = index.toInt()
            if (idx >= 0 && idx < mutableArray.size) {
                mutableArray[idx] = value
            }
        }
    }


    fun executeResultAccumulator(statement: String) {
        when {
            statement.contains("+=") -> {
                val value = statement.substringAfter("+=").trim()
                val evaluated = evaluateExpression(value)
                scriptContext.appendToResult(evaluated?.toString() ?: "")
            }
            statement.contains("=") -> {
                val value = statement.substringAfter("=").trim()
                val evaluated = evaluateExpression(value)
                scriptContext.setResult(evaluated?.toString() ?: "")
            }
        }
    }


    fun executeIncrement(varName: String) {
        val currentValue = scriptContext.getVariable(varName)
        if (currentValue is Number) {
            scriptContext.setVariable(varName, currentValue.toInt() + 1)
        }
    }


    fun executeDecrement(varName: String) {
        val currentValue = scriptContext.getVariable(varName)
        if (currentValue is Number) {
            scriptContext.setVariable(varName, currentValue.toInt() - 1)
        }
    }


    private fun findAssignmentOperator(statement: String): Int {
        var i = 0
        while (i < statement.length) {
            if (statement[i] == '=') {

                val prevChar = if (i > 0) statement[i - 1] else ' '
                val nextChar = if (i < statement.length - 1) statement[i + 1] else ' '

                if (prevChar != '!' && prevChar != '>' && prevChar != '<' && prevChar != '=' &&
                    nextChar != '=' && nextChar != '>') {
                    return i
                }
            }
            i++
        }
        return -1
    }
}
