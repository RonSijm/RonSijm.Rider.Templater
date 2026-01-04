package ronsijm.templater.common




@JvmInline
value class ModuleName(val value: String) {
    init {
        require(value.isNotBlank()) { "Module name cannot be blank" }
    }

    override fun toString(): String = value

    companion object {
        val DATE = ModuleName(ModuleNames.DATE)
        val FILE = ModuleName(ModuleNames.FILE)
        val SYSTEM = ModuleName(ModuleNames.SYSTEM)
        val WEB = ModuleName(ModuleNames.WEB)
        val FRONTMATTER = ModuleName(ModuleNames.FRONTMATTER)
        val HOOKS = ModuleName(ModuleNames.HOOKS)
        val CONFIG = ModuleName(ModuleNames.CONFIG)
        val APP = ModuleName(ModuleNames.APP)
        val OBSIDIAN = ModuleName(ModuleNames.OBSIDIAN)


        val HANDLER_MODULES = setOf(DATE, FILE, SYSTEM, WEB)


        val SPECIAL_MODULES = setOf(FRONTMATTER, HOOKS, CONFIG, APP, OBSIDIAN)

        val ALL_MODULES = HANDLER_MODULES + SPECIAL_MODULES

        fun fromStringOrNull(value: String): ModuleName? =
            if (value.isNotBlank()) ModuleName(value) else null
    }
}

@JvmInline
value class CommandName(val value: String) {
    init {
        require(value.isNotBlank()) { "Command name cannot be blank" }
    }

    override fun toString(): String = value

    companion object {
        fun fromStringOrNull(value: String): CommandName? =
            if (value.isNotBlank()) CommandName(value) else null
    }
}

@JvmInline
value class VariableName(val value: String) {
    init {
        require(value.isNotBlank()) { "Variable name cannot be blank" }
        require(isValidIdentifier(value)) { "Invalid variable name: $value" }
    }

    override fun toString(): String = value

    companion object {
        private val IDENTIFIER_REGEX = Regex("^[a-zA-Z_$][a-zA-Z0-9_$]*$")

        fun isValidIdentifier(name: String): Boolean =
            IDENTIFIER_REGEX.matches(name)

        fun fromStringOrNull(value: String): VariableName? =
            if (value.isNotBlank() && isValidIdentifier(value)) VariableName(value) else null
    }
}

@JvmInline
value class FilePath(val value: String) {
    val fileName: String
        get() = value.substringAfterLast('/').substringAfterLast('\\')

    val extension: String
        get() = fileName.substringAfterLast('.', "")

    val nameWithoutExtension: String
        get() {
            val name = fileName
            val dotIndex = name.lastIndexOf('.')
            return if (dotIndex > 0) name.substring(0, dotIndex) else name
        }

    val parent: FilePath?
        get() {
            val lastSep = maxOf(value.lastIndexOf('/'), value.lastIndexOf('\\'))
            return if (lastSep > 0) FilePath(value.substring(0, lastSep)) else null
        }

    override fun toString(): String = value

    companion object {
        fun fromStringOrNull(value: String?): FilePath? =
            if (!value.isNullOrBlank()) FilePath(value) else null
    }
}

@JvmInline
value class TemplateContent(val value: String) {
    val hasTemplateBlocks: Boolean
        get() = value.contains("<%")

    val lineCount: Int
        get() = value.count { it == '\n' } + 1

    override fun toString(): String = value

    companion object {
        val EMPTY = TemplateContent("")

        fun of(value: String): TemplateContent = TemplateContent(value)
    }
}

@JvmInline
value class RenderedContent(val value: String) {
    override fun toString(): String = value

    companion object {
        val EMPTY = RenderedContent("")

        fun of(value: String): RenderedContent = RenderedContent(value)
    }
}

