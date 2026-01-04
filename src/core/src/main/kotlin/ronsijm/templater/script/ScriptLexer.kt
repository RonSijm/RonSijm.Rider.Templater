package ronsijm.templater.script

import ronsijm.templater.utils.Logging

class ScriptLexer {

    companion object {
        private val LOG = Logging.getLogger<ScriptLexer>()

        private val MULTI_LINE_COMMENT_REGEX = Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL)
        private val AWAIT_KEYWORD_REGEX = Regex("\\bawait\\s+")
    }

    private fun isQuoteEscaped(str: String, i: Int): Boolean {
        if (i == 0) return false
        var backslashCount = 0
        var j = i - 1
        while (j >= 0 && str[j] == '\\') {
            backslashCount++
            j--
        }
        return backslashCount % 2 == 1
    }

    fun preprocessScript(script: String): String {
        var result = script

        result = result.replace(MULTI_LINE_COMMENT_REGEX, "")

        result = result.lines()
            .map { line ->
                var inString = false
                var stringChar = ' '
                var commentIndex = -1

                for (i in line.indices) {
                    val char = line[i]
                    when {
                        (char == '"' || char == '\'' || char == '`') && !inString -> {
                            inString = true
                            stringChar = char
                        }
                        char == stringChar && inString && (i == 0 || line[i-1] != '\\') -> {
                            inString = false
                        }
                        char == '/' && i < line.length - 1 && line[i+1] == '/' && !inString -> {
                            commentIndex = i
                            break
                        }
                    }
                }

                if (commentIndex >= 0) {
                    line.substring(0, commentIndex)
                } else {
                    line
                }
            }
            .joinToString("\n")

        result = result.replace(AWAIT_KEYWORD_REGEX, "")

        return result
    }

    fun smartSplitStatements(script: String): List<String> {
        val statements = mutableListOf<String>()
        val current = StringBuilder()
        var parenDepth = 0
        var braceDepth = 0
        var bracketDepth = 0
        var inQuotes = false
        var quoteChar = ' '
        var inBlockContext = false

        val lines = script.split('\n')

        for ((lineIndex, line) in lines.withIndex()) {
            val trimmedLine = line.trim()

            val isMethodChainContinuation = trimmedLine.startsWith(".")

            val nextLineIsMethodChain = lineIndex < lines.size - 1 &&
                lines[lineIndex + 1].trim().startsWith(".")

            if (inQuotes && current.isNotEmpty()) {
                current.append('\n')
            }

            for (charIndex in line.indices) {
                val char = line[charIndex]
                val fullStringSoFar = current.toString() + line.substring(0, charIndex + 1)
                val posInFull = fullStringSoFar.length - 1

                when {
                    (char == '"' || char == '\'' || char == '`') && !inQuotes && !isQuoteEscaped(fullStringSoFar, posInFull) -> {
                        inQuotes = true
                        quoteChar = char
                        current.append(char)
                    }
                    char == quoteChar && inQuotes && !isQuoteEscaped(fullStringSoFar, posInFull) -> {
                        inQuotes = false
                        current.append(char)
                    }
                    char == '(' && !inQuotes -> {
                        parenDepth++
                        current.append(char)
                    }
                    char == ')' && !inQuotes -> {
                        parenDepth--
                        current.append(char)
                    }
                    char == '{' && !inQuotes -> {
                        val currentContent = current.toString().trim()
                        val isObjectLiteral = isObjectLiteralContext(currentContent)
                        val isFunctionDeclaration = currentContent.startsWith("function ")
                        val isControlStructure = currentContent.startsWith("for ") ||
                            currentContent.startsWith("for(") ||
                            currentContent.startsWith("while ") ||
                            currentContent.startsWith("while(") ||
                            currentContent.startsWith("if ") ||
                            currentContent.startsWith("if(") ||
                            currentContent.startsWith("else ") ||
                            currentContent == "else"
                        if (isObjectLiteral || isFunctionDeclaration || isControlStructure) {
                            inBlockContext = true
                        }
                        if (inBlockContext) {
                            braceDepth++
                        }
                        current.append(char)
                    }
                    char == '}' && !inQuotes -> {
                        if (inBlockContext) {
                            braceDepth--
                            if (braceDepth == 0) {
                                inBlockContext = false
                            }
                        }
                        current.append(char)
                    }
                    char == '[' && !inQuotes -> {
                        bracketDepth++
                        current.append(char)
                    }
                    char == ']' && !inQuotes -> {
                        bracketDepth--
                        current.append(char)
                    }
                    char == ';' && !inQuotes && parenDepth == 0 && braceDepth == 0 && bracketDepth == 0 -> {
                        val stmt = current.toString().trim()
                        if (stmt.isNotEmpty()) {
                            statements.add(stmt)
                        }
                        current.clear()
                        inBlockContext = false
                    }
                    else -> {
                        current.append(char)
                    }
                }
            }

            val currentContent = current.toString().trim()
            val endsWithArrow = currentContent.endsWith("=>")

            val shouldSplit = !inQuotes && parenDepth == 0 && braceDepth == 0 && bracketDepth == 0 &&
                !isMethodChainContinuation && !nextLineIsMethodChain && !endsWithArrow

            if (shouldSplit) {
                val stmt = current.toString().trim()
                if (stmt.isNotEmpty()) {
                    statements.add(stmt)
                }
                current.clear()
                inBlockContext = false
            } else if (inBlockContext && braceDepth > 0) {
                current.append('\n')
            }
        }

        val stmt = current.toString().trim()
        if (stmt.isNotEmpty()) {
            statements.add(stmt)
        }

        if (LOG?.isDebugEnabled == true) {
            LOG.debug("Split into ${statements.size} statements:")
            statements.forEachIndexed { idx, s -> LOG.debug("  [$idx] $s") }
        }

        return statements
    }

    private fun isObjectLiteralContext(content: String): Boolean {
        val trimmed = content.trim()

        if (trimmed.startsWith("if ") || trimmed.startsWith("if(")) return false
        if (trimmed.startsWith("for ") || trimmed.startsWith("for(")) return false
        if (trimmed.startsWith("while ") || trimmed.startsWith("while(")) return false
        if (trimmed.startsWith("try")) return false
        if (trimmed.startsWith("else") || trimmed.contains("} else")) return false
        if (trimmed.startsWith("function ") || trimmed.contains("function(") || trimmed.contains("function (")) return false
        if (trimmed.startsWith("catch") || trimmed.startsWith("finally")) return false

        if (trimmed.contains("=")) {
            val beforeEquals = trimmed.substringBefore("=").trim()
            if (beforeEquals.isNotEmpty()) {
                return true
            }
        }

        return false
    }
}
