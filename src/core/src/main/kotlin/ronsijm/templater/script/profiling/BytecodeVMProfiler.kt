package ronsijm.templater.script.profiling


class BytecodeVMProfiler {

    var addOpTime = 0L; var addOpCount = 0L
    var subOpTime = 0L; var subOpCount = 0L
    var mulOpTime = 0L; var mulOpCount = 0L
    var divOpTime = 0L; var divOpCount = 0L
    var modOpTime = 0L; var modOpCount = 0L
    var loadVarTime = 0L; var loadVarCount = 0L
    var getIndexTime = 0L; var getIndexCount = 0L
    var pushConstTime = 0L; var pushConstCount = 0L


    var totalCacheHits = 0L
    var totalCacheMisses = 0L

    fun reset() {
        addOpTime = 0L; addOpCount = 0L
        subOpTime = 0L; subOpCount = 0L
        mulOpTime = 0L; mulOpCount = 0L
        divOpTime = 0L; divOpCount = 0L
        modOpTime = 0L; modOpCount = 0L
        loadVarTime = 0L; loadVarCount = 0L
        getIndexTime = 0L; getIndexCount = 0L
        pushConstTime = 0L; pushConstCount = 0L
        totalCacheHits = 0L
        totalCacheMisses = 0L
    }

    fun getReport(): String {
        val sb = StringBuilder()
        sb.appendLine("=== BYTECODE VM BREAKDOWN ===")
        sb.appendLine("ADD: ${addOpTime / 1_000_000}ms (${addOpCount} ops, ${if (addOpCount > 0) addOpTime / addOpCount / 1000 else 0}탎/op)")
        sb.appendLine("SUB: ${subOpTime / 1_000_000}ms (${subOpCount} ops, ${if (subOpCount > 0) subOpTime / subOpCount / 1000 else 0}탎/op)")
        sb.appendLine("MUL: ${mulOpTime / 1_000_000}ms (${mulOpCount} ops, ${if (mulOpCount > 0) mulOpTime / mulOpCount / 1000 else 0}탎/op)")
        sb.appendLine("DIV: ${divOpTime / 1_000_000}ms (${divOpCount} ops, ${if (divOpCount > 0) divOpTime / divOpCount / 1000 else 0}탎/op)")
        sb.appendLine("MOD: ${modOpTime / 1_000_000}ms (${modOpCount} ops, ${if (modOpCount > 0) modOpTime / modOpCount / 1000 else 0}탎/op)")
        sb.appendLine("LOAD_VAR: ${loadVarTime / 1_000_000}ms (${loadVarCount} ops, ${if (loadVarCount > 0) loadVarTime / loadVarCount / 1000 else 0}탎/op)")
        sb.appendLine("GET_INDEX: ${getIndexTime / 1_000_000}ms (${getIndexCount} ops, ${if (getIndexCount > 0) getIndexTime / getIndexCount / 1000 else 0}탎/op)")
        sb.appendLine("PUSH_CONST: ${pushConstTime / 1_000_000}ms (${pushConstCount} ops, ${if (pushConstCount > 0) pushConstTime / pushConstCount / 1000 else 0}탎/op)")

        val totalLookups = totalCacheHits + totalCacheMisses
        val hitRate = if (totalLookups > 0) (totalCacheHits * 100.0 / totalLookups) else 0.0
        sb.appendLine("\n=== INLINE CACHE STATISTICS ===")
        sb.appendLine("Cache hits: $totalCacheHits")
        sb.appendLine("Cache misses: $totalCacheMisses")
        sb.appendLine("Hit rate: ${"%.1f".format(hitRate)}%")

        return sb.toString()
    }
}
