package ronsijm.templater.debug


class BreakpointResolutionException(
    val lineNumber: Int,
    val reason: String,
    val sourceLineContent: String? = null,
    val nearestNodeLine: Int? = null,
    val nearestNodeCode: String? = null
) : Exception(buildMessage(lineNumber, reason, sourceLineContent, nearestNodeLine, nearestNodeCode)) {

    companion object {
        private fun buildMessage(
            lineNumber: Int,
            reason: String,
            sourceLineContent: String?,
            nearestNodeLine: Int?,
            nearestNodeCode: String?
        ): String {
            val parts = mutableListOf<String>()
            parts.add("Failed to resolve breakpoint at line $lineNumber: $reason")

            if (sourceLineContent != null) {
                parts.add("\n  Source line content: '$sourceLineContent'")
            }

            if (nearestNodeLine != null && nearestNodeCode != null) {
                parts.add("\n  Nearest AST node at line $nearestNodeLine: '${nearestNodeCode.take(60)}'")
            }

            return parts.joinToString("")
        }


        fun noExecutableStatement(
            lineNumber: Int,
            sourceLineContent: String?,
            nearestNode: ronsijm.templater.ast.StatementNode?
        ): BreakpointResolutionException {
            return BreakpointResolutionException(
                lineNumber = lineNumber,
                reason = "No executable statement found at or after this line",
                sourceLineContent = sourceLineContent,
                nearestNodeLine = nearestNode?.lineNumber,
                nearestNodeCode = nearestNode?.code
            )
        }


        fun codeMismatch(
            lineNumber: Int,
            sourceLineContent: String,
            nodeCode: String,
            nodeLineNumber: Int
        ): BreakpointResolutionException {
            return BreakpointResolutionException(
                lineNumber = lineNumber,
                reason = "AST node code does not match source line content",
                sourceLineContent = sourceLineContent,
                nearestNodeLine = nodeLineNumber,
                nearestNodeCode = nodeCode
            )
        }


        fun noDebugSession(lineNumber: Int): BreakpointResolutionException {
            return BreakpointResolutionException(
                lineNumber = lineNumber,
                reason = "No debug session available to add breakpoint"
            )
        }


        fun noAST(lineNumber: Int): BreakpointResolutionException {
            return BreakpointResolutionException(
                lineNumber = lineNumber,
                reason = "No AST available - template has not been parsed yet"
            )
        }
    }
}

