package ronsijm.templater.parser

import org.yaml.snakeyaml.Yaml


class FrontmatterParser {

    data class ParseResult(
        val frontmatter: Map<String, Any>,
        val content: String,
        val hasFrontmatter: Boolean,
        val frontmatterRaw: String = ""
    )


    fun parse(fileContent: String): ParseResult {
        val trimmed = fileContent.trimStart()

        if (!trimmed.startsWith("---")) {
            return ParseResult(
                frontmatter = emptyMap(),
                content = fileContent,
                hasFrontmatter = false
            )
        }

        val lines = trimmed.lines()
        var closingIndex = -1

        for (i in 1 until lines.size) {
            if (lines[i].trim() == "---") {
                closingIndex = i
                break
            }
        }

        if (closingIndex == -1) {
            return ParseResult(
                frontmatter = emptyMap(),
                content = fileContent,
                hasFrontmatter = false
            )
        }

        val frontmatterYaml = lines.subList(1, closingIndex).joinToString("\n")
        val contentLines = lines.subList(closingIndex + 1, lines.size)
        val content = contentLines.joinToString("\n")

        val yaml = Yaml()
        val frontmatter = try {
            val parsed = yaml.load<Any>(frontmatterYaml)
            when (parsed) {
                is Map<*, *> -> parsed.mapKeys { it.key.toString() }.mapValues { it.value ?: "" }
                else -> emptyMap()
            }
        } catch (e: Exception) {
            emptyMap<String, Any>()
        }

        return ParseResult(
            frontmatter = frontmatter,
            content = content,
            hasFrontmatter = true,
            frontmatterRaw = frontmatterYaml
        )
    }


    fun getNestedValue(frontmatter: Map<String, Any>, path: String): Any? {
        val parts = path.split(".")
        var current: Any? = frontmatter

        for (part in parts) {
            current = when (current) {
                is Map<*, *> -> current[part]
                else -> return null
            }
        }

        return current
    }
}
