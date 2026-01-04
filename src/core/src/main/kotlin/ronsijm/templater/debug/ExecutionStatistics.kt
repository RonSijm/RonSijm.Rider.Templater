package ronsijm.templater.debug


data class ExecutionStatistics(

    val totalSteps: Int = 0,
    val statementCount: Int = 0,
    val expressionCount: Int = 0,
    val functionCallCount: Int = 0,
    val variableAssignmentCount: Int = 0,
    val conditionEvaluationCount: Int = 0,
    val loopIterationCount: Int = 0,
    val interpolationCount: Int = 0,


    val maxNestingDepth: Int = 0,
    val totalLoops: Int = 0,
    val totalConditionals: Int = 0,
    val totalFunctions: Int = 0,


    val totalDurationNanos: Long = 0,
    val averageStepDurationNanos: Long = 0,
    val maxStepDurationNanos: Long = 0,
    val minStepDurationNanos: Long = 0,


    val estimatedBigO: String = "O(1)",
    val maxVariableCount: Int = 0,
    val totalVariableUpdates: Int = 0
) {

    fun toFormattedString(): String {
        return buildString {
            appendLine("=== Execution Statistics ===")
            appendLine()
            appendLine("Operations:")
            appendLine("  Total steps: $totalSteps")
            appendLine("  Statements: $statementCount")
            appendLine("  Expressions: $expressionCount")
            appendLine("  Function calls: $functionCallCount")
            appendLine("  Variable assignments: $variableAssignmentCount")
            appendLine("  Condition evaluations: $conditionEvaluationCount")
            appendLine("  Loop iterations: $loopIterationCount")
            appendLine("  Interpolations: $interpolationCount")
            appendLine()
            appendLine("Control Flow:")
            appendLine("  Max nesting depth: $maxNestingDepth")
            appendLine("  Total loops: $totalLoops")
            appendLine("  Total conditionals: $totalConditionals")
            appendLine("  Total functions: $totalFunctions")
            appendLine("  Estimated complexity: $estimatedBigO")
            appendLine()
            if (totalDurationNanos > 0) {
                appendLine("Performance:")
                appendLine("  Total duration: ${formatDuration(totalDurationNanos)}")
                appendLine("  Average step duration: ${formatDuration(averageStepDurationNanos)}")
                appendLine("  Max step duration: ${formatDuration(maxStepDurationNanos)}")
                appendLine("  Min step duration: ${formatDuration(minStepDurationNanos)}")
                appendLine()
            }
            appendLine("Memory:")
            appendLine("  Max variables: $maxVariableCount")
            appendLine("  Total variable updates: $totalVariableUpdates")
            appendLine()
            appendLine("============================")
        }
    }

    private fun formatDuration(nanos: Long): String {
        return when {
            nanos < 1_000 -> "${nanos}ns"
            nanos < 1_000_000 -> String.format("%.2fµs", nanos / 1_000.0)
            nanos < 1_000_000_000 -> String.format("%.2fms", nanos / 1_000_000.0)
            else -> String.format("%.2fs", nanos / 1_000_000_000.0)
        }
    }

    companion object {

        fun fromTrace(trace: ExecutionTrace): ExecutionStatistics {
            val steps = trace.allSteps
            if (steps.isEmpty()) {
                return ExecutionStatistics()
            }

            var statementCount = 0
            var expressionCount = 0
            var functionCallCount = 0
            var variableAssignmentCount = 0
            var conditionEvaluationCount = 0
            var loopIterationCount = 0
            var interpolationCount = 0

            var maxNestingDepth = 0
            var currentDepth = 0
            var totalLoops = 0
            var totalConditionals = 0
            var totalFunctions = 0

            var totalDurationNanos = 0L
            var maxStepDurationNanos = 0L
            var minStepDurationNanos = Long.MAX_VALUE

            var maxVariableCount = 0
            var totalVariableUpdates = 0

            for (step in steps) {

                when (step.type) {
                    ExecutionStep.StepType.STATEMENT -> statementCount++
                    ExecutionStep.StepType.EXPRESSION_EVAL -> expressionCount++
                    ExecutionStep.StepType.FUNCTION_CALL -> functionCallCount++
                    ExecutionStep.StepType.VARIABLE_ASSIGN -> {
                        variableAssignmentCount++
                        totalVariableUpdates++
                    }
                    ExecutionStep.StepType.CONDITION_EVAL -> conditionEvaluationCount++
                    ExecutionStep.StepType.LOOP_ITERATION -> loopIterationCount++
                    ExecutionStep.StepType.BLOCK_START -> {
                        currentDepth++
                        maxNestingDepth = maxOf(maxNestingDepth, currentDepth)


                        when {
                            step.description.startsWith("for") ||
                            step.description.startsWith("while") -> totalLoops++
                            step.description.startsWith("if") -> totalConditionals++
                            step.description.startsWith("function") -> totalFunctions++
                        }
                    }
                    ExecutionStep.StepType.BLOCK_END -> currentDepth--
                    else -> {}
                }


                if (step.durationNanos > 0) {
                    totalDurationNanos += step.durationNanos
                    maxStepDurationNanos = maxOf(maxStepDurationNanos, step.durationNanos)
                    minStepDurationNanos = minOf(minStepDurationNanos, step.durationNanos)
                }


                maxVariableCount = maxOf(maxVariableCount, step.variables.size)
            }


            val stepsWithDuration = steps.count { it.durationNanos > 0 }
            val averageStepDurationNanos = if (stepsWithDuration > 0) {
                totalDurationNanos / stepsWithDuration
            } else {
                0L
            }


            val estimatedBigO = estimateBigO(totalLoops, loopIterationCount, totalConditionals)




            return ExecutionStatistics(
                totalSteps = steps.size,
                statementCount = statementCount,
                expressionCount = expressionCount,
                functionCallCount = functionCallCount,
                variableAssignmentCount = variableAssignmentCount,
                conditionEvaluationCount = conditionEvaluationCount,
                loopIterationCount = loopIterationCount,
                interpolationCount = interpolationCount,
                maxNestingDepth = maxNestingDepth,
                totalLoops = totalLoops,
                totalConditionals = totalConditionals,
                totalFunctions = totalFunctions,
                totalDurationNanos = totalDurationNanos,
                averageStepDurationNanos = averageStepDurationNanos,
                maxStepDurationNanos = maxStepDurationNanos,
                minStepDurationNanos = if (minStepDurationNanos == Long.MAX_VALUE) 0L else minStepDurationNanos,
                estimatedBigO = estimatedBigO,
                maxVariableCount = maxVariableCount,
                totalVariableUpdates = totalVariableUpdates
            )
        }


        @Suppress("UnusedParameter")
        private fun estimateBigO(totalLoops: Int, loopIterations: Int, totalConditionals: Int): String {
            return when {
                totalLoops == 0 -> "O(1)"
                totalLoops == 1 -> "O(n)"
                totalLoops == 2 && loopIterations > 10 -> "O(n²)"
                totalLoops >= 3 -> "O(n³)"
                else -> "O(n)"
            }
        }
    }
}

