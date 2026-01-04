package ronsijm.templater.modules

import ronsijm.templater.common.FrontmatterAccess
import ronsijm.templater.common.ModuleNames
import ronsijm.templater.parser.TemplateContext


class FrontmatterModule(private val context: TemplateContext) : FrontmatterAccess {


    override fun getValue(parts: List<String>): Any? {
        if (parts.isEmpty() || parts[0] != ModuleNames.FRONTMATTER) {
            return null
        }


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


    override fun getAll(): Map<String, Any> {
        return context.frontmatter
    }
}
