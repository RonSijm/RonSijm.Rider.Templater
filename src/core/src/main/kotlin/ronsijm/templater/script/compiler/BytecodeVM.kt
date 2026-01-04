package ronsijm.templater.script.compiler

import ronsijm.templater.script.ScriptContext
import ronsijm.templater.utils.TypeConverter
import ronsijm.templater.script.profiling.ProfilingContext

class BytecodeVM(
    private val context: ScriptContext,
    private val functionCaller: (name: String, args: List<Any?>) -> Any?,
    private val methodCaller: (obj: Any?, method: String, args: List<Any?>) -> Any?,
    private val propertyGetter: (obj: Any?, prop: String) -> Any?,
    private val indexGetter: (obj: Any?, index: Any?) -> Any?
) {
    companion object {

        val profiler get() = ProfilingContext.bytecodeVMProfiler

        fun resetProfiling() {
            ProfilingContext.bytecodeVMProfiler.reset()
        }

        fun getProfilingReport(): String {
            return ProfilingContext.bytecodeVMProfiler.getReport()
        }

        private val reusableArgs = ThreadLocal.withInitial { ArrayList<Any?>(8) }
    }

    private val stack = arrayOfNulls<Any>(256)
    private var sp = 0
    private val varCache = mutableMapOf<String, Any?>()
    private val stableVars = mutableSetOf<String>()

    private var cacheHits = 0
    private var cacheMisses = 0

    fun invalidateVariable(varName: String) {
        varCache.remove(varName)
        stableVars.remove(varName)
    }

    fun clearCache() {
        varCache.clear()
        stableVars.clear()
    }

    fun execute(code: CompiledExpr): Any? {
        val opcodes = code.opcodes
        val operands = code.operands
        val constants = code.constants
        val strings = code.strings
        sp = 0
        var ip = 0

        while (ip < opcodes.size) {
            when (opcodes[ip]) {
                OpCode.PUSH_CONST -> {
                    ProfilingContext.profiled(profiler::pushConstTime, profiler::pushConstCount) {
                        push(constants[operands[ip]])
                    }
                }
                OpCode.PUSH_STRING -> push(strings[operands[ip]])
                OpCode.PUSH_INT -> push(operands[ip])
                OpCode.PUSH_TRUE -> push(true)
                OpCode.PUSH_FALSE -> push(false)
                OpCode.PUSH_NULL -> push(null)
                OpCode.PUSH_UNDEFINED -> push(Unit)
                OpCode.POP -> sp--
                OpCode.DUP -> push(peek())
                OpCode.LOAD_VAR -> {
                    ProfilingContext.profiled(profiler::loadVarTime, profiler::loadVarCount) {
                        val varName = strings[operands[ip]]
                        val value = if (varName in stableVars) {
                            val cached = varCache[varName]
                            if (cached != null || varCache.containsKey(varName)) {
                                cacheHits++
                                cached
                            } else {
                                cacheMisses++
                                val v = context.getVariable(varName)
                                varCache[varName] = v
                                v
                            }
                        } else {
                            val v = context.getVariable(varName)
                            if (varCache.containsKey(varName) && varCache[varName] == v) {
                                stableVars.add(varName)
                            } else {
                                varCache[varName] = v
                            }
                            v
                        }
                        push(value)
                    }
                }
                OpCode.STORE_VAR -> {
                    val varName = strings[operands[ip]]
                    val value = peek()
                    context.setVariable(varName, value)
                    varCache.remove(varName)
                    stableVars.remove(varName)
                }
                OpCode.ADD -> {
                    ProfilingContext.profiled(profiler::addOpTime, profiler::addOpCount) {
                        val b = pop()
                        val a = pop()
                        if (a is Int && b is Int) push(a + b) else push(add(a, b))
                    }
                }
                OpCode.SUB -> {
                    ProfilingContext.profiled(profiler::subOpTime, profiler::subOpCount) {
                        val b = pop()
                        val a = pop()
                        if (a is Int && b is Int) push(a - b) else push(sub(a, b))
                    }
                }
                OpCode.MUL -> {
                    ProfilingContext.profiled(profiler::mulOpTime, profiler::mulOpCount) {
                        val b = pop()
                        val a = pop()
                        if (a is Int && b is Int) push(a * b) else push(mul(a, b))
                    }
                }
                OpCode.DIV -> {
                    ProfilingContext.profiled(profiler::divOpTime, profiler::divOpCount) {
                        val b = pop()
                        val a = pop()
                        if (a is Int && b is Int && b != 0 && a % b == 0) push(a / b) else push(div(a, b))
                    }
                }
                OpCode.MOD -> {
                    ProfilingContext.profiled(profiler::modOpTime, profiler::modOpCount) {
                        val b = pop()
                        val a = pop()
                        if (a is Int && b is Int) push(a % b) else push(mod(a, b))
                    }
                }
                OpCode.NEG -> {
                    val v = pop()
                    push(if (isIntLike(v)) -toInt(v) else -toNum(v))
                }
                OpCode.USHR -> binaryOp { a, b -> (toInt(a).toLong() and 0xFFFFFFFFL shr toInt(b)).toInt() }
                OpCode.SHR -> binaryOp { a, b -> toInt(a) shr toInt(b) }
                OpCode.SHL -> binaryOp { a, b -> toInt(a) shl toInt(b) }
                OpCode.BAND -> binaryOp { a, b -> toInt(a) and toInt(b) }
                OpCode.BOR -> binaryOp { a, b -> toInt(a) or toInt(b) }
                OpCode.BXOR -> binaryOp { a, b -> toInt(a) xor toInt(b) }
                OpCode.EQ -> binaryOp { a, b -> looseEq(a, b) }
                OpCode.NE -> binaryOp { a, b -> !looseEq(a, b) }
                OpCode.SEQ -> binaryOp { a, b -> strictEq(a, b) }
                OpCode.SNE -> binaryOp { a, b -> !strictEq(a, b) }
                OpCode.LT -> binaryOp { a, b -> toNum(a) < toNum(b) }
                OpCode.LE -> binaryOp { a, b -> toNum(a) <= toNum(b) }
                OpCode.GT -> binaryOp { a, b -> toNum(a) > toNum(b) }
                OpCode.GE -> binaryOp { a, b -> toNum(a) >= toNum(b) }
                OpCode.NOT -> push(!truthy(pop()))
                OpCode.JMP -> { ip = operands[ip]; continue }
                OpCode.JMP_IF_FALSE -> if (!truthy(peek())) { ip = operands[ip]; continue }
                OpCode.JMP_IF_TRUE -> if (truthy(peek())) { ip = operands[ip]; continue }
                OpCode.CALL -> {
                    val args = reusableArgs.get()
                    args.clear()
                    val argCount = operands[ip]
                    repeat(argCount) { args.add(0, pop()) }
                    val result = functionCaller(pop().toString(), args)
                    args.clear()
                    push(result)
                }
                OpCode.CALL_METHOD -> {
                    val args = reusableArgs.get()
                    args.clear()
                    val argCount = operands[ip]
                    repeat(argCount) { args.add(0, pop()) }
                    val m = pop() as String
                    val result = methodCaller(pop(), m, args)
                    args.clear()
                    push(result)
                }
                OpCode.GET_PROP -> push(propertyGetter(pop(), strings[operands[ip]]))
                OpCode.GET_INDEX -> {
                    ProfilingContext.profiled(profiler::getIndexTime, profiler::getIndexCount) {
                        val i = pop()
                        val arr = pop()
                        val result = when {
                            arr is List<*> && i is Int -> if (i in arr.indices) arr[i] else null
                            arr is List<*> && i is Double -> {
                                val idx = i.toInt()
                                if (idx in arr.indices) arr[idx] else null
                            }
                            else -> indexGetter(arr, i)
                        }
                        push(result)
                    }
                }
                OpCode.TYPEOF -> push(getTypeOf(pop()))
                OpCode.MAKE_ARRAY -> {
                    val arr = mutableListOf<Any?>()
                    repeat(operands[ip]) { arr.add(0, pop()) }
                    push(arr)
                }
                OpCode.MAKE_OBJECT -> {
                    val obj = mutableMapOf<String, Any?>()
                    repeat(operands[ip]) { val v = pop(); obj[pop() as String] = v }
                    push(obj)
                }

                OpCode.MUL_INT_INT -> {
                    val b = pop() as Int
                    val a = pop() as Int
                    push(a * b)
                }
                OpCode.ADD_INT_INT -> {
                    val b = pop() as Int
                    val a = pop() as Int
                    push(a + b)
                }
                OpCode.SUB_INT_INT -> {
                    val b = pop() as Int
                    val a = pop() as Int
                    push(a - b)
                }
                OpCode.DIV_INT_INT -> {
                    val b = pop() as Int
                    val a = pop() as Int
                    push(if (b != 0 && a % b == 0) a / b else (a.toDouble() / b.toDouble()))
                }
                OpCode.MOD_INT_INT -> {
                    val b = pop() as Int
                    val a = pop() as Int
                    push(a % b)
                }
                OpCode.NEG_INT -> {
                    val a = pop() as Int
                    push(-a)
                }
                OpCode.LT_INT_INT -> {
                    val b = pop() as Int
                    val a = pop() as Int
                    push(a < b)
                }
                OpCode.LE_INT_INT -> {
                    val b = pop() as Int
                    val a = pop() as Int
                    push(a <= b)
                }
                OpCode.GT_INT_INT -> {
                    val b = pop() as Int
                    val a = pop() as Int
                    push(a > b)
                }
                OpCode.GE_INT_INT -> {
                    val b = pop() as Int
                    val a = pop() as Int
                    push(a >= b)
                }
                OpCode.EQ_INT_INT -> {
                    val b = pop() as Int
                    val a = pop() as Int
                    push(a == b)
                }
                OpCode.NE_INT_INT -> {
                    val b = pop() as Int
                    val a = pop() as Int
                    push(a != b)
                }
                OpCode.ARRAY_GET_INT -> {
                    val index = pop() as Int
                    val array = pop() as MutableList<*>
                    push(array[index])
                }
                OpCode.MUL_CONST_ARRAY -> {
                    val constValue = operands[ip]
                    val arrayName = strings[operands[ip + 1]]
                    val indexName = strings[operands[ip + 2]]

                    val array = context.getVariable(arrayName) as MutableList<*>
                    val index = context.getVariable(indexName) as Int
                    val arrayValue = array[index] as Int

                    push(constValue * arrayValue)
                    ip += 2
                }

                OpCode.RETURN -> return if (sp > 0) pop() else null
            }
            ip++
        }
        return if (sp > 0) pop() else null
    }

    private fun push(v: Any?) { stack[sp++] = v }
    private fun pop(): Any? = stack[--sp]
    private fun peek(): Any? = stack[sp - 1]
    private inline fun binaryOp(op: (Any?, Any?) -> Any?) { val b = pop(); push(op(pop(), b)) }

    private fun add(a: Any?, b: Any?): Any? =
        if (a is String || b is String) (a?.toString() ?: "") + (b?.toString() ?: "")
        else numOp(a, b) { x, y -> x + y }

    private fun sub(a: Any?, b: Any?): Any? = numOp(a, b) { x, y -> x - y }
    private fun mul(a: Any?, b: Any?): Any? = numOp(a, b) { x, y -> x * y }
    private fun div(a: Any?, b: Any?): Any? = numOp(a, b) { x, y -> x / y }
    private fun mod(a: Any?, b: Any?): Any? = numOp(a, b) { x, y -> x % y }

    private inline fun numOp(a: Any?, b: Any?, op: (Double, Double) -> Double): Any? {
        val aNum = toNum(a)
        val bNum = toNum(b)
        val result = op(aNum, bNum)
        return if (isIntLike(a) && isIntLike(b) && result == result.toLong().toDouble()) {
            result.toInt()
        } else {
            result
        }
    }

    private fun isIntLike(v: Any?): Boolean = TypeConverter.isIntLike(v)

    private fun toNum(v: Any?): Double = TypeConverter.toDouble(v)

    private fun toInt(v: Any?): Int = TypeConverter.toInt(v)

    private fun truthy(v: Any?): Boolean = when (v) {
        Unit -> false
        else -> TypeConverter.toBoolean(v)
    }

    private fun looseEq(a: Any?, b: Any?): Boolean {
        if (a == b) return true
        if ((a == null && b == Unit) || (a == Unit && b == null)) return true
        if (a is Number && b is Number) return a.toDouble() == b.toDouble()
        if (a is String && b is Number) return a.toDoubleOrNull() == b.toDouble()
        if (a is Number && b is String) return a.toDouble() == b.toDoubleOrNull()
        return false
    }

    private fun strictEq(a: Any?, b: Any?): Boolean {
        if (a == null && b == null) return true
        if (a == null || b == null) return false
        if (a is Number && b is Number) return a.toDouble() == b.toDouble()
        if (a::class != b::class) return false
        return a == b
    }

    private fun getTypeOf(v: Any?): String = when (v) {
        null -> "object"
        Unit -> "undefined"
        is Boolean -> "boolean"
        is Number -> "number"
        is String -> "string"
        is Function<*> -> "function"
        else -> "object"
    }
}
