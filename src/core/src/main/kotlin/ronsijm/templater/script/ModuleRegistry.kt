package ronsijm.templater.script

import ronsijm.templater.common.CancelledResult
import ronsijm.templater.common.CommandResult
import ronsijm.templater.common.ErrorResult
import ronsijm.templater.common.FrontmatterAccess
import ronsijm.templater.common.ModuleExecutor
import ronsijm.templater.common.ModuleNames
import ronsijm.templater.common.OkValueResult
import ronsijm.templater.common.Prefixes


class ModuleRegistry(
    private val frontmatterAccess: FrontmatterAccess,
    private val moduleExecutor: ModuleExecutor
) {

    fun executeFunction(functionPath: String, args: List<Any?>): Any? {
        val normalized = functionPath.removePrefix(Prefixes.TP)
        val parts = normalized.split(".")
        if (parts.isEmpty()) return null

        val module = parts[0]
        val function = parts.getOrNull(1) ?: return null

        if (module == ModuleNames.FRONTMATTER) {

            return frontmatterAccess.getValue(listOf(ModuleNames.FRONTMATTER) + parts.drop(1))
        }

        return extractValue(moduleExecutor.executeModuleFunction(module, function, args))
    }


    private fun extractValue(result: CommandResult): Any? {
        return when (result) {
            is OkValueResult<*> -> result.value
            is CancelledResult -> null
            is ErrorResult -> null
            else -> result.toString()
        }
    }
}