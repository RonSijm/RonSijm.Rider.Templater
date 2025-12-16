package ronsijm.templater.script.evaluators

/**
 * Evaluates JavaScript template literals (backtick strings with ${} interpolation).
 * Single Responsibility: Only handles template literal evaluation.
 */
class TemplateLiteralEvaluator(
    private val expressionEvaluator: (String) -> Any?
) {

    /**
     * Evaluate a template literal like `Hello ${name}!`
     */
    fun evaluate(expression: String): String {
        // Remove the backticks
        val content = expression.substring(1, expression.length - 1)

        val result = StringBuilder()
        var i = 0

        while (i < content.length) {
            if (i < content.length - 1 && content[i] == '$' && content[i + 1] == '{') {
                // Find the matching closing brace
                val startExpr = i + 2
                var braceDepth = 1
                var endExpr = startExpr

                while (endExpr < content.length && braceDepth > 0) {
                    when (content[endExpr]) {
                        '{' -> braceDepth++
                        '}' -> braceDepth--
                    }
                    if (braceDepth > 0) endExpr++
                }

                // Extract and evaluate the expression
                val innerExpr = content.substring(startExpr, endExpr)
                val value = expressionEvaluator(innerExpr)
                result.append(value?.toString() ?: "")

                i = endExpr + 1
            } else {
                result.append(content[i])
                i++
            }
        }

        return result.toString()
    }
}

