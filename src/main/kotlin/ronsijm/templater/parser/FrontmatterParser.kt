package ronsijm.templater.parser

import org.yaml.snakeyaml.Yaml

/**
 * Parses YAML frontmatter from markdown files
 */
class FrontmatterParser {
    
    data class ParseResult(
        val frontmatter: Map<String, Any>,
        val content: String,
        val hasFrontmatter: Boolean
    )
    
    /**
     * Parse frontmatter from file content
     * Frontmatter is expected to be at the start of the file, delimited by ---
     * 
     * Example:
     * ---
     * title: My Document
     * author: John Doe
     * ---
     * Content here...
     */
    fun parse(fileContent: String): ParseResult {
        val trimmed = fileContent.trimStart()
        
        // Check if file starts with frontmatter delimiter
        if (!trimmed.startsWith("---")) {
            return ParseResult(
                frontmatter = emptyMap(),
                content = fileContent,
                hasFrontmatter = false
            )
        }
        
        // Find the closing delimiter
        val lines = trimmed.lines()
        var closingIndex = -1
        
        for (i in 1 until lines.size) {
            if (lines[i].trim() == "---") {
                closingIndex = i
                break
            }
        }
        
        if (closingIndex == -1) {
            // No closing delimiter found
            return ParseResult(
                frontmatter = emptyMap(),
                content = fileContent,
                hasFrontmatter = false
            )
        }
        
        // Extract frontmatter YAML
        val frontmatterYaml = lines.subList(1, closingIndex).joinToString("\n")
        
        // Extract content after frontmatter
        val contentLines = lines.subList(closingIndex + 1, lines.size)
        val content = contentLines.joinToString("\n")
        
        // Parse YAML
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
            hasFrontmatter = true
        )
    }
    
    /**
     * Get a nested value from frontmatter using dot notation
     * Example: "author.name" -> frontmatter["author"]["name"]
     */
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