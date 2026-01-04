package ronsijm.templater.script.compiler

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.TestContextFactory
import ronsijm.templater.script.ScriptContext


class SpecializedOpcodesTest {

    private fun createContext(): ScriptContext = ScriptContext()

    private fun createVM(context: ScriptContext) = BytecodeVM(
        context,
        functionCaller = { _, _ -> null },
        methodCaller = { _, _, _ -> null },
        propertyGetter = { _, _ -> null },
        indexGetter = { _, _ -> null }
    )



    @Test
    fun `test SUB_INT_INT specialized subtraction`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        val vm = createVM(context)


        val code = compiler.compile("10 - 3")
        val result = vm.execute(code)
        assertEquals(7, result)


        assertTrue(code.opcodes.contains(OpCode.SUB_INT_INT),
            "Peephole optimizer should emit SUB_INT_INT for integer subtraction")
    }

    @Test
    fun `test DIV_INT_INT specialized division with exact result`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        val vm = createVM(context)


        val code = compiler.compile("20 / 4")
        val result = vm.execute(code)
        assertEquals(5, result)

        assertTrue(code.opcodes.contains(OpCode.DIV_INT_INT))
    }

    @Test
    fun `test DIV_INT_INT specialized division with remainder returns double`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        val vm = createVM(context)


        val code = compiler.compile("10 / 3")
        val result = vm.execute(code)
        assertEquals(3.3333333333333335, result)

        assertTrue(code.opcodes.contains(OpCode.DIV_INT_INT))
    }

    @Test
    fun `test MOD_INT_INT specialized modulo`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        val vm = createVM(context)


        val code = compiler.compile("17 % 5")
        val result = vm.execute(code)
        assertEquals(2, result)

        assertTrue(code.opcodes.contains(OpCode.MOD_INT_INT))
    }

    @Test
    fun `test NEG_INT specialized negation`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        val vm = createVM(context)


        val code = compiler.compile("-42")
        val result = vm.execute(code)
        assertEquals(-42, result)

        assertTrue(code.opcodes.contains(OpCode.NEG_INT))
    }



    @Test
    fun `test LT_INT_INT specialized less than`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        val vm = createVM(context)

        assertEquals(true, vm.execute(compiler.compile("3 < 5")))
        assertEquals(false, vm.execute(compiler.compile("5 < 3")))
        assertEquals(false, vm.execute(compiler.compile("5 < 5")))

        val code = compiler.compile("3 < 5")
        assertTrue(code.opcodes.contains(OpCode.LT_INT_INT))
    }

    @Test
    fun `test LE_INT_INT specialized less than or equal`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        val vm = createVM(context)

        assertEquals(true, vm.execute(compiler.compile("3 <= 5")))
        assertEquals(false, vm.execute(compiler.compile("5 <= 3")))
        assertEquals(true, vm.execute(compiler.compile("5 <= 5")))

        val code = compiler.compile("3 <= 5")
        assertTrue(code.opcodes.contains(OpCode.LE_INT_INT))
    }

    @Test
    fun `test GT_INT_INT specialized greater than`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        val vm = createVM(context)

        assertEquals(false, vm.execute(compiler.compile("3 > 5")))
        assertEquals(true, vm.execute(compiler.compile("5 > 3")))
        assertEquals(false, vm.execute(compiler.compile("5 > 5")))

        val code = compiler.compile("3 > 5")
        assertTrue(code.opcodes.contains(OpCode.GT_INT_INT))
    }

    @Test
    fun `test GE_INT_INT specialized greater than or equal`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        val vm = createVM(context)

        assertEquals(false, vm.execute(compiler.compile("3 >= 5")))
        assertEquals(true, vm.execute(compiler.compile("5 >= 3")))
        assertEquals(true, vm.execute(compiler.compile("5 >= 5")))

        val code = compiler.compile("3 >= 5")
        assertTrue(code.opcodes.contains(OpCode.GE_INT_INT))
    }

    @Test
    fun `test EQ_INT_INT specialized equality`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        val vm = createVM(context)

        assertEquals(true, vm.execute(compiler.compile("5 == 5")))
        assertEquals(false, vm.execute(compiler.compile("5 == 3")))
        assertEquals(true, vm.execute(compiler.compile("0 == 0")))

        val code = compiler.compile("5 == 5")
        assertTrue(code.opcodes.contains(OpCode.EQ_INT_INT))
    }

    @Test
    fun `test NE_INT_INT specialized not equal`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        val vm = createVM(context)

        assertEquals(false, vm.execute(compiler.compile("5 != 5")))
        assertEquals(true, vm.execute(compiler.compile("5 != 3")))
        assertEquals(false, vm.execute(compiler.compile("0 != 0")))

        val code = compiler.compile("5 != 3")
        assertTrue(code.opcodes.contains(OpCode.NE_INT_INT))
    }



    @Test
    fun `test complex expression with multiple specialized opcodes`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        val vm = createVM(context)


        val code = compiler.compile("(10 - 3) * 2 + 5")
        val result = vm.execute(code)
        assertEquals(19, result)



        assertTrue(code.opcodes.contains(OpCode.SUB_INT_INT))


    }

    @Test
    fun `test loop condition with specialized comparison`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        context.setVariable("i", 5)
        context.setVariable("n", 10)
        val vm = createVM(context)


        val code = compiler.compile("i < n")
        val result = vm.execute(code)
        assertEquals(true, result)



        val code2 = compiler.compile("5 < 10")
        assertEquals(true, vm.execute(code2))
        assertTrue(code2.opcodes.contains(OpCode.LT_INT_INT))
    }

    @Test
    fun `test modulo in loop index calculation`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        val vm = createVM(context)


        val code = compiler.compile("17 % 5")
        assertEquals(2, vm.execute(code))
        assertTrue(code.opcodes.contains(OpCode.MOD_INT_INT))
    }

    @Test
    fun `test division with zero returns infinity`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        val vm = createVM(context)


        val code = compiler.compile("10 / 0")
        val result = vm.execute(code)
        assertEquals(Double.POSITIVE_INFINITY, result)
    }

    @Test
    fun `test negative number arithmetic`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        val vm = createVM(context)


        val code = compiler.compile("-5 + 10")
        val result = vm.execute(code)
        assertEquals(5, result)


        assertTrue(code.opcodes.contains(OpCode.NEG_INT))

    }

    @Test
    fun `test chained comparisons in conditional`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        val vm = createVM(context)


        assertEquals(true, vm.execute(compiler.compile("5 > 3")))
        assertEquals(false, vm.execute(compiler.compile("5 < 3")))
        assertEquals(true, vm.execute(compiler.compile("5 >= 5")))
        assertEquals(true, vm.execute(compiler.compile("5 <= 5")))
    }



    @Test
    fun `test peephole optimizer does not optimize non-integer operations`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        context.setVariable("x", 5)
        context.setVariable("y", 3)
        val vm = createVM(context)


        val code = compiler.compile("x + y")
        val result = vm.execute(code)
        assertEquals(8, result)


        assertFalse(code.opcodes.contains(OpCode.ADD_INT_INT))
    }

    @Test
    fun `test peephole optimizer with mixed integer and double`() {
        val compiler = ExpressionCompiler()
        val context = createContext()
        val vm = createVM(context)


        val code = compiler.compile("5 + 3.5")
        val result = vm.execute(code)
        assertEquals(8.5, result)


        assertFalse(code.opcodes.contains(OpCode.ADD_INT_INT))
    }
}
