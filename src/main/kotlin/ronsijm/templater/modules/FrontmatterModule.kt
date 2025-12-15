package ronsijm.templater.modules

import ronsijm.templater.parser.TemplateContext

/**
 * Frontmatter module for Templater
 * Provides tp.frontmatter.* access
 */
class FrontmatterModule(private val context: TemplateContext) {

    /**
     * Get a frontmatter value by path
     * @param parts The path parts (e.g., ["frontmatter", "key"] or ["frontmatter", "nested", "key"])
     * @return The value or null
     */
    fun getValue(parts: List<String>): Any? {
        if (parts.isEmpty() || parts[0] != "frontmatter") {
            return null
        }

        // Navigate through nested structure
        var current: Any? = context.frontmatter
        for (i in 1 until parts.size) {
            val part = parts[i]
            current = when (current) {
                is Map<*, *> -> current[part]
                else -> return null
            }
        }

        return current
    }

    /**
     * Get all frontmatter as a map
     */
    fun getAll(): Map<String, Any> {
        return context.frontmatter
    }
}

