package ronsijm.templater.script.methods

import ronsijm.templater.script.ArrowFunction
import ronsijm.templater.utils.Logging


typealias ArrowFunctionExecutor = (ArrowFunction, List<Any?>) -> Any?


object ArrayMethodExecutor {

    private val LOG = Logging.getLogger<ArrayMethodExecutor>()


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
                list.any { it == searchItem || it?.toString() == searchItem?.toString() }
            }
            "indexOf" -> {
                val searchItem = args.getOrNull(0)
                list.indexOfFirst { it == searchItem || it?.toString() == searchItem?.toString() }
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
            "forEach" -> executeForEach(list, args, arrowExecutor)
            "push" -> executePush(list, args)
            "pop" -> executePop(list)
            "shift" -> executeShift(list)
            "concat" -> executeConcat(list, args)
            "flat" -> executeFlat(list, args)
            "sort" -> executeSort(list, args, arrowExecutor)
            "fill" -> executeFill(list, args)
            "reduce" -> executeReduce(list, args, arrowExecutor)
            else -> null
        }
    }


    private fun executeFill(list: List<*>, args: List<Any?>): List<Any?> {
        val fillValue = args.getOrNull(0)
        val start = (args.getOrNull(1) as? Number)?.toInt() ?: 0
        val end = (args.getOrNull(2) as? Number)?.toInt() ?: list.size

        val actualStart = if (start < 0) (list.size + start).coerceAtLeast(0) else start.coerceIn(0, list.size)
        val actualEnd = if (end < 0) (list.size + end).coerceAtLeast(0) else end.coerceIn(0, list.size)



        val result = ArrayList<Any?>(list.size)
        list.forEachIndexed { index, value ->
            result.add(if (index in actualStart until actualEnd) fillValue else value)
        }
        return result
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
            list
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


    private fun executeForEach(
        list: List<*>,
        args: List<Any?>,
        arrowExecutor: ArrowFunctionExecutor
    ): Any? {
        val callback = args.getOrNull(0)
        if (callback is ArrowFunction) {
            list.forEachIndexed { index, item ->

                arrowExecutor(callback, listOf(item, index, list))
            }
        }
        return null
    }


    private fun executePush(list: List<*>, args: List<Any?>): Any {

        return try {
            if (list is MutableList<*>) {
                @Suppress("UNCHECKED_CAST")
                val mutableList = list as MutableList<Any?>
                args.forEach { mutableList.add(it) }
                mutableList.size
            } else {
                list + args
            }
        } catch (e: UnsupportedOperationException) {

            list + args
        }
    }


    private fun executePop(list: List<*>): Any? {
        if (list.isEmpty()) return null


        return try {
            if (list is MutableList<*>) {
                list.removeAt(list.size - 1)
            } else {
                list.lastOrNull()
            }
        } catch (e: UnsupportedOperationException) {

            list.lastOrNull()
        }
    }


    private fun executeShift(list: List<*>): Any? {
        if (list is MutableList<*> && list.isNotEmpty()) {
            return list.removeAt(0)
        }
        return list.firstOrNull()
    }


    private fun executeConcat(list: List<*>, args: List<Any?>): List<*> {
        val result = list.toMutableList()
        for (arg in args) {
            when (arg) {
                is List<*> -> result.addAll(arg)
                else -> result.add(arg)
            }
        }
        return result
    }


    private fun executeFlat(list: List<*>, args: List<Any?>): List<*> {
        val depth = (args.getOrNull(0) as? Number)?.toInt() ?: 1
        return flattenList(list, depth)
    }

    private fun flattenList(list: List<*>, depth: Int): List<*> {
        if (depth <= 0) return list
        val result = mutableListOf<Any?>()
        for (item in list) {
            if (item is List<*>) {
                result.addAll(flattenList(item, depth - 1))
            } else {
                result.add(item)
            }
        }
        return result
    }


    private fun executeSort(
        list: List<*>,
        args: List<Any?>,
        arrowExecutor: ArrowFunctionExecutor
    ): List<*> {
        val callback = args.getOrNull(0)
        val sorted = if (callback is ArrowFunction) {
            list.sortedWith { a, b ->
                val result = arrowExecutor(callback, listOf(a, b))
                (result as? Number)?.toInt() ?: 0
            }
        } else {

            list.sortedBy { it?.toString() ?: "" }
        }


        if (list is MutableList<*>) {
            try {
                @Suppress("UNCHECKED_CAST")
                val mutableList = list as MutableList<Any?>
                mutableList.clear()
                mutableList.addAll(sorted)
                return mutableList
            } catch (e: UnsupportedOperationException) {

                return sorted
            }
        }

        return sorted
    }


    private fun executeReduce(
        list: List<*>,
        args: List<Any?>,
        arrowExecutor: ArrowFunctionExecutor
    ): Any? {
        val callback = args.getOrNull(0) as? ArrowFunction ?: return null
        var accumulator: Any? = args.getOrNull(1)

        for (item in list) {
            accumulator = arrowExecutor(callback, listOf(accumulator, item))
        }

        return accumulator
    }


    fun isSupported(methodName: String): Boolean {
        return methodName in SUPPORTED_METHODS
    }

    private val SUPPORTED_METHODS = setOf(
        "length", "join", "includes", "contains", "indexOf",
        "slice", "reverse", "filter", "map", "find", "some", "every",
        "forEach", "push", "pop", "shift", "concat", "flat", "sort", "fill", "reduce"
    )
}
