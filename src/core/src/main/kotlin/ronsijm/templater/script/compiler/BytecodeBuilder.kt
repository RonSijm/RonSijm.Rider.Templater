package ronsijm.templater.script.compiler


class BytecodeBuilder {
    private val opcodes = mutableListOf<Int>()
    private val operands = mutableListOf<Int>()
    private val constants = mutableListOf<Double>()
    private val strings = mutableListOf<String>()
    private val stringIndex = mutableMapOf<String, Int>()

    fun emit(opcode: Int, operand: Int = 0) {
        opcodes.add(opcode)
        operands.add(operand)
    }

    fun addConstant(value: Double): Int {
        val index = constants.size
        constants.add(value)
        return index
    }

    fun addString(value: String): Int {
        return stringIndex.getOrPut(value) {
            val index = strings.size
            strings.add(value)
            index
        }
    }

    fun currentPosition(): Int = opcodes.size

    fun patchJump(position: Int, target: Int) {
        operands[position] = target
    }

    fun build(sourceExpr: String): CompiledExpr {

        peepholeOptimize()

        return CompiledExpr(
            opcodes.toIntArray(),
            operands.toIntArray(),
            constants.toDoubleArray(),
            strings.toTypedArray(),
            sourceExpr
        )
    }


    private fun peepholeOptimize() {
        var i = 0
        while (i < opcodes.size) {

            if (i + 2 < opcodes.size && opcodes[i] == OpCode.PUSH_INT && opcodes[i + 1] == OpCode.PUSH_INT) {
                when (opcodes[i + 2]) {
                    OpCode.ADD -> {
                        opcodes[i + 2] = OpCode.ADD_INT_INT
                        i += 3
                        continue
                    }
                    OpCode.SUB -> {
                        opcodes[i + 2] = OpCode.SUB_INT_INT
                        i += 3
                        continue
                    }
                    OpCode.MUL -> {
                        opcodes[i + 2] = OpCode.MUL_INT_INT
                        i += 3
                        continue
                    }
                    OpCode.DIV -> {
                        opcodes[i + 2] = OpCode.DIV_INT_INT
                        i += 3
                        continue
                    }
                    OpCode.MOD -> {
                        opcodes[i + 2] = OpCode.MOD_INT_INT
                        i += 3
                        continue
                    }
                    OpCode.LT -> {
                        opcodes[i + 2] = OpCode.LT_INT_INT
                        i += 3
                        continue
                    }
                    OpCode.LE -> {
                        opcodes[i + 2] = OpCode.LE_INT_INT
                        i += 3
                        continue
                    }
                    OpCode.GT -> {
                        opcodes[i + 2] = OpCode.GT_INT_INT
                        i += 3
                        continue
                    }
                    OpCode.GE -> {
                        opcodes[i + 2] = OpCode.GE_INT_INT
                        i += 3
                        continue
                    }
                    OpCode.EQ -> {
                        opcodes[i + 2] = OpCode.EQ_INT_INT
                        i += 3
                        continue
                    }
                    OpCode.NE -> {
                        opcodes[i + 2] = OpCode.NE_INT_INT
                        i += 3
                        continue
                    }
                }
            }


            if (i + 1 < opcodes.size && opcodes[i] == OpCode.PUSH_INT && opcodes[i + 1] == OpCode.NEG) {
                opcodes[i + 1] = OpCode.NEG_INT
                i += 2
                continue
            }

            i++
        }
    }
}
