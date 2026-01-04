package ronsijm.templater.common

object ModuleNames {
    const val DATE = "date"
    const val FILE = "file"
    const val SYSTEM = "system"
    const val WEB = "web"
    const val FRONTMATTER = "frontmatter"
    const val HOOKS = "hooks"
    const val CONFIG = "config"
    const val APP = "app"
    const val OBSIDIAN = "obsidian"

    val HANDLER_MODULES = setOf(DATE, FILE, SYSTEM, WEB)
    val SPECIAL_MODULES = setOf(FRONTMATTER, HOOKS, CONFIG, APP, OBSIDIAN)
    val ALL_MODULES = HANDLER_MODULES + SPECIAL_MODULES
}

object Prefixes {
    const val TP = "tp."
    const val AWAIT = "await "
    const val MATH = "Math."
    const val LET = "let "
    const val CONST = "const "
    const val VAR = "var "
    const val RETURN = "return "
    const val FUNCTION = "function "
    const val FOR = "for "
    const val WHILE = "while "
    const val IF = "if "
    const val ELSE = "else"
    const val TR = "tR"
}

object TemplateSyntax {
    const val BLOCK_START = "<%"
    const val BLOCK_END = "%>"
    const val EXECUTION_MARKER = "*"
    const val TRIM_NEWLINE = "-"
    const val TRIM_WHITESPACE = "_"

    val TEMPLATE_BLOCK_REGEX = Regex("""<%([_-])?(\*)?(.+?)([_-])?%>""", RegexOption.DOT_MATCHES_ALL)

    const val GROUP_LEFT_TRIM = 1
    const val GROUP_EXECUTION_MARKER = 2
    const val GROUP_CONTENT = 3
    const val GROUP_RIGHT_TRIM = 4
}

object Operators {
    const val PLUS_ASSIGN = "+="
    const val MINUS_ASSIGN = "-="
    const val MULTIPLY_ASSIGN = "*="
    const val DIVIDE_ASSIGN = "/="
    const val INCREMENT = "++"
    const val DECREMENT = "--"
    const val EQUALS = "=="
    const val NOT_EQUALS = "!="
    const val STRICT_EQUALS = "==="
    const val STRICT_NOT_EQUALS = "!=="
    const val LESS_THAN = "<"
    const val GREATER_THAN = ">"
    const val LESS_THAN_OR_EQUAL = "<="
    const val GREATER_THAN_OR_EQUAL = ">="
    const val LOGICAL_AND = "&&"
    const val LOGICAL_OR = "||"
    const val ARROW = "=>"
}

object Keywords {
    const val TRUE = "true"
    const val FALSE = "false"
    const val NULL = "null"
    const val UNDEFINED = "undefined"
    const val TYPEOF = "typeof"
    const val NEW = "new"
    const val THIS = "this"
}

object BuiltinObjects {
    const val MATH = "Math"
    const val DATE = "Date"
    const val ARRAY = "Array"
    const val OBJECT = "Object"
    const val STRING = "String"
    const val JSON = "JSON"
    const val CONSOLE = "console"
}

object FileExtensions {
    const val MARKDOWN = "md"
    const val TEMPLATE = "template"
}

object ErrorTemplates {
    const val UNKNOWN_MODULE = "Unknown module: %s"
    const val INVALID_COMMAND_FORMAT = "Invalid command format"
    const val COMMAND_FORMAT_SUGGESTION = "Commands should be in format: tp.module.function()"

    fun unknownModuleSuggestion(module: String): String? = when {
        module.startsWith("dat") -> "Did you mean '${Prefixes.TP}${ModuleNames.DATE}'?"
        module.startsWith("fil") -> "Did you mean '${Prefixes.TP}${ModuleNames.FILE}'?"
        module.startsWith("front") -> "Did you mean '${Prefixes.TP}${ModuleNames.FRONTMATTER}'?"
        module.startsWith("sys") -> "Did you mean '${Prefixes.TP}${ModuleNames.SYSTEM}'?"
        else -> null
    }
}

object ScriptEngineDefaults {

    const val STATEMENT_CACHE_SIZE = 512


    const val MAX_WHILE_ITERATIONS = 100_000
}

object CacheConfig {

    const val FUNCTION_CACHE_SIZE = 256


    const val CACHE_LOAD_FACTOR = 0.75f
}

object ParserConstants {

    const val NEW_DATE_PREFIX = "new Date("


    const val NEW_DATE_PREFIX_LENGTH = 9


    const val NEW_ARRAY_PREFIX = "new Array("


    const val NEW_ARRAY_PREFIX_LENGTH = 10
}

