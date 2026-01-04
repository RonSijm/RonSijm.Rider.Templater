package ronsijm.templater.script.profiling


class ScriptExecutorProfiler {

    var evalExprTime = 0L; var evalExprCount = 0L
    var arrayAssignTime = 0L; var arrayAssignCount = 0L
    var simpleAssignTime = 0L; var simpleAssignCount = 0L
    var forLoopTime = 0L; var forLoopCount = 0L
    var ifStatementTime = 0L; var ifStatementCount = 0L
    var varDeclTime = 0L; var varDeclCount = 0L
    var compoundAssignTime = 0L; var compoundAssignCount = 0L
    var incrementTime = 0L; var incrementCount = 0L


    var varDeclRegexTime = 0L
    var varDeclFindOpTime = 0L
    var varDeclEvaluateTime = 0L
    var varDeclSetVarTime = 0L

    fun reset() {
        evalExprTime = 0L; evalExprCount = 0L
        arrayAssignTime = 0L; arrayAssignCount = 0L
        simpleAssignTime = 0L; simpleAssignCount = 0L
        forLoopTime = 0L; forLoopCount = 0L
        ifStatementTime = 0L; ifStatementCount = 0L
        varDeclTime = 0L; varDeclCount = 0L
        compoundAssignTime = 0L; compoundAssignCount = 0L
        incrementTime = 0L; incrementCount = 0L
        varDeclRegexTime = 0L
        varDeclFindOpTime = 0L
        varDeclEvaluateTime = 0L
        varDeclSetVarTime = 0L
    }

    fun getReport(evaluatorProfiler: ScriptEvaluatorProfiler): String {
        val sb = StringBuilder()
        sb.appendLine("=== PROFILING REPORT ===")
        val evalExprAvg = if (evalExprCount > 0) evalExprTime / evalExprCount / 1000 else 0
        sb.appendLine("evalExpr: ${evalExprTime / 1_000_000}ms ($evalExprCount calls, ${evalExprAvg}µs/call)")
        val arrayAvg = if (arrayAssignCount > 0) arrayAssignTime / arrayAssignCount / 1000 else 0
        sb.appendLine("arrayAssign: ${arrayAssignTime / 1_000_000}ms ($arrayAssignCount calls, ${arrayAvg}µs/call)")
        val simpleAvg = if (simpleAssignCount > 0) simpleAssignTime / simpleAssignCount / 1000 else 0
        sb.appendLine("simpleAssign: ${simpleAssignTime / 1_000_000}ms ($simpleAssignCount calls, ${simpleAvg}µs/call)")
        sb.appendLine("forLoop: ${forLoopTime / 1_000_000}ms ($forLoopCount calls)")
        sb.appendLine("ifStatement: ${ifStatementTime / 1_000_000}ms ($ifStatementCount calls)")
        val varDeclAvg = if (varDeclCount > 0) varDeclTime / varDeclCount / 1000 else 0
        sb.appendLine("varDecl: ${varDeclTime / 1_000_000}ms ($varDeclCount calls, ${varDeclAvg}µs/call)")
        sb.appendLine("  - regex strip keyword: ${varDeclRegexTime / 1_000_000}ms")
        sb.appendLine("  - find assignment op: ${varDeclFindOpTime / 1_000_000}ms")
        sb.appendLine("  - evaluateExpression: ${varDeclEvaluateTime / 1_000_000}ms")
        sb.appendLine("  - setVariable: ${varDeclSetVarTime / 1_000_000}ms")
        sb.appendLine("compoundAssign: ${compoundAssignTime / 1_000_000}ms ($compoundAssignCount calls)")
        sb.appendLine("increment: ${incrementTime / 1_000_000}ms ($incrementCount calls)")
        sb.appendLine()
        sb.append(evaluatorProfiler.getReport(
            ProfilingContext.bytecodeVMProfiler,
            ProfilingContext.arithmeticProfiler
        ))
        return sb.toString()
    }
}
