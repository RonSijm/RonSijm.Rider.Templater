package ronsijm.templater.script.parser


object BraceMatchingUtils {


    fun findMatchingBrace(str: String, openBraceIndex: Int): Int {
        if (openBraceIndex < 0 || openBraceIndex >= str.length || str[openBraceIndex] != '{') {
            return -1
        }

        var depth = 1
        var inString = false
        var stringChar = ' '

        for (i in (openBraceIndex + 1) until str.length) {
            val char = str[i]


            if (!inString && (char == '"' || char == '\'' || char == '`')) {
                if (!isQuoteEscaped(str, i)) {
                    inString = true
                    stringChar = char
                }
            } else if (inString && char == stringChar) {
                if (!isQuoteEscaped(str, i)) {
                    inString = false
                }
            } else if (!inString) {
                when (char) {
                    '{' -> depth++
                    '}' -> {
                        depth--
                        if (depth == 0) {
                            return i
                        }
                    }
                }
            }
        }

        return -1
    }


    fun findMatchingParen(str: String, openParenIndex: Int): Int {
        if (openParenIndex < 0 || openParenIndex >= str.length || str[openParenIndex] != '(') {
            return -1
        }

        var depth = 1
        var inString = false
        var stringChar = ' '

        for (i in (openParenIndex + 1) until str.length) {
            val char = str[i]

            if (!inString && (char == '"' || char == '\'' || char == '`')) {
                if (!isQuoteEscaped(str, i)) {
                    inString = true
                    stringChar = char
                }
            } else if (inString && char == stringChar) {
                if (!isQuoteEscaped(str, i)) {
                    inString = false
                }
            } else if (!inString) {
                when (char) {
                    '(' -> depth++
                    ')' -> {
                        depth--
                        if (depth == 0) {
                            return i
                        }
                    }
                }
            }
        }

        return -1
    }


    fun isQuoteEscaped(code: String, i: Int): Boolean {
        if (i == 0) return false
        var backslashCount = 0
        var j = i - 1
        while (j >= 0 && code[j] == '\\') {
            backslashCount++
            j--
        }
        return backslashCount % 2 == 1
    }
}
