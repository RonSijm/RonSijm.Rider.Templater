package ronsijm.templater.script.compiler

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.TestContextFactory
import ronsijm.templater.script.ScriptContext

class BytecodeCompilerTest {

    private fun createContext(): ScriptContext = ScriptContext()

    @Test
    fun `test simple arithmetic compilation and execution`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        val vm = BytecodeVM(
            context,
            functionCaller = { _, _ -> null },
            methodCaller = { _, _, _ -> null },
            propertyGetter = { _, _ -> null },
            indexGetter = { _, _ -> null }
        )


        val code = compiler.compile("1 + 2 * 3")
        val result = vm.execute(code)
        assertEquals(7, result)
    }

    @Test
    fun `test variable access`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        context.setVariable("x", 10)
        context.setVariable("y", 5)

        val vm = BytecodeVM(
            context,
            functionCaller = { _, _ -> null },
            methodCaller = { _, _, _ -> null },
            propertyGetter = { _, _ -> null },
            indexGetter = { _, _ -> null }
        )

        val code = compiler.compile("x + y")
        val result = vm.execute(code)
        assertEquals(15, result)
    }

    @Test
    fun `test comparison operators`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        val vm = BytecodeVM(
            context,
            functionCaller = { _, _ -> null },
            methodCaller = { _, _, _ -> null },
            propertyGetter = { _, _ -> null },
            indexGetter = { _, _ -> null }
        )

        assertEquals(true, vm.execute(compiler.compile("5 > 3")))
        assertEquals(false, vm.execute(compiler.compile("5 < 3")))
        assertEquals(true, vm.execute(compiler.compile("5 >= 5")))
        assertEquals(true, vm.execute(compiler.compile("3 <= 5")))
        assertEquals(true, vm.execute(compiler.compile("5 == 5")))
        assertEquals(false, vm.execute(compiler.compile("5 != 5")))
    }

    @Test
    fun `test bitwise operators`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        val vm = BytecodeVM(
            context,
            functionCaller = { _, _ -> null },
            methodCaller = { _, _, _ -> null },
            propertyGetter = { _, _ -> null },
            indexGetter = { _, _ -> null }
        )


        assertEquals(1, vm.execute(compiler.compile("8 >>> 3")))

        assertEquals(1, vm.execute(compiler.compile("8 >> 3")))

        assertEquals(16, vm.execute(compiler.compile("2 << 3")))
    }

    @Test
    fun `test ternary operator`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        val vm = BytecodeVM(
            context,
            functionCaller = { _, _ -> null },
            methodCaller = { _, _, _ -> null },
            propertyGetter = { _, _ -> null },
            indexGetter = { _, _ -> null }
        )

        assertEquals(10, vm.execute(compiler.compile("true ? 10 : 20")))
        assertEquals(20, vm.execute(compiler.compile("false ? 10 : 20")))
    }

    @Test
    fun `test string concatenation`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        val vm = BytecodeVM(
            context,
            functionCaller = { _, _ -> null },
            methodCaller = { _, _, _ -> null },
            propertyGetter = { _, _ -> null },
            indexGetter = { _, _ -> null }
        )

        val code = compiler.compile("\"hello\" + \" \" + \"world\"")
        assertEquals("hello world", vm.execute(code))
    }

    @Test
    fun `test array index access`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        val arr = mutableListOf(10, 20, 30, 40, 50)
        context.setVariable("a", arr)
        context.setVariable("i", 2)
        context.setVariable("q", 5)

        val vm = BytecodeVM(
            context,
            functionCaller = { _, _ -> null },
            methodCaller = { _, _, _ -> null },
            propertyGetter = { _, _ -> null },
            indexGetter = { obj, index ->
                when (obj) {
                    is List<*> -> {
                        val idx = (index as? Number)?.toInt() ?: return@BytecodeVM null
                        if (idx in obj.indices) obj[idx] else null
                    }
                    else -> null
                }
            }
        )


        val code1 = compiler.compile("a[i]")
        assertEquals(30, vm.execute(code1))


        val code2 = compiler.compile("a[i - 1]")
        assertEquals(20, vm.execute(code2))



        val code3 = compiler.compile("10 * a[i] + q * (i + 1)")
        assertEquals(315, vm.execute(code3))
    }

    @Test
    fun `benchmark compiled vs interpreted`() {
        val cache = ExpressionCache()
        val context = createContext()
        context.setVariable("a", 5)
        context.setVariable("b", 3)

        val vm = BytecodeVM(
            context,
            functionCaller = { _, _ -> null },
            methodCaller = { _, _, _ -> null },
            propertyGetter = { _, _ -> null },
            indexGetter = { _, _ -> null }
        )

        println("\n=== Bytecode Compiler Benchmark ===")


        val expressions = listOf(
            "1 + 2 * 3" to 10000,
            "a + b * 2" to 10000,
            "(1 + 2) * (3 + 4)" to 10000,
            "a > b ? a : b" to 10000,
            "8 >>> 3" to 10000
        )

        for ((expr, iterations) in expressions) {

            repeat(100) { vm.execute(cache.getOrCompile(expr)) }


            val start = System.nanoTime()
            repeat(iterations) {
                vm.execute(cache.getOrCompile(expr))
            }
            val time = (System.nanoTime() - start) / 1_000_000.0

            println("${iterations}x '$expr': ${String.format("%.2f", time)}ms (${String.format("%.4f", time / iterations)}ms each)")
        }

        println("=== End Benchmark ===\n")
    }
}
