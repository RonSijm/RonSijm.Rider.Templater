package ronsijm.templater.common

interface FrontmatterAccess {
    fun getValue(parts: List<String>): Any?
    fun getAll(): Map<String, Any>
}

interface ModuleExecutor {
    fun executeModuleFunction(module: String, function: String, args: List<Any?>): CommandResult
}

