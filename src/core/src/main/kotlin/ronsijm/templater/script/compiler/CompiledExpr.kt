package ronsijm.templater.script.compiler


class CompiledExpr(
    val opcodes: IntArray,
    val operands: IntArray,
    val constants: DoubleArray,
    val strings: Array<String>,
    val sourceExpr: String
) {
    val size: Int get() = opcodes.size
}
