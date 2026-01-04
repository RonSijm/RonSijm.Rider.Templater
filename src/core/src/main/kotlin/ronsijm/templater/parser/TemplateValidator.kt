package ronsijm.templater.parser

import ronsijm.templater.common.ModuleNames
import ronsijm.templater.common.Prefixes
import ronsijm.templater.common.TemplateSyntax
import ronsijm.templater.utils.Logging
import ronsijm.templater.utils.TextUtils


class TemplateValidator {

    companion object {
        private val LOG = Logging.getLogger<TemplateValidator>()

        private val TEMPLATE_PATTERN = TemplateSyntax.TEMPLATE_BLOCK_REGEX
        private val UNCLOSED_TEMPLATE = Regex("""<%(?![^<]*%>)""")
        private val UNOPENED_TEMPLATE = Regex("""(?<!<%[^>]*)%>""")

        private val EMPTY_COMMAND_PATTERN = Regex("""<%([_-])?(\*)?[\s]*([_-])?%>""")
        private val MISSING_PARENS_PATTERN = Regex("""tp\.\w+\.\w+$""")
    }


    fun validate(content: String): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()


        errors.addAll(checkUnclosedTags(content))


        errors.addAll(checkUnopenedClosingTags(content))


        errors.addAll(checkNestedTags(content))


        errors.addAll(checkEmptyCommands(content))


        errors.addAll(checkCommonSyntaxErrors(content))

        return errors
    }

    private fun checkUnclosedTags(content: String): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        var position = 0

        while (position < content.length) {
            val openIndex = content.indexOf("<%", position)
            if (openIndex == -1) break

            val closeIndex = content.indexOf("%>", openIndex)
            if (closeIndex == -1) {
                val lineNumber = TextUtils.calculateLineNumber(content, openIndex)
                errors.add(
                    ValidationError(
                        message = "Unclosed template tag",
                        lineNumber = lineNumber,
                        suggestion = "Add '%>' to close the template tag"
                    )
                )
                break
            }

            position = closeIndex + 2
        }

        return errors
    }

    private fun checkUnopenedClosingTags(content: String): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()


        var position = 0
        var openCount = 0

        while (position < content.length) {
            val nextOpen = content.indexOf("<%", position)
            val nextClose = content.indexOf("%>", position)

            when {
                nextOpen == -1 && nextClose == -1 -> break
                nextClose == -1 -> {

                    break
                }
                nextOpen == -1 || nextClose < nextOpen -> {

                    if (openCount == 0) {
                        val lineNumber = TextUtils.calculateLineNumber(content, nextClose)
                        errors.add(
                            ValidationError(
                                message = "Closing tag '%>' without opening tag '<%'",
                                lineNumber = lineNumber,
                                suggestion = "Add '<%' before this closing tag or remove the '%>'"
                            )
                        )
                    } else {
                        openCount--
                    }
                    position = nextClose + 2
                }
                else -> {

                    openCount++
                    position = nextOpen + 2
                }
            }
        }

        return errors
    }


    private fun checkNestedTags(content: String): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()


        var i = 0
        var depth = 0
        var lastOpenIndex = -1

        while (i < content.length - 1) {
            if (content[i] == '<' && content[i + 1] == '%') {
                if (depth > 0) {

                    val lineNumber = TextUtils.calculateLineNumber(content, i)
                    errors.add(
                        ValidationError(
                            message = "Nested template tags are not allowed",
                            lineNumber = lineNumber,
                            suggestion = "Close the outer template tag before starting a new one"
                        )
                    )
                }
                depth++
                lastOpenIndex = i
                i += 2
            } else if (content[i] == '%' && content[i + 1] == '>') {
                if (depth > 0) {
                    depth--
                }
                i += 2
            } else {
                i++
            }
        }

        return errors
    }

    private fun checkEmptyCommands(content: String): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        EMPTY_COMMAND_PATTERN.findAll(content).forEach { match ->
            val lineNumber = TextUtils.calculateLineNumber(content, match.range.first)
            errors.add(
                ValidationError(
                    message = "Empty template command",
                    lineNumber = lineNumber,
                    suggestion = "Add a command between <% and %>, or remove the empty tag"
                )
            )
        }

        return errors
    }

    private fun checkCommonSyntaxErrors(content: String): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()


        val typoCorrections = mapOf(
            "${Prefixes.TP}dat." to "${Prefixes.TP}${ModuleNames.DATE}",
            "${Prefixes.TP}fil." to "${Prefixes.TP}${ModuleNames.FILE}",
            "${Prefixes.TP}front." to "${Prefixes.TP}${ModuleNames.FRONTMATTER}",
            "${Prefixes.TP}sys." to "${Prefixes.TP}${ModuleNames.SYSTEM}"
        )

        TEMPLATE_PATTERN.findAll(content).forEach { match ->
            val command = match.groupValues[3].trim()
            val lineNumber = TextUtils.calculateLineNumber(content, match.range.first)


            val typoFound = typoCorrections.entries.find { (typo, _) -> command.startsWith(typo) }
            when {
                typoFound != null -> {
                    val typoModule = typoFound.key.removeSuffix(".")
                    errors.add(
                        ValidationError(
                            message = "Unknown module '$typoModule'",
                            lineNumber = lineNumber,
                            suggestion = "Did you mean '${typoFound.value}'?"
                        )
                    )
                }

                command.matches(MISSING_PARENS_PATTERN) && !command.contains("(") -> {
                    errors.add(
                        ValidationError(
                            message = "Function call missing parentheses",
                            lineNumber = lineNumber,
                            suggestion = "Add () after the function name: $command()"
                        )
                    )
                }
            }
        }

        return errors
    }
}
