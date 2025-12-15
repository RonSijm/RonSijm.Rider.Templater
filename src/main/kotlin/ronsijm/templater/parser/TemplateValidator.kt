package ronsijm.templater.parser

import com.intellij.openapi.diagnostic.Logger

/**
 * Validates template syntax and provides helpful error messages
 */
class TemplateValidator {
    
    companion object {
        private val LOG: Logger? = try {
            Logger.getInstance(TemplateValidator::class.java)
        } catch (e: Throwable) {
            null
        }
        
        // Common template patterns
        private val TEMPLATE_PATTERN = Regex("""<%([_-])?(\*)?(.+?)([_-])?%>""", RegexOption.DOT_MATCHES_ALL)
        private val UNCLOSED_TEMPLATE = Regex("""<%(?![^<]*%>)""")
        private val UNOPENED_TEMPLATE = Regex("""(?<!<%[^>]*)%>""")
    }
    
    /**
     * Validate template syntax and return list of validation errors
     */
    fun validate(content: String): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        
        // Check for unclosed template tags
        errors.addAll(checkUnclosedTags(content))
        
        // Check for unopened closing tags
        errors.addAll(checkUnopenedClosingTags(content))
        
        // Check for nested template tags
        errors.addAll(checkNestedTags(content))
        
        // Check for empty template commands
        errors.addAll(checkEmptyCommands(content))
        
        // Check for common syntax errors
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
                val lineNumber = content.substring(0, openIndex).count { it == '\n' } + 1
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
        val lines = content.lines()
        
        for ((index, line) in lines.withIndex()) {
            // Look for %> that doesn't have a corresponding <%
            var pos = 0
            while (pos < line.length) {
                val closeIndex = line.indexOf("%>", pos)
                if (closeIndex == -1) break
                
                // Check if there's a corresponding opening tag before this
                val openIndex = line.lastIndexOf("<%", closeIndex)
                if (openIndex == -1 || openIndex > closeIndex) {
                    errors.add(
                        ValidationError(
                            message = "Closing tag '%>' without opening tag '<%'",
                            lineNumber = index + 1,
                            suggestion = "Add '<%' before this closing tag or remove the '%>'"
                        )
                    )
                }
                
                pos = closeIndex + 2
            }
        }
        
        return errors
    }
    
    private fun checkNestedTags(content: String): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val matches = TEMPLATE_PATTERN.findAll(content).toList()
        
        for (i in matches.indices) {
            for (j in i + 1 until matches.size) {
                val outer = matches[i]
                val inner = matches[j]
                
                // Check if inner is completely contained within outer
                if (inner.range.first > outer.range.first && inner.range.last < outer.range.last) {
                    val lineNumber = content.substring(0, inner.range.first).count { it == '\n' } + 1
                    errors.add(
                        ValidationError(
                            message = "Nested template tags are not allowed",
                            lineNumber = lineNumber,
                            suggestion = "Close the outer template tag before starting a new one"
                        )
                    )
                }
            }
        }
        
        return errors
    }
    
    private fun checkEmptyCommands(content: String): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val emptyPattern = Regex("""<%([_-])?(\*)?[\s]*([_-])?%>""")

        emptyPattern.findAll(content).forEach { match ->
            val lineNumber = content.substring(0, match.range.first).count { it == '\n' } + 1
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

        TEMPLATE_PATTERN.findAll(content).forEach { match ->
            val command = match.groupValues[3].trim()
            val lineNumber = content.substring(0, match.range.first).count { it == '\n' } + 1

            // Check for common typos in module names
            when {
                command.startsWith("tp.dat.") -> {
                    errors.add(
                        ValidationError(
                            message = "Unknown module 'tp.dat'",
                            lineNumber = lineNumber,
                            suggestion = "Did you mean 'tp.date'?"
                        )
                    )
                }
                command.startsWith("tp.fil.") -> {
                    errors.add(
                        ValidationError(
                            message = "Unknown module 'tp.fil'",
                            lineNumber = lineNumber,
                            suggestion = "Did you mean 'tp.file'?"
                        )
                    )
                }
                command.startsWith("tp.front.") -> {
                    errors.add(
                        ValidationError(
                            message = "Unknown module 'tp.front'",
                            lineNumber = lineNumber,
                            suggestion = "Did you mean 'tp.frontmatter'?"
                        )
                    )
                }
                command.startsWith("tp.sys.") -> {
                    errors.add(
                        ValidationError(
                            message = "Unknown module 'tp.sys'",
                            lineNumber = lineNumber,
                            suggestion = "Did you mean 'tp.system'?"
                        )
                    )
                }
                // Check for missing parentheses on function calls
                command.matches(Regex("""tp\.\w+\.\w+$""")) && !command.contains("(") -> {
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