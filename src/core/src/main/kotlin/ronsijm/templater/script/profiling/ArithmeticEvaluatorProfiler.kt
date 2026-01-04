package ronsijm.templater.script.profiling


class ArithmeticEvaluatorProfiler {

    var recursiveLeftEvalTime = 0L
    var recursiveLeftEvalCount = 0L
    var recursiveRightEvalTime = 0L
    var recursiveRightEvalCount = 0L

    fun reset() {
        recursiveLeftEvalTime = 0L
        recursiveLeftEvalCount = 0L
        recursiveRightEvalTime = 0L
        recursiveRightEvalCount = 0L
    }

    fun getReport(): String {
        val sb = StringBuilder()
        sb.appendLine("=== ARITHMETIC EVALUATOR BREAKDOWN ===")
        sb.appendLine("recursiveLeftEval: ${recursiveLeftEvalTime / 1_000_000}ms (${recursiveLeftEvalCount} calls, ${if (recursiveLeftEvalCount > 0) recursiveLeftEvalTime / recursiveLeftEvalCount / 1000 else 0}µs/call)")
        sb.appendLine("recursiveRightEval: ${recursiveRightEvalTime / 1_000_000}ms (${recursiveRightEvalCount} calls, ${if (recursiveRightEvalCount > 0) recursiveRightEvalTime / recursiveRightEvalCount / 1000 else 0}µs/call)")
        return sb.toString()
    }
}
