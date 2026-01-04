package ronsijm.templater.script.ast

import ronsijm.templater.common.FrontmatterAccess
import ronsijm.templater.common.ModuleExecutor
import ronsijm.templater.script.ScriptContext
import ronsijm.templater.script.ScriptExecutionCallback
import ronsijm.templater.script.NoOpExecutionCallback
import ronsijm.templater.utils.CancellationChecker
import ronsijm.templater.utils.NoCancellationChecker


@Suppress("UnusedPrivateProperty")
class AstScriptEngine(
    private val frontmatterAccess: FrontmatterAccess,
    private val moduleExecutor: ModuleExecutor,
    private val cancellationChecker: CancellationChecker = NoCancellationChecker,
    private val executionCallback: ScriptExecutionCallback = NoOpExecutionCallback
) {
    private val scriptContext = ScriptContext()

    fun initializeResultAccumulator(currentOutput: String) {
        scriptContext.initializeResultAccumulator(currentOutput)
    }

    fun getResultAccumulator(): String {
        return scriptContext.getResultAccumulator()
    }


    fun execute(script: String): String {
        try {

            val lexer = Lexer(script)
            val tokens = lexer.tokenize()


            val parser = Parser(tokens)
            val program = parser.parse()


            val interpreter = AstInterpreter(
                context = scriptContext,
                functionCaller = { name, args -> callFunction(name, args) },
                methodCaller = { obj, method, args -> callMethod(obj, method, args) }
            )


            val result = interpreter.execute(program)

            return result?.toString() ?: ""
        } catch (e: Lexer.LexerError) {
            return "[Lexer Error: ${e.message}]"
        } catch (e: Parser.ParseError) {
            return "[Parse Error: ${e.message}]"
        } catch (e: AstInterpreter.RuntimeError) {
            return "[Runtime Error: ${e.message}]"
        } catch (e: Exception) {
            return "[Error: ${e.message}]"
        }
    }


    private fun callFunction(name: String, args: List<Any?>): Any? {

        if (name.startsWith("tp.")) {
            val parts = name.removePrefix("tp.").split(".", limit = 2)
            if (parts.size == 2) {
                val module = parts[0]
                val function = parts[1]
                val result = moduleExecutor.executeModuleFunction(module, function, args)
                return result.value
            }
        }


        if (name == "fm" || name.startsWith("fm.")) {
            return handleFrontmatterAccess(name, args)
        }

        return null
    }


    private fun callMethod(obj: Any?, method: String, args: List<Any?>): Any? {

        if (obj is String) {
            return handleStringMethod(obj, method, args)
        }


        if (obj is List<*>) {
            return handleArrayMethod(obj, method, args)
        }


        if (obj is Map<*, *>) {
            return handleMapMethod(obj, method, args)
        }

        return null
    }

    private fun handleFrontmatterAccess(name: String, args: List<Any?>): Any? {
        if (name == "fm" && args.isNotEmpty()) {
            return frontmatterAccess.getValue(listOf(args[0]?.toString() ?: ""))
        }
        if (name.startsWith("fm.")) {
            val key = name.removePrefix("fm.")
            return frontmatterAccess.getValue(listOf(key))
        }
        return null
    }

    private fun handleStringMethod(str: String, method: String, args: List<Any?>): Any? {
        return when (method) {
            "length" -> str.length
            "toUpperCase" -> str.uppercase()
            "toLowerCase" -> str.lowercase()
            "trim" -> str.trim()
            "split" -> args.getOrNull(0)?.toString()?.let { str.split(it) } ?: listOf(str)
            "replace" -> {
                val search = args.getOrNull(0)?.toString() ?: return str
                val replacement = args.getOrNull(1)?.toString() ?: ""
                str.replace(search, replacement)
            }
            "substring" -> {
                val start = (args.getOrNull(0) as? Number)?.toInt() ?: 0
                val end = (args.getOrNull(1) as? Number)?.toInt() ?: str.length
                str.substring(start.coerceIn(0, str.length), end.coerceIn(0, str.length))
            }
            "includes", "contains" -> args.getOrNull(0)?.toString()?.let { str.contains(it) } ?: false
            "startsWith" -> args.getOrNull(0)?.toString()?.let { str.startsWith(it) } ?: false
            "endsWith" -> args.getOrNull(0)?.toString()?.let { str.endsWith(it) } ?: false
            "indexOf" -> args.getOrNull(0)?.toString()?.let { str.indexOf(it) } ?: -1
            "charAt" -> (args.getOrNull(0) as? Number)?.toInt()?.let { str.getOrNull(it)?.toString() }
            else -> null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun handleArrayMethod(list: List<*>, method: String, args: List<Any?>): Any? {
        return when (method) {
            "length" -> list.size
            "push" -> {
                if (list is MutableList<*>) {
                    (list as MutableList<Any?>).add(args.getOrNull(0))
                    list.size
                } else list.size
            }
            "pop" -> {
                if (list is MutableList<*>) {
                    (list as MutableList<Any?>).removeLastOrNull()
                } else list.lastOrNull()
            }
            "shift" -> {
                if (list is MutableList<*>) {
                    (list as MutableList<Any?>).removeFirstOrNull()
                } else list.firstOrNull()
            }
            "join" -> list.joinToString(args.getOrNull(0)?.toString() ?: ",")
            "includes", "contains" -> list.contains(args.getOrNull(0))
            "indexOf" -> list.indexOf(args.getOrNull(0))
            "slice" -> {
                val start = (args.getOrNull(0) as? Number)?.toInt() ?: 0
                val end = (args.getOrNull(1) as? Number)?.toInt() ?: list.size
                list.subList(start.coerceIn(0, list.size), end.coerceIn(0, list.size))
            }
            "map" -> {
                val func = args.getOrNull(0) as? AstFunction ?: return list
                list.map { item ->
                    val tempContext = ScriptContext()
                    tempContext.setVariable(func.parameters.getOrNull(0) ?: "it", item)
                    val interpreter = AstInterpreter(tempContext, ::callFunction, ::callMethod)
                    when (val body = func.body) {
                        is Expression -> interpreter.evaluate(body)
                        is BlockStatement -> interpreter.execute(Program(body.statements, body.location))
                        else -> item
                    }
                }
            }
            "filter" -> {
                val func = args.getOrNull(0) as? AstFunction ?: return list
                list.filter { item ->
                    val tempContext = ScriptContext()
                    tempContext.setVariable(func.parameters.getOrNull(0) ?: "it", item)
                    val interpreter = AstInterpreter(tempContext, ::callFunction, ::callMethod)
                    val result = when (val body = func.body) {
                        is Expression -> interpreter.evaluate(body)
                        is BlockStatement -> interpreter.execute(Program(body.statements, body.location))
                        else -> false
                    }
                    result == true || (result is Number && result.toDouble() != 0.0) || (result is String && result.isNotEmpty())
                }
            }
            "forEach" -> {
                val func = args.getOrNull(0) as? AstFunction ?: return null
                list.forEach { item ->
                    val tempContext = ScriptContext()
                    tempContext.setVariable(func.parameters.getOrNull(0) ?: "it", item)
                    val interpreter = AstInterpreter(tempContext, ::callFunction, ::callMethod)
                    when (val body = func.body) {
                        is Expression -> interpreter.evaluate(body)
                        is BlockStatement -> interpreter.execute(Program(body.statements, body.location))
                        else -> { }
                    }
                }
                null
            }
            "find" -> {
                val func = args.getOrNull(0) as? AstFunction ?: return null
                list.find { item ->
                    val tempContext = ScriptContext()
                    tempContext.setVariable(func.parameters.getOrNull(0) ?: "it", item)
                    val interpreter = AstInterpreter(tempContext, ::callFunction, ::callMethod)
                    val result = when (val body = func.body) {
                        is Expression -> interpreter.evaluate(body)
                        is BlockStatement -> interpreter.execute(Program(body.statements, body.location))
                        else -> false
                    }
                    result == true || (result is Number && result.toDouble() != 0.0) || (result is String && result.isNotEmpty())
                }
            }
            "reverse" -> list.reversed()
            "sort" -> list.sortedWith { a, b ->
                when {
                    a is Number && b is Number -> a.toDouble().compareTo(b.toDouble())
                    else -> a.toString().compareTo(b.toString())
                }
            }
            else -> null
        }
    }

    private fun handleMapMethod(map: Map<*, *>, method: String, args: List<Any?>): Any? {
        return when (method) {
            "keys" -> map.keys.toList()
            "values" -> map.values.toList()
            "entries" -> map.entries.map { listOf(it.key, it.value) }
            "has", "containsKey" -> map.containsKey(args.getOrNull(0))
            "get" -> map[args.getOrNull(0)]
            else -> null
        }
    }


    fun getVariable(name: String): Any? = scriptContext.getVariable(name)


    fun setVariable(name: String, value: Any?) = scriptContext.setVariable(name, value)
}

