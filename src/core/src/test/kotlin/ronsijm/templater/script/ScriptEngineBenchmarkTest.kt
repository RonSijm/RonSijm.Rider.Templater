package ronsijm.templater.script

import ronsijm.templater.TestContextFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*


class ScriptEngineBenchmarkTest {

    @Test
    fun `benchmark simple arithmetic`() {
        val engine = TestContextFactory.createScriptEngine()

        println("\n=== Simple Arithmetic Benchmark ===")
        val start = System.nanoTime()
        repeat(1000) {
            engine.execute("const x = 1 + 2 * 3")
        }
        val elapsed = (System.nanoTime() - start) / 1_000_000.0
        println("1000x simple arithmetic (1 + 2 * 3): ${String.format("%.2f", elapsed)}ms (${String.format("%.3f", elapsed/1000)}ms each)")
    }

    @Test
    fun `benchmark variable assignment and retrieval`() {
        val engine = TestContextFactory.createScriptEngine()

        println("\n=== Variable Assignment Benchmark ===")
        val start = System.nanoTime()
        repeat(1000) {
            engine.execute("const y = 42")
            engine.getVariable("y")
        }
        val elapsed = (System.nanoTime() - start) / 1_000_000.0
        println("1000x variable assign+get: ${String.format("%.2f", elapsed)}ms (${String.format("%.3f", elapsed/1000)}ms each)")
    }

    @Test
    fun `benchmark function call`() {
        val engine = TestContextFactory.createScriptEngine()

        println("\n=== Function Call Benchmark ===")
        engine.execute("function add(a, b) { return a + b }")
        val start = System.nanoTime()
        repeat(1000) {
            engine.execute("const r = add(1, 2)")
        }
        val elapsed = (System.nanoTime() - start) / 1_000_000.0
        println("1000x function call add(1,2): ${String.format("%.2f", elapsed)}ms (${String.format("%.3f", elapsed/1000)}ms each)")
    }

    @Test
    fun `benchmark for loop 10 iterations`() {
        val engine = TestContextFactory.createScriptEngine()

        println("\n=== For Loop (10 iterations) Benchmark ===")
        val start = System.nanoTime()
        repeat(100) {
            engine.execute("""
                let sum = 0
                for (let i = 0; i < 10; i++) {
                    sum = sum + i
                }
            """.trimIndent())
        }
        val elapsed = (System.nanoTime() - start) / 1_000_000.0
        println("100x for loop (10 iters): ${String.format("%.2f", elapsed)}ms (${String.format("%.3f", elapsed/100)}ms each)")
    }

    @Test
    fun `benchmark for loop 100 iterations`() {
        val engine = TestContextFactory.createScriptEngine()

        println("\n=== For Loop (100 iterations) Benchmark ===")
        val start = System.nanoTime()
        repeat(10) {
            engine.execute("""
                let sum2 = 0
                for (let i = 0; i < 100; i++) {
                    sum2 = sum2 + i
                }
            """.trimIndent())
        }
        val elapsed = (System.nanoTime() - start) / 1_000_000.0
        println("10x for loop (100 iters): ${String.format("%.2f", elapsed)}ms (${String.format("%.3f", elapsed/10)}ms each)")
    }

    @Test
    fun `benchmark array operations`() {
        val engine = TestContextFactory.createScriptEngine()

        println("\n=== Array Operations Benchmark ===")
        val start = System.nanoTime()
        repeat(100) {
            engine.execute("""
                const arr = [1, 2, 3, 4, 5]
                const len = arr.length
                const first = arr[0]
                const pushed = arr.push(6)
            """.trimIndent())
        }
        val elapsed = (System.nanoTime() - start) / 1_000_000.0
        println("100x array operations: ${String.format("%.2f", elapsed)}ms (${String.format("%.3f", elapsed/100)}ms each)")
    }

    @Test
    fun `benchmark string operations`() {
        val engine = TestContextFactory.createScriptEngine()

        println("\n=== String Operations Benchmark ===")
        val start = System.nanoTime()
        repeat(100) {
            engine.execute("""
                const str = "hello world"
                const upper = str.toUpperCase()
                const split = str.split(" ")
                const joined = split.join("-")
            """.trimIndent())
        }
        val elapsed = (System.nanoTime() - start) / 1_000_000.0
        println("100x string operations: ${String.format("%.2f", elapsed)}ms (${String.format("%.3f", elapsed/100)}ms each)")
    }

    @Test
    fun `benchmark template literal`() {
        val engine = TestContextFactory.createScriptEngine()

        println("\n=== Template Literal Benchmark ===")
        engine.execute("let name = 'World'")
        val start = System.nanoTime()
        repeat(1000) {
            engine.evaluateExpression("`Hello ${'$'}{name}!`")
        }
        val elapsed = (System.nanoTime() - start) / 1_000_000.0
        println("1000x template literal: ${String.format("%.2f", elapsed)}ms (${String.format("%.3f", elapsed/1000)}ms each)")
    }
}

