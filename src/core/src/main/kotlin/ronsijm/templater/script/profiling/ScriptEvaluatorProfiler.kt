package ronsijm.templater.script.profiling


class ScriptEvaluatorProfiler {

    var findArithmeticOpTime = 0L; var findArithmeticOpCount = 0L
    var evaluateArithmeticTime = 0L; var evaluateArithmeticCount = 0L
    var findComparisonOpTime = 0L; var findComparisonOpCount = 0L
    var evaluateComparisonTime = 0L; var evaluateComparisonCount = 0L
    var variableLookupTime = 0L; var variableLookupCount = 0L
    var arrayAccessTime = 0L; var arrayAccessCount = 0L
    var literalParseTime = 0L; var literalParseCount = 0L
    var recursiveEvalTime = 0L; var recursiveEvalCount = 0L
    var bytecodeEvalTime = 0L; var bytecodeEvalCount = 0L

    fun reset() {
        findArithmeticOpTime = 0L; findArithmeticOpCount = 0L
        evaluateArithmeticTime = 0L; evaluateArithmeticCount = 0L
        findComparisonOpTime = 0L; findComparisonOpCount = 0L
        evaluateComparisonTime = 0L; evaluateComparisonCount = 0L
        variableLookupTime = 0L; variableLookupCount = 0L
        arrayAccessTime = 0L; arrayAccessCount = 0L
        literalParseTime = 0L; literalParseCount = 0L
        recursiveEvalTime = 0L; recursiveEvalCount = 0L
        bytecodeEvalTime = 0L; bytecodeEvalCount = 0L
    }

    fun getReport(bytecodeVMProfiler: BytecodeVMProfiler, arithmeticProfiler: ArithmeticEvaluatorProfiler): String {
        val sb = StringBuilder()
        sb.appendLine("=== EXPRESSION EVALUATION BREAKDOWN ===")
        sb.appendLine("findArithmeticOp: ${findArithmeticOpTime / 1_000_000}ms (${findArithmeticOpCount} calls, ${if (findArithmeticOpCount > 0) findArithmeticOpTime / findArithmeticOpCount / 1000 else 0}탎/call)")
        sb.appendLine("evaluateArithmetic: ${evaluateArithmeticTime / 1_000_000}ms (${evaluateArithmeticCount} calls, ${if (evaluateArithmeticCount > 0) evaluateArithmeticTime / evaluateArithmeticCount / 1000 else 0}탎/call)")
        sb.appendLine("findComparisonOp: ${findComparisonOpTime / 1_000_000}ms (${findComparisonOpCount} calls, ${if (findComparisonOpCount > 0) findComparisonOpTime / findComparisonOpCount / 1000 else 0}탎/call)")
        sb.appendLine("evaluateComparison: ${evaluateComparisonTime / 1_000_000}ms (${evaluateComparisonCount} calls, ${if (evaluateComparisonCount > 0) evaluateComparisonTime / evaluateComparisonCount / 1000 else 0}탎/call)")
        sb.appendLine("variableLookup: ${variableLookupTime / 1_000_000}ms (${variableLookupCount} calls, ${if (variableLookupCount > 0) variableLookupTime / variableLookupCount / 1000 else 0}탎/call)")
        sb.appendLine("arrayAccess: ${arrayAccessTime / 1_000_000}ms (${arrayAccessCount} calls, ${if (arrayAccessCount > 0) arrayAccessTime / arrayAccessCount / 1000 else 0}탎/call)")
        sb.appendLine("literalParse: ${literalParseTime / 1_000_000}ms (${literalParseCount} calls, ${if (literalParseCount > 0) literalParseTime / literalParseCount / 1000 else 0}탎/call)")
        sb.appendLine("recursiveEval: ${recursiveEvalTime / 1_000_000}ms (${recursiveEvalCount} calls, ${if (recursiveEvalCount > 0) recursiveEvalTime / recursiveEvalCount / 1000 else 0}탎/call)")
        sb.appendLine("bytecodeEval: ${bytecodeEvalTime / 1_000_000}ms (${bytecodeEvalCount} calls, ${if (bytecodeEvalCount > 0) bytecodeEvalTime / bytecodeEvalCount / 1000 else 0}탎/call)")
        sb.appendLine()
        sb.append(bytecodeVMProfiler.getReport())
        sb.appendLine()
        sb.append(arithmeticProfiler.getReport())
        return sb.toString()
    }
}
