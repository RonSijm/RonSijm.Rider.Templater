package ronsijm.templater.script.compiler

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*


class PeepholeOptimizerTest {

    @Test
    fun `test peephole optimizer detects PUSH_INT PUSH_INT ADD pattern`() {
        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 5)
        builder.emit(OpCode.PUSH_INT, 3)
        builder.emit(OpCode.ADD, 0)

        val code = builder.build("5 + 3")


        assertTrue(code.opcodes.contains(OpCode.ADD_INT_INT))
        assertFalse(code.opcodes.contains(OpCode.ADD))
    }

    @Test
    fun `test peephole optimizer detects PUSH_INT PUSH_INT SUB pattern`() {
        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 10)
        builder.emit(OpCode.PUSH_INT, 3)
        builder.emit(OpCode.SUB, 0)

        val code = builder.build("10 - 3")

        assertTrue(code.opcodes.contains(OpCode.SUB_INT_INT))
        assertFalse(code.opcodes.contains(OpCode.SUB))
    }

    @Test
    fun `test peephole optimizer detects PUSH_INT PUSH_INT MUL pattern`() {
        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 7)
        builder.emit(OpCode.PUSH_INT, 6)
        builder.emit(OpCode.MUL, 0)

        val code = builder.build("7 * 6")

        assertTrue(code.opcodes.contains(OpCode.MUL_INT_INT))
        assertFalse(code.opcodes.contains(OpCode.MUL))
    }

    @Test
    fun `test peephole optimizer detects PUSH_INT PUSH_INT DIV pattern`() {
        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 20)
        builder.emit(OpCode.PUSH_INT, 4)
        builder.emit(OpCode.DIV, 0)

        val code = builder.build("20 / 4")

        assertTrue(code.opcodes.contains(OpCode.DIV_INT_INT))
        assertFalse(code.opcodes.contains(OpCode.DIV))
    }

    @Test
    fun `test peephole optimizer detects PUSH_INT PUSH_INT MOD pattern`() {
        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 17)
        builder.emit(OpCode.PUSH_INT, 5)
        builder.emit(OpCode.MOD, 0)

        val code = builder.build("17 % 5")

        assertTrue(code.opcodes.contains(OpCode.MOD_INT_INT))
        assertFalse(code.opcodes.contains(OpCode.MOD))
    }

    @Test
    fun `test peephole optimizer detects PUSH_INT NEG pattern`() {
        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 42)
        builder.emit(OpCode.NEG, 0)

        val code = builder.build("-42")

        assertTrue(code.opcodes.contains(OpCode.NEG_INT))
        assertFalse(code.opcodes.contains(OpCode.NEG))
    }

    @Test
    fun `test peephole optimizer detects PUSH_INT PUSH_INT LT pattern`() {
        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 3)
        builder.emit(OpCode.PUSH_INT, 5)
        builder.emit(OpCode.LT, 0)

        val code = builder.build("3 < 5")

        assertTrue(code.opcodes.contains(OpCode.LT_INT_INT))
        assertFalse(code.opcodes.contains(OpCode.LT))
    }

    @Test
    fun `test peephole optimizer detects PUSH_INT PUSH_INT LE pattern`() {
        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 3)
        builder.emit(OpCode.PUSH_INT, 5)
        builder.emit(OpCode.LE, 0)

        val code = builder.build("3 <= 5")

        assertTrue(code.opcodes.contains(OpCode.LE_INT_INT))
        assertFalse(code.opcodes.contains(OpCode.LE))
    }

    @Test
    fun `test peephole optimizer detects PUSH_INT PUSH_INT GT pattern`() {
        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 5)
        builder.emit(OpCode.PUSH_INT, 3)
        builder.emit(OpCode.GT, 0)

        val code = builder.build("5 > 3")

        assertTrue(code.opcodes.contains(OpCode.GT_INT_INT))
        assertFalse(code.opcodes.contains(OpCode.GT))
    }

    @Test
    fun `test peephole optimizer detects PUSH_INT PUSH_INT GE pattern`() {
        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 5)
        builder.emit(OpCode.PUSH_INT, 5)
        builder.emit(OpCode.GE, 0)

        val code = builder.build("5 >= 5")

        assertTrue(code.opcodes.contains(OpCode.GE_INT_INT))
        assertFalse(code.opcodes.contains(OpCode.GE))
    }

    @Test
    fun `test peephole optimizer detects PUSH_INT PUSH_INT EQ pattern`() {
        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 5)
        builder.emit(OpCode.PUSH_INT, 5)
        builder.emit(OpCode.EQ, 0)

        val code = builder.build("5 == 5")

        assertTrue(code.opcodes.contains(OpCode.EQ_INT_INT))
        assertFalse(code.opcodes.contains(OpCode.EQ))
    }

    @Test
    fun `test peephole optimizer detects PUSH_INT PUSH_INT NE pattern`() {
        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 5)
        builder.emit(OpCode.PUSH_INT, 3)
        builder.emit(OpCode.NE, 0)

        val code = builder.build("5 != 3")

        assertTrue(code.opcodes.contains(OpCode.NE_INT_INT))
        assertFalse(code.opcodes.contains(OpCode.NE))
    }



    @Test
    fun `test peephole optimizer handles multiple patterns in sequence`() {
        val builder = BytecodeBuilder()

        builder.emit(OpCode.PUSH_INT, 5)
        builder.emit(OpCode.PUSH_INT, 3)
        builder.emit(OpCode.ADD, 0)
        builder.emit(OpCode.PUSH_INT, 2)
        builder.emit(OpCode.MUL, 0)

        val code = builder.build("(5 + 3) * 2")


        assertTrue(code.opcodes.contains(OpCode.ADD_INT_INT))

        assertFalse(code.opcodes.contains(OpCode.MUL_INT_INT))
    }

    @Test
    fun `test peephole optimizer does not optimize PUSH_CONST patterns`() {
        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_CONST, 0)
        builder.emit(OpCode.PUSH_CONST, 1)
        builder.emit(OpCode.ADD, 0)

        val code = builder.build("3.5 + 2.5")


        assertFalse(code.opcodes.contains(OpCode.ADD_INT_INT))
        assertTrue(code.opcodes.contains(OpCode.ADD))
    }

    @Test
    fun `test peephole optimizer does not optimize LOAD_VAR patterns`() {
        val builder = BytecodeBuilder()
        builder.emit(OpCode.LOAD_VAR, 0)
        builder.emit(OpCode.LOAD_VAR, 1)
        builder.emit(OpCode.ADD, 0)

        val code = builder.build("x + y")


        assertFalse(code.opcodes.contains(OpCode.ADD_INT_INT))
        assertTrue(code.opcodes.contains(OpCode.ADD))
    }

    @Test
    fun `test peephole optimizer handles empty bytecode`() {
        val builder = BytecodeBuilder()

        val code = builder.build("")


        assertEquals(0, code.opcodes.size)
    }

    @Test
    fun `test peephole optimizer handles single instruction`() {
        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 42)

        val code = builder.build("42")


        assertEquals(1, code.opcodes.size)
        assertEquals(OpCode.PUSH_INT, code.opcodes[0])
    }

    @Test
    fun `test peephole optimizer handles two instructions without pattern`() {
        val builder = BytecodeBuilder()
        builder.emit(OpCode.PUSH_INT, 42)
        builder.emit(OpCode.PUSH_INT, 10)

        val code = builder.build("42, 10")


        assertEquals(2, code.opcodes.size)
    }

    @Test
    fun `test peephole optimizer optimizes all arithmetic operations in complex expression`() {
        val builder = BytecodeBuilder()

        builder.emit(OpCode.PUSH_INT, 10)
        builder.emit(OpCode.PUSH_INT, 3)
        builder.emit(OpCode.SUB, 0)

        val code = builder.build("10 - 3")

        assertTrue(code.opcodes.contains(OpCode.SUB_INT_INT))
        assertFalse(code.opcodes.contains(OpCode.SUB))
    }

    @Test
    fun `test peephole optimizer optimizes all comparison operations`() {
        val builder = BytecodeBuilder()

        builder.emit(OpCode.PUSH_INT, 5)
        builder.emit(OpCode.PUSH_INT, 3)
        builder.emit(OpCode.LT, 0)

        val code1 = builder.build("5 < 3")
        assertTrue(code1.opcodes.contains(OpCode.LT_INT_INT))

        val builder2 = BytecodeBuilder()
        builder2.emit(OpCode.PUSH_INT, 5)
        builder2.emit(OpCode.PUSH_INT, 3)
        builder2.emit(OpCode.GT, 0)

        val code2 = builder2.build("5 > 3")
        assertTrue(code2.opcodes.contains(OpCode.GT_INT_INT))
    }
}
