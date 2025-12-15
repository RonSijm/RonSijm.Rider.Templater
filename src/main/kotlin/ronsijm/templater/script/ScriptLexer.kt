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
     * Splits on:
     * - Semicolons (;) outside of parentheses and quotes
     * - Newlines (\n) outside of parentheses and quotes
     * 
     * Preserves:
     * - Content inside parentheses (for loop headers)
     * - Content inside quotes (strings)
     */
    fun smartSplitStatements(script: String): List<String> {
        val statements = mutableListOf<String>()
        val current = StringBuilder()
        var parenDepth = 0
        var inQuotes = false
        var quoteChar = ' '
        
        for (char in script) {
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
                (char == ';' || char == '\n') && !inQuotes && parenDepth == 0 -> {
                    // Split here
                    val stmt = current.toString().trim()
                    if (stmt.isNotEmpty()) {
                        statements.add(stmt)
                    }
                    current.clear()
                }
                else -> {
                    current.append(char)
                }
            }
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
}