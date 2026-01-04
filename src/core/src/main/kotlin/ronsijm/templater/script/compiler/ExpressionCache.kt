package ronsijm.templater.script.compiler


class ExpressionCache(private val maxSize: Int = 1024) {
    private val cache = LinkedHashMap<String, CompiledExpr>(maxSize, 0.75f, true)
    private val compiler = ExpressionCompiler()


    fun getOrCompile(expression: String): CompiledExpr {
        return cache.getOrPut(expression) {

            if (cache.size >= maxSize) {
                val oldest = cache.keys.first()
                cache.remove(oldest)
            }
            compiler.compile(expression)
        }
    }


    fun clear() {
        cache.clear()
    }


    fun stats(): CacheStats {
        return CacheStats(cache.size, maxSize)
    }
}

data class CacheStats(val size: Int, val maxSize: Int)
