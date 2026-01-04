package ronsijm.templater.settings


data class MermaidNodeStyle(
    val fill: String,
    val stroke: String,
    val strokeWidth: String
) {

    fun toClassDef(): String {
        return "fill:$fill,stroke:$stroke,stroke-width:$strokeWidth"
    }

    companion object {

        fun fromClassDef(classDef: String): MermaidNodeStyle {
            val parts = classDef.split(",").associate { part ->
                val (key, value) = part.split(":", limit = 2)
                key.trim() to value.trim()
            }
            return MermaidNodeStyle(
                fill = parts["fill"] ?: "#fff",
                stroke = parts["stroke"] ?: "#333",
                strokeWidth = parts["stroke-width"] ?: "1px"
            )
        }
    }
}


data class MermaidNodeStyles(
    var startEnd: MermaidNodeStyle = MermaidNodeStyle("#9f9", "#333", "2px"),
    var condition: MermaidNodeStyle = MermaidNodeStyle("#ffd700", "#333", "2px"),
    var loop: MermaidNodeStyle = MermaidNodeStyle("#87ceeb", "#333", "2px"),
    var loopEnd: MermaidNodeStyle = MermaidNodeStyle("#b0e0e6", "#333", "1px"),
    var interpolation: MermaidNodeStyle = MermaidNodeStyle("#98fb98", "#333", "1px"),
    var execution: MermaidNodeStyle = MermaidNodeStyle("#bbf", "#333", "1px"),
    var funcDecl: MermaidNodeStyle = MermaidNodeStyle("#dda0dd", "#333", "1px"),
    var funcCall: MermaidNodeStyle = MermaidNodeStyle("#ff9", "#333", "1px"),
    var variable: MermaidNodeStyle = MermaidNodeStyle("#fbf", "#333", "1px"),
    var returnNode: MermaidNodeStyle = MermaidNodeStyle("#ffa07a", "#333", "1px"),
    var fork: MermaidNodeStyle = MermaidNodeStyle("#ff6b6b", "#333", "2px"),
    var join: MermaidNodeStyle = MermaidNodeStyle("#4ecdc4", "#333", "2px"),
    var error: MermaidNodeStyle = MermaidNodeStyle("#f66", "#333", "2px")
) {
    fun toClassDefLines(): List<String> = listOf(
        "classDef startEnd ${startEnd.toClassDef()}",
        "classDef condition ${condition.toClassDef()}",
        "classDef loop ${loop.toClassDef()}",
        "classDef loopEnd ${loopEnd.toClassDef()}",
        "classDef interpolation ${interpolation.toClassDef()}",
        "classDef execution ${execution.toClassDef()}",
        "classDef funcDecl ${funcDecl.toClassDef()}",
        "classDef funcCall ${funcCall.toClassDef()}",
        "classDef variable ${variable.toClassDef()}",
        "classDef returnNode ${returnNode.toClassDef()}",
        "classDef fork ${fork.toClassDef()}",
        "classDef join ${join.toClassDef()}",
        "classDef error ${error.toClassDef()}"
    )


    fun loadFrom(other: MermaidNodeStyles) {
        startEnd = other.startEnd.copy()
        condition = other.condition.copy()
        loop = other.loop.copy()
        loopEnd = other.loopEnd.copy()
        interpolation = other.interpolation.copy()
        execution = other.execution.copy()
        funcDecl = other.funcDecl.copy()
        funcCall = other.funcCall.copy()
        variable = other.variable.copy()
        returnNode = other.returnNode.copy()
        fork = other.fork.copy()
        join = other.join.copy()
        error = other.error.copy()
    }

    companion object {

        fun defaults(): MermaidNodeStyles = MermaidNodeStyles()


        val STYLE_NAMES = listOf(
            "startEnd" to "Start/End nodes",
            "condition" to "Condition (if/else)",
            "loop" to "Loop start",
            "loopEnd" to "Loop end",
            "interpolation" to "Interpolation (<%= %>)",
            "execution" to "Execution (<% %>)",
            "funcDecl" to "Function declaration",
            "funcCall" to "Function call",
            "variable" to "Variable assignment",
            "returnNode" to "Return statement",
            "fork" to "Fork (parallel start)",
            "join" to "Join (parallel end)",
            "error" to "Error"
        )
    }
}

