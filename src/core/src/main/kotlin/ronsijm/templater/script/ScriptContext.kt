package ronsijm.templater.script

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference


class ScriptContext {

    private object NullSentinel

    private val variables = ConcurrentHashMap<String, Any>()
    private val resultAccumulator = AtomicReference(StringBuilder())


    @Volatile private var returnRequested = AtomicBoolean(false)


    private val returnValue = AtomicReference<Any?>(null)

    fun initializeResultAccumulator(currentOutput: String) {
        resultAccumulator.set(StringBuilder(currentOutput))
        returnRequested.set(false)
    }

    fun getResultAccumulator() = resultAccumulator.get().toString()
    fun appendToResult(value: String) { resultAccumulator.get().append(value) }
    fun setResult(value: String) { resultAccumulator.set(StringBuilder(value)) }

    fun getVariable(name: String): Any? {
        val value = variables[name]
        return if (value === NullSentinel) null else value
    }


    fun finalizeStringBuilders() {
        variables.replaceAll { _, value ->
            if (value is StringBuilder) value.toString() else value
        }
    }

    fun setVariable(name: String, value: Any?) {
        variables[name] = value ?: NullSentinel
    }

    fun removeVariable(name: String) { variables.remove(name) }

    fun hasVariable(name: String) = variables.containsKey(name)


    fun getAllVariables(): Map<String, Any?> {
        return variables.mapValues { (_, value) ->
            when {
                value === NullSentinel -> null
                value is StringBuilder -> value.toString()
                else -> value
            }
        }
    }


    fun requestReturn() { returnRequested.set(true) }


    fun isReturnRequested() = returnRequested.get()


    fun resetReturn() { returnRequested.set(false) }


    fun setReturnValue(value: Any?) { returnValue.set(value) }


    fun getReturnValue(): Any? = returnValue.get()


    fun clearReturnState() {
        returnRequested.set(false)
        returnValue.set(null)
    }


    fun clearVariable(name: String) { variables.remove(name) }
}