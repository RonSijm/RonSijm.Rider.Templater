package ronsijm.templater.utils

/**
 * Centralized constants for template syntax
 * Makes it easy to change template syntax in one place
 */
object TemplateConstants {
    // Template tags
    const val TAG_OPEN = "<%"
    const val TAG_CLOSE = "%>"
    const val TAG_EXECUTION = "*"
    
    // Whitespace control
    const val TRIM_LEFT = "-"
    const val TRIM_RIGHT = "-"
    const val STRIP_LEFT = "_"
    const val STRIP_RIGHT = "_"
    
    // Module prefix
    const val MODULE_PREFIX = "tp."
    
    // Regex patterns (as strings for documentation)
    const val TEMPLATE_PATTERN_STRING = """<%([_-])?(\*)?(.+?)([_-])?%>"""
}

