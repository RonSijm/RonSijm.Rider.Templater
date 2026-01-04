package ronsijm.templater.script.executors

import ronsijm.templater.common.ScriptEngineDefaults
import ronsijm.templater.script.ClassifiedStatement
import ronsijm.templater.script.ScriptParser
import ronsijm.templater.script.parser.IfStatementParser


class StatementClassifier(private val parser: ScriptParser) {

    private val classificationCache = LinkedHashMap<String, ClassifiedStatement>(
        ScriptEngineDefaults.STATEMENT_CACHE_SIZE, 0.75f, true
    )
    private val maxCacheSize = ScriptEngineDefaults.STATEMENT_CACHE_SIZE


    fun classify(statement: String): ClassifiedStatement {
        classificationCache[statement]?.let { return it }

        val stmt = statement.trim()
        val classified = classifyStatement(stmt, statement)


        if (classificationCache.size >= maxCacheSize) {
            val firstKey = classificationCache.keys.first()
            classificationCache.remove(firstKey)
        }
        classificationCache[statement] = classified

        return classified
    }


    fun classifyAll(statements: List<String>): List<ClassifiedStatement> {
        return statements.map { classify(it) }
    }


    fun clearCache() {
        classificationCache.clear()
    }

    private fun classifyStatement(stmt: String, original: String): ClassifiedStatement {
        return when {
            stmt.isEmpty() -> ClassifiedStatement.Empty(original)
            stmt == "{" || stmt == "}" -> ClassifiedStatement.Brace(original)
            stmt == "return" || stmt == "return;" -> ClassifiedStatement.ReturnVoid(original)
            stmt.startsWith("return ") -> {
                val valueExpr = stmt.removePrefix("return ").removeSuffix(";").trim()
                ClassifiedStatement.ReturnValue(valueExpr)
            }
            stmt.startsWith("for ") || stmt.startsWith("for(") -> classifyForLoop(stmt, original)
            stmt.startsWith("while ") || stmt.startsWith("while(") -> classifyWhileLoop(stmt, original)
            stmt.startsWith("if ") || stmt.startsWith("if(") -> classifyIfStatement(stmt, original)
            stmt.startsWith("function ") -> ClassifiedStatement.FunctionDecl(original)
            stmt.startsWith("let ") || stmt.startsWith("const ") || stmt.startsWith("var ") ->
                ClassifiedStatement.VarDecl(original)
            stmt.startsWith("tR") -> ClassifiedStatement.TrAccumulator(original)
            stmt.endsWith("++") -> ClassifiedStatement.Increment(stmt.dropLast(2).trim())
            stmt.endsWith("--") -> ClassifiedStatement.Decrement(stmt.dropLast(2).trim())
            stmt.contains("+=") || stmt.contains("-=") || stmt.contains("*=") || stmt.contains("/=") ->
                ClassifiedStatement.CompoundAssign(original)
            stmt.contains("=") && !stmt.contains("==") && !stmt.contains("=>") ->
                classifyAssignment(stmt, original)
            stmt.contains("(") -> ClassifiedStatement.FunctionCall(original)
            else -> ClassifiedStatement.Other(original)
        }
    }

    private fun classifyForLoop(stmt: String, original: String): ClassifiedStatement {
        val loopInfo = parser.extractForLoop(listOf(stmt), 0)
        return if (loopInfo != null) {
            ClassifiedStatement.ForLoop(loopInfo.first, loopInfo.second)
        } else {
            ClassifiedStatement.Other(original)
        }
    }

    private fun classifyWhileLoop(stmt: String, original: String): ClassifiedStatement {
        val loopInfo = parser.extractWhileLoop(listOf(stmt), 0)
        return if (loopInfo != null) {
            ClassifiedStatement.WhileLoop(loopInfo.first, loopInfo.second)
        } else {
            ClassifiedStatement.Other(original)
        }
    }

    private fun classifyIfStatement(stmt: String, original: String): ClassifiedStatement {
        val ifInfo = IfStatementParser.extractIfStatementFromSingleStatement(stmt, 0, parser)
        return if (ifInfo != null) {
            ClassifiedStatement.IfStatement(ifInfo.first, ifInfo.second, ifInfo.third)
        } else {
            ClassifiedStatement.Other(original)
        }
    }

    private fun classifyAssignment(stmt: String, original: String): ClassifiedStatement {
        val bracketIdx = stmt.indexOf('[')
        val eqIdx = stmt.indexOf('=')

        return if (bracketIdx > 0 && bracketIdx < eqIdx) {
            val closeBracket = stmt.indexOf(']', bracketIdx)
            if (closeBracket > bracketIdx && closeBracket < eqIdx) {
                val arrayName = stmt.substring(0, bracketIdx).trim()
                val indexExpr = stmt.substring(bracketIdx + 1, closeBracket).trim()
                val valueExpr = stmt.substring(eqIdx + 1).removeSuffix(";").trim()
                ClassifiedStatement.ArrayAssign(arrayName, indexExpr, valueExpr)
            } else {
                ClassifiedStatement.Assignment(original)
            }
        } else if (!stmt.contains('[')) {
            val varName = stmt.substring(0, eqIdx).trim()
            val valueExpr = stmt.substring(eqIdx + 1).removeSuffix(";").trim()
            ClassifiedStatement.SimpleAssign(varName, valueExpr)
        } else {
            ClassifiedStatement.Assignment(original)
        }
    }
}

