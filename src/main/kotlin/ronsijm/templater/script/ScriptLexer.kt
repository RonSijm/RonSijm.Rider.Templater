package ronsijm.templater.script

import com.intellij.openapi.diagnostic.Logger

/**
 * Lexer for tokenizing and preprocessing script input
 * Handles comment removal and statement splitting
 */
class ScriptLexer {
    
    companion object {
        private val LOG: Logger? = try {
            Logger.getInstance(ScriptLexer::class.java)
        } catch (e: Throwable) {
            null // Logger not available in test environment
        }
    }
    
    /**
     * Preprocess script to handle multi-line constructs
     * Removes comments and strips await keyword
     */
    fun preprocessScript(script: String): String {
        var result = script

        // Remove multi-line comments /* ... */
        result = result.replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), "")

        // Remove single-line comments //
        result = result.lines()
            .map { line ->
                // Don't remove // inside strings
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

        // Strip await keyword
        result = result.replace(Regex("\\bawait\\s+"), "")

        return result
    }
    
    /**
     * Smart split that doesn't split on semicolons inside parentheses
     * This prevents breaking for loops like: for (let i = 1; i <= 3; i++)
     *
     * For object literals (const obj = { ... }), we track braces only when
     * we're in a variable declaration or assignment context, not for control
     * flow structures (if, for, while, try).
     *
     * Splits on:
     * - Semicolons (;) outside of parentheses and quotes
     * - Newlines (\n) outside of parentheses and quotes (unless in object literal context or method chain)
     *
     * Preserves:
     * - Content inside parentheses (for loop headers)
     * - Content inside braces when in object literal context (variable declarations/assignments)
     * - Content inside brackets (array literals)
     * - Content inside quotes (strings)
     * - Method chains across lines (lines starting with .)
     */
    fun smartSplitStatements(script: String): List<String> {
        val statements = mutableListOf<String>()
        val current = StringBuilder()
        var parenDepth = 0
        var braceDepth = 0
        var bracketDepth = 0
        var inQuotes = false
        var quoteChar = ' '
        var inObjectLiteralContext = false  // Track if we're in a variable declaration/assignment with object literal

        val lines = script.split('\n')

        for ((lineIndex, line) in lines.withIndex()) {
            val trimmedLine = line.trim()

            // Check if this line continues a method chain (starts with .)
            val isMethodChainContinuation = trimmedLine.startsWith(".")

            // Check if next line continues a method chain
            val nextLineIsMethodChain = lineIndex < lines.size - 1 &&
                lines[lineIndex + 1].trim().startsWith(".")

            // If we're inside quotes from a previous line, preserve the newline
            if (inQuotes && current.isNotEmpty()) {
                current.append('\n')
            }

            for (char in line) {
                when {
                    (char == '"' || char == '\'' || char == '`') && !inQuotes -> {
                        inQuotes = true
                        quoteChar = char
                        current.append(char)
                    }
                    char == quoteChar && inQuotes -> {
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
                        // Check if this is an object literal context (variable declaration or assignment)
                        // by looking at the current statement content
                        val currentContent = current.toString().trim()
                        val isObjectLiteral = isObjectLiteralContext(currentContent)
                        if (isObjectLiteral) {
                            inObjectLiteralContext = true
                        }
                        if (inObjectLiteralContext) {
                            braceDepth++
                        }
                        current.append(char)
                    }
                    char == '}' && !inQuotes -> {
                        if (inObjectLiteralContext) {
                            braceDepth--
                            if (braceDepth == 0) {
                                inObjectLiteralContext = false
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
                        // Split on semicolon
                        val stmt = current.toString().trim()
                        if (stmt.isNotEmpty()) {
                            statements.add(stmt)
                        }
                        current.clear()
                        inObjectLiteralContext = false
                    }
                    else -> {
                        current.append(char)
                    }
                }
            }

            // At end of line, decide whether to split or continue
            val shouldSplit = !inQuotes && parenDepth == 0 && braceDepth == 0 && bracketDepth == 0 &&
                !isMethodChainContinuation && !nextLineIsMethodChain

            if (shouldSplit) {
                val stmt = current.toString().trim()
                if (stmt.isNotEmpty()) {
                    statements.add(stmt)
                }
                current.clear()
                inObjectLiteralContext = false
            }
            // If not splitting, we continue accumulating (no newline added - statements are joined)
        }

        // Add remaining content
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

    /**
     * Determines if the current statement content indicates an object literal context.
     * Object literals appear in:
     * - Variable declarations: const/let/var x = {
     * - Assignments: x = {
     * - Function arguments: func({
     *
     * NOT object literals (control flow):
     * - if (condition) {
     * - for (init; cond; incr) {
     * - while (condition) {
     * - try {
     * - else {
     * - function name() {
     */
    private fun isObjectLiteralContext(content: String): Boolean {
        val trimmed = content.trim()

        // Control flow keywords - NOT object literals
        if (trimmed.startsWith("if ") || trimmed.startsWith("if(")) return false
        if (trimmed.startsWith("for ") || trimmed.startsWith("for(")) return false
        if (trimmed.startsWith("while ") || trimmed.startsWith("while(")) return false
        if (trimmed.startsWith("try")) return false
        if (trimmed.startsWith("else") || trimmed.contains("} else")) return false
        if (trimmed.startsWith("function ") || trimmed.contains("function(") || trimmed.contains("function (")) return false
        if (trimmed.startsWith("catch") || trimmed.startsWith("finally")) return false

        // Variable declarations with = are object literal contexts
        if (trimmed.contains("=")) {
            // Check if it's a variable declaration or assignment ending with =
            // e.g., "const callouts =" or "x ="
            val beforeEquals = trimmed.substringBefore("=").trim()
            if (beforeEquals.isNotEmpty()) {
                return true
            }
        }

        // Function call with object argument: func(
        // This is tricky - we need to be careful here
        // For now, if we're inside parentheses and see {, it could be an object argument
        // But this is handled by parenDepth, so we don't need special handling here

        return false
    }
}