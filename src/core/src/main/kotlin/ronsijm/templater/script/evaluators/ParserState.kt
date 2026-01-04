package ronsijm.templater.script.evaluators


class ParserState {
    private var inQuotes = false
    private var quoteChar = ' '
    private var parenDepth = 0
    private var bracketDepth = 0
    private var braceDepth = 0
    private var backslashCount = 0


    fun updateForChar(char: Char) {

        if (char == '\\') {
            backslashCount++
        } else {

            if (char == '"' || char == '\'' || char == '`') {
                val isEscaped = backslashCount % 2 == 1
                if (!isEscaped) {
                    if (!inQuotes) {
                        inQuotes = true
                        quoteChar = char
                    } else if (char == quoteChar) {
                        inQuotes = false
                    }
                }
            }


            backslashCount = 0


            if (!inQuotes) {
                when (char) {
                    '(' -> parenDepth++
                    ')' -> parenDepth--
                    '[' -> bracketDepth++
                    ']' -> bracketDepth--
                    '{' -> braceDepth++
                    '}' -> braceDepth--
                }
            }
        }
    }


    fun isAtTopLevel(): Boolean {
        return !inQuotes && parenDepth == 0 && bracketDepth == 0 && braceDepth == 0
    }


    fun isNotInQuotes(): Boolean = !inQuotes


    fun isAtZeroParenDepth(): Boolean = parenDepth == 0


    fun reset() {
        inQuotes = false
        quoteChar = ' '
        parenDepth = 0
        bracketDepth = 0
        braceDepth = 0
        backslashCount = 0
    }
}
