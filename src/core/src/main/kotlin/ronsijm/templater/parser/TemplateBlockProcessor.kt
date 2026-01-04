package ronsijm.templater.parser

import ronsijm.templater.common.TemplateSyntax
import ronsijm.templater.parallel.TemplateBlock
import ronsijm.templater.utils.Logging


object TemplateBlockProcessor {
    private val LOG = Logging.getLogger<TemplateBlockProcessor>()
    private val templateRegex = TemplateSyntax.TEMPLATE_BLOCK_REGEX


    fun extractBlocks(content: String): List<TemplateBlock> {
        return templateRegex.findAll(content).mapIndexed { index, match ->
            TemplateBlock(
                id = index,
                matchText = match.value,
                command = match.groupValues[3].trim(),
                isExecution = match.groupValues[2] == "*",
                leftTrim = match.groupValues[1],
                rightTrim = match.groupValues[4],
                originalStart = match.range.first,
                originalEnd = match.range.last + 1
            )
        }.toList()
    }


    fun applyResults(
        content: String,
        blocks: List<TemplateBlock>,
        results: Map<Int, String>
    ): String {
        var result = content


        for (block in blocks.sortedByDescending { it.originalStart }) {
            val replacement = results[block.id] ?: ""


            var trimStart = block.originalStart
            var trimEnd = block.originalEnd


            trimStart = applyLeftTrim(result, trimStart, block.leftTrim)


            trimEnd = applyRightTrim(result, trimEnd, block.rightTrim)

            result = result.substring(0, trimStart) + replacement + result.substring(trimEnd)
        }

        return result
    }


    fun applyLeftTrim(content: String, start: Int, trimMarker: String): Int {
        var trimStart = start
        when (trimMarker) {
            "_" -> {

                while (trimStart > 0 && content[trimStart - 1].isWhitespace()) {
                    trimStart--
                }
            }
            "-" -> {

                if (trimStart > 0 && content[trimStart - 1] == '\n') {
                    trimStart--

                    if (trimStart > 0 && content[trimStart - 1] == '\r') {
                        trimStart--
                    }
                }
            }
        }
        return trimStart
    }


    fun applyRightTrim(content: String, end: Int, trimMarker: String): Int {
        var trimEnd = end
        when (trimMarker) {
            "_" -> {

                while (trimEnd < content.length && content[trimEnd].isWhitespace()) {
                    trimEnd++
                }
            }
            "-" -> {

                if (trimEnd < content.length && content[trimEnd] == '\r') {
                    trimEnd++
                }
                if (trimEnd < content.length && content[trimEnd] == '\n') {
                    trimEnd++
                }
            }
        }
        return trimEnd
    }


    fun validateAndLog(content: String, validator: TemplateValidator): Boolean {
        val validationErrors = validator.validate(content)
        if (validationErrors.isNotEmpty()) {
            LOG?.warn("Template validation failed:\n${validationErrors.joinToString("\n")}")
            return false
        }
        return true
    }
}

