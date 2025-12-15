package ronsijm.templater.script.methods

import ronsijm.templater.script.ArrowFunction
import ronsijm.templater.utils.Logging

/**
 * Callback type for executing arrow functions
 */
typealias ArrowFunctionExecutor = (ArrowFunction, List<Any?>) -> Any?

/**
 * Executes JavaScript-like array methods on List values.
 * Extracted from ScriptEvaluator to improve code organization.
 */
object ArrayMethodExecutor {

    private val LOG = Logging.getLogger<ArrayMethodExecutor>()

    /**
     * Execute a method on a List value
     * @param list The list to operate on
     * @param methodName The method name (e.g., "join", "filter", "map")
     * @param args The method arguments
     * @param arrowExecutor Callback to execute arrow functions (needed for filter, map, etc.)
     * @return The result of the method call, or null if method not supported
     */
    fun execute(
        list: List<*>,
        methodName: String,
        args: List<Any?>,
        arrowExecutor: ArrowFunctionExecutor
    ): Any? {
        return when (methodName) {
            "length" -> list.size
            "join" -> {
                val separator = args.getOrNull(0)?.toString() ?: ","
                list.joinToString(separator) { it?.toString() ?: "" }
            }
            "includes", "contains" -> {
                val searchItem = args.getOrNull(0)
                list.contains(searchItem) || list.any { it?.toString() == searchItem?.toString() }
            }
            "indexOf" -> {
                val searchItem = args.getOrNull(0)
                val index = list.indexOf(searchItem)
                if (index >= 0) index else list.indexOfFirst { it?.toString() == searchItem?.toString() }
            }
            "slice" -> {
                val start = (args.getOrNull(0) as? Number)?.toInt() ?: 0
                val end = (args.getOrNull(1) as? Number)?.toInt() ?: list.size
                val actualStart = if (start < 0) (list.size + start).coerceAtLeast(0) else start
                val actualEnd = if (end < 0) (list.size + end).coerceAtLeast(0) else end
                list.subList(actualStart.coerceIn(0, list.size), actualEnd.coerceIn(0, list.size))
            }
            "reverse" -> list.reversed()
            "filter" -> executeFilter(list, args, arrowExecutor)
            "map" -> executeMap(list, args, arrowExecutor)
            "find" -> executeFind(list, args, arrowExecutor)
            "some" -> executeSome(list, args, arrowExecutor)
            "every" -> executeEvery(list, args, arrowExecutor)
            else -> null
        }
    }

    private fun executeFilter(
        list: List<*>,
        args: List<Any?>,
        arrowExecutor: ArrowFunctionExecutor
    ): List<*> {
        val callback = args.getOrNull(0)
        LOG?.debug("filter: callback=$callback (type: ${callback?.javaClass})")
        return if (callback is ArrowFunction) {
            list.filter { item ->
                LOG?.debug("filter: processing item=$item")
                val result = arrowExecutor(callback, listOf(item))
                LOG?.debug("filter: result=$result for item=$item")
                result == true || result?.toString() == "true"
            }
        } else {
            LOG?.debug("filter: callback is not ArrowFunction, using default filter")
            // Default: filter out empty/null values
            list.filter { it != null && it.toString().isNotEmpty() }
        }
    }

    private fun executeMap(
        list: List<*>,
        args: List<Any?>,
        arrowExecutor: ArrowFunctionExecutor
    ): List<*> {
        val callback = args.getOrNull(0)
        return if (callback is ArrowFunction) {
            list.map { item -> arrowExecutor(callback, listOf(item)) }
        } else {
            list // No transformation without callback
        }
    }

    private fun executeFind(
        list: List<*>,
        args: List<Any?>,
        arrowExecutor: ArrowFunctionExecutor
    ): Any? {
        val callback = args.getOrNull(0)
        return if (callback is ArrowFunction) {
            list.find { item ->
                val result = arrowExecutor(callback, listOf(item))
                result == true || result?.toString() == "true"
            }
        } else {
            null
        }
    }

    private fun executeSome(
        list: List<*>,
        args: List<Any?>,
        arrowExecutor: ArrowFunctionExecutor
    ): Boolean {
        val callback = args.getOrNull(0)
        return if (callback is ArrowFunction) {
            list.any { item ->
                val result = arrowExecutor(callback, listOf(item))
                result == true || result?.toString() == "true"
            }
        } else {
            list.isNotEmpty()
        }
    }

    private fun executeEvery(
        list: List<*>,
        args: List<Any?>,
        arrowExecutor: ArrowFunctionExecutor
    ): Boolean {
        val callback = args.getOrNull(0)
        return if (callback is ArrowFunction) {
            list.all { item ->
                val result = arrowExecutor(callback, listOf(item))
                result == true || result?.toString() == "true"
            }
        } else {
            true
        }
    }

    /**
     * Check if a method name is a supported array method
     */
    fun isSupported(methodName: String): Boolean {
        return methodName in SUPPORTED_METHODS
    }

    private val SUPPORTED_METHODS = setOf(
        "length", "join", "includes", "contains", "indexOf",
        "slice", "reverse", "filter", "map", "find", "some", "every"
    )
}

