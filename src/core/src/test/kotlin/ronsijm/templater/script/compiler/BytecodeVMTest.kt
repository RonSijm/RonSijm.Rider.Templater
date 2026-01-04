package ronsijm.templater.script.compiler

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.TestContextFactory
import ronsijm.templater.script.ScriptContext


class BytecodeVMTest {

    private fun createContext(): ScriptContext = ScriptContext()

    private fun createVM(context: ScriptContext) = BytecodeVM(
        context,
        functionCaller = { _, _ -> null },
        methodCaller = { _, _, _ -> null },
        propertyGetter = { _, _ -> null },
        indexGetter = { _, _ -> null }
    )



    @Test
    fun `test VM executes SUB_INT_INT opcode`() {
        val context = createContext()
        val vm = createVM(context)

        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 10)
        builder.emit(OpCode.PUSH_INT, 3)
        builder.emit(OpCode.SUB_INT_INT, 0)

        val code = builder.build("10 - 3")
        val result = vm.execute(code)

        assertEquals(7, result)
    }

    @Test
    fun `test VM executes DIV_INT_INT opcode with exact division`() {
        val context = createContext()
        val vm = createVM(context)

        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 20)
        builder.emit(OpCode.PUSH_INT, 4)
        builder.emit(OpCode.DIV_INT_INT, 0)

        val code = builder.build("20 / 4")
        val result = vm.execute(code)

        assertEquals(5, result)
    }

    @Test
    fun `test VM executes DIV_INT_INT opcode with remainder returns double`() {
        val context = createContext()
        val vm = createVM(context)

        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 10)
        builder.emit(OpCode.PUSH_INT, 3)
        builder.emit(OpCode.DIV_INT_INT, 0)

        val code = builder.build("10 / 3")
        val result = vm.execute(code)

        assertTrue(result is Double)
        assertEquals(3.3333333333333335, result)
    }

    @Test
    fun `test VM executes MOD_INT_INT opcode`() {
        val context = createContext()
        val vm = createVM(context)

        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 17)
        builder.emit(OpCode.PUSH_INT, 5)
        builder.emit(OpCode.MOD_INT_INT, 0)

        val code = builder.build("17 % 5")
        val result = vm.execute(code)

        assertEquals(2, result)
    }

    @Test
    fun `test VM executes NEG_INT opcode`() {
        val context = createContext()
        val vm = createVM(context)

        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 42)
        builder.emit(OpCode.NEG_INT, 0)

        val code = builder.build("-42")
        val result = vm.execute(code)

        assertEquals(-42, result)
    }

    @Test
    fun `test VM executes LT_INT_INT opcode`() {
        val context = createContext()
        val vm = createVM(context)

        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 3)
        builder.emit(OpCode.PUSH_INT, 5)
        builder.emit(OpCode.LT_INT_INT, 0)

        val code = builder.build("3 < 5")
        val result = vm.execute(code)

        assertEquals(true, result)
    }

    @Test
    fun `test VM executes LE_INT_INT opcode`() {
        val context = createContext()
        val vm = createVM(context)

        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 5)
        builder.emit(OpCode.PUSH_INT, 5)
        builder.emit(OpCode.LE_INT_INT, 0)

        val code = builder.build("5 <= 5")
        val result = vm.execute(code)

        assertEquals(true, result)
    }

    @Test
    fun `test VM executes GT_INT_INT opcode`() {
        val context = createContext()
        val vm = createVM(context)

        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 5)
        builder.emit(OpCode.PUSH_INT, 3)
        builder.emit(OpCode.GT_INT_INT, 0)

        val code = builder.build("5 > 3")
        val result = vm.execute(code)

        assertEquals(true, result)
    }

    @Test
    fun `test VM executes GE_INT_INT opcode`() {
        val context = createContext()
        val vm = createVM(context)

        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 5)
        builder.emit(OpCode.PUSH_INT, 5)
        builder.emit(OpCode.GE_INT_INT, 0)

        val code = builder.build("5 >= 5")
        val result = vm.execute(code)

        assertEquals(true, result)
    }

    @Test
    fun `test VM executes EQ_INT_INT opcode`() {
        val context = createContext()
        val vm = createVM(context)

        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 5)
        builder.emit(OpCode.PUSH_INT, 5)
        builder.emit(OpCode.EQ_INT_INT, 0)

        val code = builder.build("5 == 5")
        val result = vm.execute(code)

        assertEquals(true, result)
    }

    @Test
    fun `test VM executes NE_INT_INT opcode`() {
        val context = createContext()
        val vm = createVM(context)

        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 5)
        builder.emit(OpCode.PUSH_INT, 3)
        builder.emit(OpCode.NE_INT_INT, 0)

        val code = builder.build("5 != 3")
        val result = vm.execute(code)

        assertEquals(true, result)
    }



    @Test
    fun `test VM handles negative numbers with SUB_INT_INT`() {
        val context = createContext()
        val vm = createVM(context)

        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 3)
        builder.emit(OpCode.PUSH_INT, 10)
        builder.emit(OpCode.SUB_INT_INT, 0)

        val code = builder.build("3 - 10")
        val result = vm.execute(code)

        assertEquals(-7, result)
    }

    @Test
    fun `test VM handles zero with MOD_INT_INT`() {
        val context = createContext()
        val vm = createVM(context)

        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 10)
        builder.emit(OpCode.PUSH_INT, 5)
        builder.emit(OpCode.MOD_INT_INT, 0)

        val code = builder.build("10 % 5")
        val result = vm.execute(code)

        assertEquals(0, result)
    }

    @Test
    fun `test VM handles large numbers with MUL_INT_INT`() {
        val context = createContext()
        val vm = createVM(context)

        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 1000)
        builder.emit(OpCode.PUSH_INT, 1000)
        builder.emit(OpCode.MUL_INT_INT, 0)

        val code = builder.build("1000 * 1000")
        val result = vm.execute(code)

        assertEquals(1000000, result)
    }

    @Test
    fun `test VM comparison opcodes return boolean`() {
        val context = createContext()
        val vm = createVM(context)


        val builder1 = BytecodeBuilder()
        builder1.emit(OpCode.PUSH_INT, 5)
        builder1.emit(OpCode.PUSH_INT, 3)
        builder1.emit(OpCode.GT_INT_INT, 0)

        val result1 = vm.execute(builder1.build("5 > 3"))
        assertTrue(result1 is Boolean)
        assertEquals(true, result1)

        val builder2 = BytecodeBuilder()
        builder2.emit(OpCode.PUSH_INT, 3)
        builder2.emit(OpCode.PUSH_INT, 5)
        builder2.emit(OpCode.GT_INT_INT, 0)

        val result2 = vm.execute(builder2.build("3 > 5"))
        assertTrue(result2 is Boolean)
        assertEquals(false, result2)
    }

    @Test
    fun `test VM handles complex expression with multiple specialized opcodes`() {
        val context = createContext()
        val vm = createVM(context)



        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 10)
        builder.emit(OpCode.PUSH_INT, 3)
        builder.emit(OpCode.SUB_INT_INT, 0)
        builder.emit(OpCode.PUSH_INT, 5)
        builder.emit(OpCode.PUSH_INT, 2)
        builder.emit(OpCode.MUL_INT_INT, 0)
        builder.emit(OpCode.LT, 0)

        val code = builder.build("(10 - 3) < (5 * 2)")
        val result = vm.execute(code)

        assertEquals(true, result)
    }
}
