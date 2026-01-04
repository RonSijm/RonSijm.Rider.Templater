package ronsijm.templater.script

import ronsijm.templater.script.parser.BraceMatchingUtils
import ronsijm.templater.script.parser.ForLoopParser
import ronsijm.templater.script.parser.FunctionParser
import ronsijm.templater.script.parser.IfStatementParser
import ronsijm.templater.script.parser.TryCatchParser
import ronsijm.templater.script.parser.WhileLoopParser
import ronsijm.templater.utils.Quadruple

class ScriptParser {

    companion object {

        private val VAR_DECL_REGEX = Regex("^(let|const|var)\\s+")
    }

    fun extractForLoop(statements: List<String>, startIndex: Int): Triple<String, List<String>, Int>? {
        return ForLoopParser.extractForLoop(statements, startIndex, this)
    }

    fun extractWhileLoop(statements: List<String>, startIndex: Int): Triple<String, List<String>, Int>? {
        return WhileLoopParser.extractWhileLoop(statements, startIndex, this)
    }

    fun parseForLoopHeader(loopHeader: String): ForLoopParser.ForLoopInfo? {
        return ForLoopParser.parseForLoopHeader(loopHeader)
    }

    fun parseForOfLoopHeader(loopHeader: String): ForLoopParser.ForOfLoopInfo? {
        return ForLoopParser.parseForOfLoopHeader(loopHeader)
    }

    fun isForOfLoop(loopHeader: String): Boolean {
        return ForLoopParser.isForOfLoop(loopHeader)
    }

    fun checkCondition(current: Int, operator: String, target: Int) = when (operator) {
        "<=" -> current <= target
        ">=" -> current >= target
        "<" -> current < target
        ">" -> current > target
        else -> false
    }

    fun extractIfStatement(statements: List<String>, startIndex: Int): Quadruple<String, List<String>, List<Pair<String?, List<String>>>, Int>? {
        return IfStatementParser.extractIfStatement(statements, startIndex, this)
    }

    fun extractTryCatch(statements: List<String>, startIndex: Int): Pair<TryCatchParser.TryCatchInfo, Int>? {
        return TryCatchParser.extractTryCatch(statements, startIndex)
    }

    fun extractFunction(statements: List<String>, startIndex: Int): Pair<FunctionParser.FunctionInfo, Int>? {
        return FunctionParser.extractFunction(statements, startIndex, this)
    }

    fun splitStatements(code: String): List<String> {
        val statements = mutableListOf<String>()
        val current = StringBuilder()
        var braceDepth = 0
        var parenDepth = 0
        var inString = false
        var stringChar = ' '

        var i = 0
        while (i < code.length) {
            val char = code[i]


            if ((char == '"' || char == '\'' || char == '`') && !BraceMatchingUtils.isQuoteEscaped(code, i)) {
                if (!inString) {
                    inString = true
                    stringChar = char
                } else if (char == stringChar) {
                    inString = false
                }
            }


            if (!inString) {
                when (char) {
                    '{' -> braceDepth++
                    '}' -> braceDepth--
                    '(' -> parenDepth++
                    ')' -> parenDepth--
                }
            }


            if (!inString && braceDepth == 0 && parenDepth == 0 && (char == ';' || char == '\n')) {


                val remaining = code.substring(i + 1)
                val nextNonWhitespace = remaining.trimStart()
                if (nextNonWhitespace.startsWith("else")) {

                    if (char != '\n') {
                        current.append(char)
                    } else {
                        current.append(' ')
                    }
                } else {
                    val stmt = current.toString().trim()
                    if (stmt.isNotEmpty()) {
                        statements.add(stmt)
                    }
                    current.clear()
                }
            } else {
                current.append(char)
            }
            i++
        }


        val remaining = current.toString().trim()
        if (remaining.isNotEmpty()) {
            statements.add(remaining)
        }

        return statements
    }

}