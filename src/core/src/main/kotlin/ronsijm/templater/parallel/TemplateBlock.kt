package ronsijm.templater.parallel


data class TemplateBlock(
    val id: Int,
    val matchText: String,
    val command: String,
    val isExecution: Boolean,
    val leftTrim: String,
    val rightTrim: String,
    val originalStart: Int,
    val originalEnd: Int
)


data class BlockAnalysis(
    val block: TemplateBlock,
    val variablesRead: Set<String>,
    val variablesWritten: Set<String>,
    val isBarrier: Boolean,
    val hasTrWrite: Boolean
) {

    fun dependsOn(other: BlockAnalysis): Boolean {

        if (other.isBarrier) return true


        if (variablesRead.any { it in other.variablesWritten }) return true


        if ("tR" in variablesRead && other.hasTrWrite) return true


        if (variablesWritten.any { it in other.variablesWritten }) return true


        if (hasTrWrite && other.hasTrWrite) return true

        return false
    }
}
