package ronsijm.templater.utils


object TextUtils {

    fun calculateLineNumber(text: String, position: Int): Int {
        if (position < 0 || position > text.length) {
            throw IllegalArgumentException("Position $position out of bounds for text of length ${text.length}")
        }
        return text.substring(0, position).count { it == '\n' } + 1
    }


    fun calculateLineRange(text: String, startPos: Int, endPos: Int): IntRange {
        val startLine = calculateLineNumber(text, startPos)
        val endLine = calculateLineNumber(text, endPos)
        return startLine..endLine
    }


    fun isQuoteEscaped(str: String, pos: Int): Boolean {
        if (pos == 0) return false
        var backslashCount = 0
        var i = pos - 1
        while (i >= 0 && str[i] == '\\') {
            backslashCount++
            i--
        }
        return backslashCount % 2 == 1
    }


    fun removeHtmlComments(text: String): String {
        val result = StringBuilder(text)
        var pos = 0

        while (pos < result.length) {

            val commentStart = result.indexOf("<!--", pos)
            if (commentStart == -1) break


            val commentEnd = result.indexOf("-->", commentStart + 4)
            if (commentEnd == -1) {

                for (i in commentStart until result.length) {
                    if (result[i] == '\n') continue
                    result[i] = ' '
                }
                break
            }


            for (i in commentStart until (commentEnd + 3)) {
                if (result[i] == '\n') continue
                result[i] = ' '
            }

            pos = commentEnd + 3
        }

        return result.toString()
    }
}

