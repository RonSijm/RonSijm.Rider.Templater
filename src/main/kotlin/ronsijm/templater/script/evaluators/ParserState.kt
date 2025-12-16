package ronsijm.templater.script.evaluators

/**
 * Tracks parser state while scanning through expressions.
 * Handles quote tracking and bracket depth for proper parsing.
 */
class ParserState {
    private var inQuotes = false
    private var quoteChar = ' '
    private var parenDepth = 0
    private var bracketDepth = 0
    private var braceDepth = 0

    /**
     * Update state based on the current character.
     */
    fun updateForChar(char: Char) {
        when {
            (char == '"' || char == '\'' || char == '`') && !inQuotes -> {
                inQuotes = true
                quoteChar = char
            }
            char == quoteChar && inQuotes -> {
                inQuotes = false
            }
            char == '(' && !inQuotes -> parenDepth++
            char == ')' && !inQuotes -> parenDepth--
            char == '[' && !inQuotes -> bracketDepth++
            char == ']' && !inQuotes -> bracketDepth--
            char == '{' && !inQuotes -> braceDepth++
            char == '}' && !inQuotes -> braceDepth--
        }
    }

    /**
     * Check if we're at the top level (not inside quotes or any brackets).
     */
    fun isAtTopLevel(): Boolean {
        return !inQuotes && parenDepth == 0 && bracketDepth == 0 && braceDepth == 0
    }

    /**
     * Check if we're not inside quotes (but may be inside brackets).
     */
    fun isNotInQuotes(): Boolean = !inQuotes

    /**
     * Check if we're at zero parenthesis depth.
     */
    fun isAtZeroParenDepth(): Boolean = parenDepth == 0

    /**
     * Reset state for reuse.
     */
    fun reset() {
        inQuotes = false
        quoteChar = ' '
        parenDepth = 0
        bracketDepth = 0
        braceDepth = 0
    }
}

